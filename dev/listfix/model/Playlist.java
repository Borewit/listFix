/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2010 Jeremy Caron
 * 
 * This file is part of listFix().
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.model;

import listfix.model.enums.PlaylistType;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import listfix.io.FileLauncher;
import listfix.io.FileWriter;
import listfix.io.IPlaylistReader;
import listfix.io.PlaylistReaderFactory;

import listfix.util.FileNameTokenizer;
import listfix.util.UnicodeUtils;

import listfix.view.support.IPlaylistModifiedListener;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;

public class Playlist
{
	private final static String FS = System.getProperty("file.separator");
	private final static String BR = System.getProperty("line.separator");
	private final static String HOME_DIR = System.getProperty("user.home");
	private static int _newListCount = -1;
	private File _file;
	private List<PlaylistEntry> _entries = new ArrayList<PlaylistEntry>();
	private List<PlaylistEntry> _originalEntries = new ArrayList<PlaylistEntry>();
	private boolean _utfFormat = false;
	private PlaylistType _type = PlaylistType.UNKNOWN;
	private int _fixedCount;
	private int _urlCount;
	private int _missingCount;
	private boolean _isModified;
	private boolean _isNew;

	public enum SortIx
	{
		None,
		Filename,
		Path,
		Status,
		Random,
		Reverse
	}

	private Playlist(List<PlaylistEntry> sublist) throws IOException
	{
		_utfFormat = false;
		setEntries(sublist);
		_file = File.createTempFile("yay", ".m3u");
		_file.deleteOnExit();

		_type = PlaylistType.M3U;
		_isModified = false;
		refreshStatus();
		save(false, null);
	}

	public Playlist(File playlist, IProgressObserver observer) throws IOException
	{
		init(playlist, observer);
	}

	public Playlist() throws IOException
	{
		_newListCount++;
		_file = new File(HOME_DIR + FS + "Untitled-" + _newListCount + ".m3u");
		_file.deleteOnExit();
		_utfFormat = false;
		_type = PlaylistType.M3U;
		_isModified = false;
		_isNew = true;
		refreshStatus();
	}

	private void init(File playlist, IProgressObserver observer)
	{
		try
		{
			IPlaylistReader playlistProcessor = PlaylistReaderFactory.getPlaylistReader(playlist);
			if (observer != null)
			{
				List<PlaylistEntry> tempEntries = playlistProcessor.readPlaylist(observer);
				if (tempEntries != null)
				{
					this.setEntries(tempEntries);
				}
				else
				{
					return;
				}
			}
			else
			{
				this.setEntries(playlistProcessor.readPlaylist());
			}
			_utfFormat = playlistProcessor.getEncoding().equals("UTF-8");
			_file = playlist;
			_type = playlistProcessor.getPlaylistType();
			if (_type == PlaylistType.PLS)
			{
				// let's override our previous determination in the PLS case so we don't end up saving it out incorrectly
				_utfFormat = false;
			}
			_isModified = false;
			refreshStatus();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public Playlist getSublist(int[] rows) throws IOException
	{
		List<PlaylistEntry> tempList = new ArrayList<PlaylistEntry>();
		for (int i : rows)
		{
			tempList.add(_entries.get(i));
		}
		return new Playlist(tempList);
	}

	/**
	 * @return the _type
	 */
	public PlaylistType getType()
	{
		return _type;
	}

	/**
	 * @param _type the _type to set
	 */
	public void setType(PlaylistType type)
	{
		this._type = type;
	}

	public File getFile()
	{
		return _file;
	}

	public void setFile(File file)
	{
		this._file = file;
	}

	public void addModifiedListener(IPlaylistModifiedListener listener)
	{
		if (listener != null)
		{
			if (!_listeners.contains(listener))
			{
				_listeners.add(listener);
			}
		}
	}

	public void removeModifiedListener(IPlaylistModifiedListener listener)
	{
		_listeners.remove(listener);
	}

	public List<IPlaylistModifiedListener> getModifiedListeners()
	{
		return _listeners;
	}

	private void firePlaylistModified()
	{
		refreshStatus();
		for (IPlaylistModifiedListener listener : _listeners)
		{
			if (listener != null)
			{
				listener.playlistModified(this);
			}
		}
	}
	List<IPlaylistModifiedListener> _listeners = new ArrayList<IPlaylistModifiedListener>();

	private void setEntries(List<PlaylistEntry> aEntries)
	{
		// TODO: Somehow track this part of the task
		_entries = aEntries;
		_originalEntries.clear();
		for (int i = 0; i < _entries.size(); i++)
		{
			_originalEntries.add((PlaylistEntry) _entries.get(i).clone());
		}
	}

	public int size()
	{
		return _entries.size();
	}

	public boolean playlistModified()
	{
		boolean result = false;
		if (_originalEntries.size() != _entries.size())
		{
			result = true;
		}
		else
		{
			for (int i = 0; i < _entries.size(); i++)
			{
				PlaylistEntry entryA = _entries.get(i);
				PlaylistEntry entryB = _originalEntries.get(i);
				if ((entryA.isURL() && entryB.isURL()) || (!entryA.isURL() && !entryB.isURL()))
				{
					if (!entryA.isURL())
					{
						if (!entryA.getFile().getPath().equals(entryB.getFile().getPath()))
						{
							result = true;
							break;
						}
					}
					else
					{
						if (!entryA.getURI().toString().equals(entryB.getURI().toString()))
						{
							result = true;
							break;
						}
					}
				}
				else
				{
					result = true;
					break;
				}
			}
		}
		return result;
	}

	private void refreshStatus()
	{
		if (_originalEntries.size() != _entries.size())
		{
			_isModified = true;
		}

		_urlCount = 0;
		_missingCount = 0;
		_fixedCount = 0;

		for (int ix = 0; ix < _entries.size(); ix++)
		{
			PlaylistEntry entry = _entries.get(ix);
			boolean entryIsUrl = entry.isURL();
			if (entryIsUrl)
			{
				_urlCount++;
			}
			else if (!entry.isFound())
			{
				_missingCount++;
			}
			if (entry.isFixed())
			{
				_fixedCount++;
			}

			if (!isModified())
			{
				PlaylistEntry origEntry = _originalEntries.get(ix);
				boolean origIsUrl = origEntry.isURL();
				if (entryIsUrl == origIsUrl)
				{
					if (!entryIsUrl)
					{
						if (!entry.getFile().getPath().equalsIgnoreCase(origEntry.getFile().getPath()))
						{
							_isModified = true;
						}
					}
					else
					{
						if (!entry.getURI().equals(origEntry.getURI()))
						{
							_isModified = true;
						}
					}
				}
				else
				{
					_isModified = true;
				}
			}
		}

	}

	public int getFixedCount()
	{
		return _fixedCount;
	}

	public int getUrlCount()
	{
		return _urlCount;
	}

	public int getMissingCount()
	{
		return _missingCount;
	}

	public boolean isModified()
	{
		return _isModified;
	}

	public String getFilename()
	{
		if (_file == null)
		{
			return "";
		}
		return _file.getName();
	}

	public boolean isEmpty()
	{
		return _entries.isEmpty();
	}

	public void play()
	{
		try
		{
			FileLauncher.launch(_file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean isUtfFormat()
	{
		return _utfFormat;
	}

	public void setUtfFormat(boolean utfFormat)
	{
		this._utfFormat = utfFormat;
	}
	
	public boolean isNew()
	{
		return _isNew;
	}

	public void replace(int index, PlaylistEntry newEntry)
	{

		if (!_entries.get(index).isFound())
		{
			newEntry.markFixedIfFound();
		}
		_entries.set(index, newEntry);
		firePlaylistModified();
	}

	public void moveUp(int[] indexes)
	{
		Arrays.sort(indexes);
		int ceiling = 0;
		for (int ix = 0; ix < indexes.length; ix++)
		{
			int rowIx = indexes[ix];
			if (rowIx != ceiling)
			{
				Collections.swap(_entries, rowIx, rowIx - 1);
				indexes[ix] = rowIx - 1;
			}
			else
			{
				ceiling++;
			}
		}
		firePlaylistModified();
	}

	public void moveTo(int initialPos, int finalPos)
	{
		PlaylistEntry temp = _entries.get(initialPos);
		_entries.remove(initialPos);
		_entries.add(finalPos, temp);
		firePlaylistModified();
	}

	public void moveDown(int[] indexes)
	{
		Arrays.sort(indexes);
		int floor = _entries.size() - 1;
		for (int ix = indexes.length - 1; ix >= 0; ix--)
		{
			int rowIx = indexes[ix];
			if (rowIx != floor)
			{
				Collections.swap(_entries, rowIx, rowIx + 1);
				indexes[ix] = rowIx + 1;
			}
			else
			{
				floor--;
			}
		}
		firePlaylistModified();
	}

	public int add(File[] files, IProgressObserver<String> observer) throws FileNotFoundException, IOException
	{
		List<PlaylistEntry> newEntries = getEntriesForFiles(files, observer);
		if (newEntries != null)
		{
			_entries.addAll(newEntries);
			firePlaylistModified();
			return newEntries.size();
		}
		else
		{
			return 0;
		}
	}

	public int add(int ix, File[] files, IProgressObserver<String> observer) throws FileNotFoundException, IOException
	{
		List<PlaylistEntry> newEntries = getEntriesForFiles(files, observer);
		if (newEntries != null)
		{
			_entries.addAll(ix + 1, newEntries);
			firePlaylistModified();
			return newEntries.size();
		}
		else
		{
			return 0;
		}
	}

	private List<PlaylistEntry> getEntriesForFiles(File[] files, IProgressObserver<String> observer) throws FileNotFoundException, IOException
	{
		ProgressAdapter<String> progress = ProgressAdapter.wrap(observer);
		progress.setTotal(files.length);

		List<PlaylistEntry> ents = new ArrayList<PlaylistEntry>();
		for (File file : files)
		{
			if (!observer.getCancelled())
			{
				if (Playlist.isPlaylist(file))
				{
					// playlist file
					IPlaylistReader reader = PlaylistReaderFactory.getPlaylistReader(file);
					ents.addAll(reader.readPlaylist());
				}
				else
				{
					// regular file
					ents.add(new PlaylistEntry(file, null));
				}
			}
			else
			{
				return null;
			}

			progress.stepCompleted();
		}
		return ents;
	}

	public void changeEntryFileName(int ix, String newName)
	{
		_entries.get(ix).setFileName(newName);
		_entries.get(ix).markFixedIfFound();
		firePlaylistModified();
	}

	// returns positions of repaired rows
	public List<Integer> repair(String[] librayFiles, IProgressObserver observer)
	{
		ProgressAdapter progress = ProgressAdapter.wrap(observer);
		progress.setTotal(_entries.size());

		List<Integer> fixed = new ArrayList<Integer>();
		for (int ix = 0; ix < _entries.size(); ix++)
		{
			if (!observer.getCancelled())
			{
				progress.stepCompleted();

				PlaylistEntry entry = _entries.get(ix);
				if (!entry.isFound() && !entry.isURL())
				{
					entry.findNewLocationFromFileList(librayFiles);
					if (entry.isFound())
					{
						fixed.add(ix);
					}
				}
			}
			else
			{
				return null;
			}
		}

		if (!fixed.isEmpty())
		{
			firePlaylistModified();
		}
		return fixed;
	}

	// similar to repair, but doesn't return repaired row information
	public void batchRepair(String[] fileList, IProgressObserver observer)
	{
		ProgressAdapter progress = ProgressAdapter.wrap(observer);
		progress.setTotal(_entries.size());

		boolean isModified = false;
		for (PlaylistEntry entry : _entries)
		{
			progress.stepCompleted();

			if (!entry.isFound() && !entry.isURL())
			{
				entry.findNewLocationFromFileList(fileList);
				if (!isModified && entry.isFound())
				{
					isModified = true;
				}
			}
		}

		if (isModified)
		{
			firePlaylistModified();
		}
	}

	public List<BatchMatchItem> findClosestMatches(String[] librayFiles, IProgressObserver observer)
	{
		ProgressAdapter progress = ProgressAdapter.wrap(observer);
		progress.setTotal(_entries.size());

		List<BatchMatchItem> fixed = new ArrayList<BatchMatchItem>();
		PlaylistEntry entry = null;
		List<MatchedPlaylistEntry> matches = null;
		for (int ix = 0; ix < _entries.size(); ix++)
		{
			progress.stepCompleted();
			if (!observer.getCancelled())
			{
				entry = _entries.get(ix);
				if (!entry.isFound())
				{
					matches = entry.findClosestMatches(librayFiles, null);
					if (!matches.isEmpty())
					{
						fixed.add(new BatchMatchItem(ix, entry, matches));
					}
				}
			}
			else
			{
				return null;
			}
		}

		return fixed;
	}

	public List<Integer> applyClosestMatchSelections(List<BatchMatchItem> items)
	{
		List<Integer> fixed = new ArrayList<Integer>();
		for (BatchMatchItem item : items)
		{
			if (item.getSelectedIx() >= 0)
			{
				int ix = item.getEntryIx();
				fixed.add(ix);
				_entries.set(ix, item.getSelectedMatch().getPlaylistFile());
				_entries.get(ix).markFixedIfFound();
			}
		}

		if (!fixed.isEmpty())
		{
			firePlaylistModified();
		}
		return fixed;
	}

	public PlaylistEntry get(int index)
	{
		return _entries.get(index);
	}

	public void remove(int[] indexes)
	{
		Arrays.sort(indexes);
		for (int ix = indexes.length - 1; ix >= 0; ix--)
		{
			int rowIx = indexes[ix];
			_entries.remove(rowIx);
		}
		firePlaylistModified();
	}

	public void reorder(SortIx sortIx, boolean isDescending)
	{
		switch (sortIx)
		{
			case Filename:
			case Path:
			case Status:
				Collections.sort(_entries, new EntryComparator(sortIx, isDescending));
				break;

			case Random:
				Collections.shuffle(_entries);
				break;

			case Reverse:
				Collections.reverse(_entries);
				break;
		}
		firePlaylistModified();
	}

	private static class EntryComparator implements Comparator<PlaylistEntry>
	{
		public EntryComparator(SortIx sortIx, boolean isDescending)
		{
			_sortIx = sortIx;
			_isDescending = isDescending;
		}
		final SortIx _sortIx;
		final boolean _isDescending;

		public int compare(PlaylistEntry lhs, PlaylistEntry rhs)
		{
			int rc = 0;

			switch (_sortIx)
			{
				case Filename:
					rc = lhs.getFileName().compareToIgnoreCase(rhs.getFileName());
					break;
				case Path:
					rc = lhs.getPath().compareToIgnoreCase(rhs.getPath());
					break;
				case Status:
					// Randomly chosen order... Found > Fixed > Missing > URL (seemed reasonable)
					if (lhs.isURL())
					{
						if (rhs.isURL())
						{
							rc = 0;
							break;
						}
						rc =  1;
						break;
					}
					else if (!lhs.isFound())
					{
						if (rhs.isURL())
						{
							rc =  -1;
							break;
						}
						else if (!rhs.isFound())
						{
							rc =  0;
							break;
						}
						rc =  1;
						break;
					}
					else if (lhs.isFixed())
					{
						if (!rhs.isFound() || rhs.isURL())
						{
							rc = -1;
							break;
						}
						else if (rhs.isFixed())
						{
							rc = 0;
							break;
						}
						rc = 1;
						break;
					}
					rc =  -1;
					break;
			}

			return _isDescending ? -rc : rc;
		}
	}
	// TODO: track progress? [this is O(n), so it might not need it]

	public int removeDuplicates()
	{
		int removed = 0;
		Set<String> found = new HashSet<String>();
		for (int ix = 0; ix < _entries.size();)
		{
			PlaylistEntry entry = _entries.get(ix);
			String name = entry.getFileName();
			if (found.contains(name))
			{
				// duplicate found, remove
				_entries.remove(ix);
				removed++;
			}
			else
			{
				found.add(name);
				ix++;
			}
		}
		if (removed > 0)
		{
			firePlaylistModified();
		}
		return removed;
	}

	public int removeMissing()
	{
		int removed = 0;
		for (int ix = _entries.size() - 1; ix >= 0; ix--)
		{
			PlaylistEntry entry = _entries.get(ix);
			if (!entry.isURL() && !entry.isFound())
			{
				_entries.remove(ix);
				removed++;
			}
		}
		if (removed > 0)
		{
			firePlaylistModified();
		}
		return removed;
	}

	public void saveAs(File destination, boolean saveRelative, IProgressObserver observer) throws IOException
	{
		_file = destination;
		_type = determinePlaylistType(destination);
		if (_type == PlaylistType.PLS)
		{
			// apparently winamp shits itself if PLS files are saved in UTF-8 (who knew...)
			_utfFormat = false;
		}
		save(saveRelative, observer);
	}

	public final void save(boolean saveRelative, IProgressObserver observer) throws IOException
	{
		// avoid resetting total if part of batch operation
		boolean hasTotal = observer instanceof ProgressAdapter;
		ProgressAdapter progress = ProgressAdapter.wrap(observer);
		if (!hasTotal)
		{
			progress.setTotal(_entries.size());
		}

		if (getType() == PlaylistType.M3U)
		{
			saveM3U(saveRelative, progress);
		}
		else if (getType() == PlaylistType.PLS)
		{
			savePLS(saveRelative, progress);
		}

		// change original _entries
		setEntries(_entries);

		// set entries unfixed
		for(PlaylistEntry entry : _entries)
		{
			entry.setFixed(false);
		}

		_isModified = false;
		_isNew = false;
		firePlaylistModified();
	}

	// TODO: Make cancellable
	private void saveM3U(boolean saveRelative, ProgressAdapter progress) throws IOException
	{
		boolean track = progress != null;

		StringBuilder buffer = new StringBuilder();
		buffer.append("#EXTM3U").append(BR);
		for (int i = 0; i < _entries.size(); i++)
		{
			if (!track || !progress.getCancelled())
			{
				if (track)
				{
					progress.stepCompleted();
				}
				PlaylistEntry entry = _entries.get(i);

				if (!entry.isURL())
				{
					if (!saveRelative && entry.isRelative() && entry.getAbsoluteFile() != null)
					{
						// replace existing relative entry with a new absolute one
						entry = new PlaylistEntry(entry.getAbsoluteFile().getCanonicalFile(), entry.getExtInf());
						_entries.set(i, entry);
					}
					else if (saveRelative && !entry.isRelative() && !entry.isURL())
					{
						// replace existing absolute entry with a new relative one
						String relativePath = FileWriter.getRelativePath(entry.getFile().getAbsoluteFile(), _file);
						entry = new PlaylistEntry(new File(relativePath), entry.getExtInf());
						_entries.set(i, entry);
					}
				}

				buffer.append(entry.toM3UString()).append(BR);
			}
			else
			{
				return;
			}
		}

		if (!track || !progress.getCancelled())
		{
			// TODO: remove saveRelative check?
			if (!saveRelative)
			{
				File dirToSaveIn = _file.getParentFile().getAbsoluteFile();
				if (!dirToSaveIn.exists())
				{
					dirToSaveIn.mkdirs();
				}
			}

			FileOutputStream outputStream = new FileOutputStream(_file);
			if (isUtfFormat() || _file.getName().toLowerCase().endsWith("m3u8"))
			{
				Writer osw = new OutputStreamWriter(outputStream, "UTF8");
				BufferedWriter output = new BufferedWriter(osw);
				output.write(UnicodeUtils.getBOM("UTF-8") + buffer.toString());
				output.close();
				outputStream.close();
				setUtfFormat(true);
			}
			else
			{
				BufferedOutputStream output = new BufferedOutputStream(outputStream);
				output.write(buffer.toString().getBytes());
				output.close();
				outputStream.close();
				setUtfFormat(false);
			}
		}
	}

	// TODO: Make cancellable
	private void savePLS(boolean saveRelative, ProgressAdapter progress) throws IOException
	{
		boolean track = progress != null;

		PlaylistEntry tempEntry = null;
		StringBuilder buffer = new StringBuilder();
		buffer.append("[playlist]").append(BR);
		for (int i = 0; i < _entries.size(); i++)
		{
			if (!track || !progress.getCancelled())
			{
				if (track)
				{
					progress.stepCompleted();
				}
				tempEntry = _entries.get(i);
				if (!saveRelative && tempEntry.isRelative() && tempEntry.getAbsoluteFile() != null)
				{
					tempEntry = new PlaylistEntry(tempEntry.getAbsoluteFile().getCanonicalFile(), tempEntry.getTitle(), tempEntry.getLength());
					_entries.set(i, tempEntry);
				}
				else if (saveRelative && !tempEntry.isRelative() && !tempEntry.isURL())
				{
					// replace existing absolute entry with a new relative one
					String relativePath = FileWriter.getRelativePath(tempEntry.getFile().getAbsoluteFile(), _file);
					tempEntry = new PlaylistEntry(new File(relativePath), tempEntry.getExtInf());
					_entries.set(i, tempEntry);
				}
				buffer.append(tempEntry.toPLSString(i + 1));
			}
			else
			{
				return;
			}
		}

		buffer.append("NumberOfEntries=").append(_entries.size()).append(BR);
		buffer.append("Version=2");

		if (!track || !progress.getCancelled())
		{
			File dirToSaveIn = _file.getParentFile().getAbsoluteFile();
			if (!dirToSaveIn.exists())
			{
				dirToSaveIn.mkdirs();
			}

			if (isUtfFormat())
			{
				FileOutputStream outputStream = new FileOutputStream(_file);
				Writer osw = new OutputStreamWriter(outputStream, "UTF8");
				BufferedWriter output = new BufferedWriter(osw);
				output.write(UnicodeUtils.getBOM("UTF-8") + buffer.toString());
				output.close();
				outputStream.close();
				setUtfFormat(true);
			}
			else
			{
				FileOutputStream outputStream = new FileOutputStream(_file);
				BufferedOutputStream output = new BufferedOutputStream(outputStream);
				output.write(buffer.toString().getBytes());
				output.close();
				outputStream.close();
				setUtfFormat(false);
			}
		}
	}

	public void reload(IProgressObserver observer)
	{
		init(_file, observer);
		firePlaylistModified();
	}

	public static boolean isPlaylist(File input)
	{
		return determinePlaylistType(input) != PlaylistType.UNKNOWN;
	}

	private static PlaylistType determinePlaylistType(File input)
	{
		if (input != null)
		{
			String lowerCaseExtension = FileNameTokenizer.getExtensionFromFileName(input.getName()).toLowerCase();
			if (lowerCaseExtension.equals("m3u") || lowerCaseExtension.equals("m3u8"))
			{
				return PlaylistType.M3U;
			}
			else if (lowerCaseExtension.equals("pls"))
			{
				return PlaylistType.PLS;
			}
		}
		return PlaylistType.UNKNOWN;
	}
}
