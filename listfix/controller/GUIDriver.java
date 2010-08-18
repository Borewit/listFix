/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2009 Jeremy Caron
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

package listfix.controller;

import java.io.*;
import java.util.*;

import listfix.comparators.*;
import listfix.controller.tasks.LocateClosestMatchesTask;
import listfix.controller.tasks.LocateFilesTask;
import listfix.exceptions.*;
import listfix.io.FileWriter;
import listfix.io.IPlaylistReader;
import listfix.io.IniFileReader;
import listfix.io.M3UReader;
import listfix.io.PlaylistReaderFactory;
import listfix.io.UNCFile;
import listfix.model.*;
import listfix.util.ArrayFunctions;

public class GUIDriver
{
	private boolean showMediaDirWindow = false;
	private String[] mediaDir = null;
	private String[] mediaLibraryDirectoryList = null;
	private String[] mediaLibraryFileList = null;
	private Playlist currentList = new Playlist();
	private AppOptions options = new AppOptions();
	private M3UHistory history = new M3UHistory(options.getMaxPlaylistHistoryEntries());
	public static final boolean fileSystemIsCaseSensitive = File.separatorChar == '/';

	public GUIDriver()
	{
		try
		{
			(new FileWriter()).writeDefaultIniFilesIfNeeded();
			IniFileReader initReader = new IniFileReader();
			initReader.readIni();
			options = initReader.getAppOptions();
			mediaDir = initReader.getMediaDirs();
			history = new M3UHistory(options.getMaxPlaylistHistoryEntries());
			history.initHistory(initReader.getHistory());
			mediaLibraryDirectoryList = initReader.getMediaLibrary();
			mediaLibraryFileList = initReader.getMediaLibraryFiles();

			for (String dir : mediaDir)
			{
				if (!new File(dir).exists())
				{
					this.removeMediaDir(dir);
				}
			}

			if (mediaDir.length == 0)
			{
				showMediaDirWindow = true;
			}
		}
		catch (Exception e)
		{
			showMediaDirWindow = true;
			e.printStackTrace();
		}
	}

	public Playlist getPlaylist()
	{
		return currentList;
	}

	public void setPlaylist(Playlist list)
	{
		currentList = list;
	}

	public AppOptions getAppOptions()
	{
		return options;
	}

	public void setAppOptions(AppOptions opts)
	{
		options = opts;
	}

	public String[] getMediaDirs()
	{
		return mediaDir;
	}

	public void setMediaDirs(String[] value)
	{
		mediaDir = value;
	}

	public String[] getMediaLibraryDirectoryList()
	{
		return mediaLibraryDirectoryList;
	}

	public void setMediaLibraryDirectoryList(String[] value)
	{
		mediaLibraryDirectoryList = value;
	}

	public String[] getMediaLibraryFileList()
	{
		return mediaLibraryFileList;
	}

	public void setMediaLibraryFileList(String[] value)
	{
		mediaLibraryFileList = value;
	}

	public boolean getShowMediaDirWindow()
	{
		return showMediaDirWindow;
	}

	public M3UHistory getHistory()
	{
		return history;
	}

	public void playPlaylist()
	{
		currentList.play();
	}

	public void clearM3UHistory()
	{
		history.clearHistory();
		(new FileWriter()).writeMruPlaylists(history);
	}

	public Object[][] closePlaylist()
	{
		setPlaylist(new Playlist());
		Object[][] result = new Object[0][3];
		return result;
	}

	public String[][] guiTableUpdate()
	{
		int size = currentList.getEntries().size();
		String[][] result = null;
		result = new String[size][3];
		for (int i = 0; i < size; i++)
		{
			PlaylistEntry tempEntry = currentList.getEntries().elementAt(i);
			if (!tempEntry.isURL())
			{
				result[i][0] = (i + 1) + ". " + tempEntry.getFileName();
				result[i][1] = tempEntry.getMessage();
				result[i][2] = tempEntry.getPath();
			}
			else
			{
				result[i][0] = (i + 1) + ". " + tempEntry.getURI().toString();
				result[i][1] = tempEntry.getMessage();
				result[i][2] = "";
			}
		}
		return result;
	}

	public String[] getRecentM3Us()
	{
		return history.getM3UFilenames();
	}

	public String[] removeMediaDir(String dir) throws MediaDirNotFoundException
	{
		boolean found = false;
		int i = 0;
		int length = mediaDir.length;
		if (mediaDir.length == 1)
		{
			while ((i < length) && (found != true))
			{
				if (mediaDir[i].equals(dir))
				{
					mediaDir = ArrayFunctions.removeItem(mediaDir, i);
					found = true;
				}
				else
				{
					i++;
				}
			}
			if (found)
			{
				mediaLibraryDirectoryList = new String[0];
				mediaLibraryFileList = new String[0];
				(new FileWriter()).writeIni(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList, options);
			}
			else
			{
				throw new MediaDirNotFoundException(dir + " was not in the media directory list.");
			}
			return mediaDir;
		}
		else
		{
			while ((i < length) && (found != true))
			{
				if (mediaDir[i].equals(dir))
				{
					mediaDir = ArrayFunctions.removeItem(mediaDir, i);
					found = true;
				}
				else
				{
					i++;
				}
			}
			if (found)
			{
				Vector<String> mldVector = new Vector<String>(Arrays.asList(mediaLibraryDirectoryList));
				Vector<String> toRemove = new Vector<String>();
				for (String toTest : mldVector)
				{
					if (toTest.startsWith(dir))
					{
						toRemove.add(toTest);
					}
				}
				mldVector.removeAll(toRemove);
				mediaLibraryDirectoryList = mldVector.toArray(new String[mldVector.size()]);
				mldVector = null;
				toRemove.removeAllElements();

				Vector<String> mlfVector = new Vector<String>(Arrays.asList(mediaLibraryFileList));
				for (String toTest : mlfVector)
				{
					if (toTest.startsWith(dir))
					{
						toRemove.add(toTest);
					}
				}
				mlfVector.removeAll(toRemove);
				mediaLibraryFileList = mlfVector.toArray(new String[mlfVector.size()]);
				mlfVector = null;

				(new FileWriter()).writeIni(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList, options);
			}
			else
			{
				throw new MediaDirNotFoundException(dir + " was not in the media directory list.");
			}
			return mediaDir;
		}
	}

	public String[][] moveUp(int entryIndex)
	{
		Vector<PlaylistEntry> entries = currentList.getEntries();
		PlaylistEntry temp = entries.elementAt(entryIndex);
		entries.removeElementAt(entryIndex);
		entries.insertElementAt(temp, entryIndex - 1);
		return guiTableUpdate();
	}

	public String[][] moveDown(int entryIndex)
	{
		Vector<PlaylistEntry> entries = currentList.getEntries();
		PlaylistEntry temp = entries.elementAt(entryIndex);
		entries.removeElementAt(entryIndex);
		entries.insertElementAt(temp, entryIndex + 1);
		return guiTableUpdate();
	}

	public String[][] moveTo(int initialPos, int finalPos)
	{
		Vector<PlaylistEntry> entries = currentList.getEntries();
		PlaylistEntry temp = entries.elementAt(initialPos);
		entries.removeElementAt(initialPos);
		entries.insertElementAt(temp, finalPos);
		return guiTableUpdate();
	}

	public String[][] delete(int entryIndex)
	{
		currentList.getEntries().removeElementAt(entryIndex);
		return guiTableUpdate();
	}

	public String[][] addEntry(File input)
	{
		PlaylistEntry entryToInsert = new PlaylistEntry(input, "");
		currentList.getEntries().add(entryToInsert);
		return guiTableUpdate();
	}

	public String[][] appendPlaylist(File input) throws FileNotFoundException, IOException
	{
		IPlaylistReader playlistProcessor = PlaylistReaderFactory.getPlaylistReader(input);
		currentList.getEntries().addAll(playlistProcessor.readPlaylist());
		return guiTableUpdate();
	}

	public String[][] insertPlaylist(File input, int index) throws FileNotFoundException, IOException
	{
		IPlaylistReader playlistProcessor = PlaylistReaderFactory.getPlaylistReader(input);
		Vector<PlaylistEntry> temp = playlistProcessor.readPlaylist();
		while (temp.size() > 0)
		{
			PlaylistEntry x = temp.remove(temp.size() - 1);
			currentList.getEntries().insertElementAt(x, index + 1);
		}
		return guiTableUpdate();
	}

	public void setPlaylistFile(File input) throws java.io.FileNotFoundException, java.io.IOException
	{
		currentList.setFile(input);
	}

	public String[][] locateFiles(LocateFilesTask input)
	{
		currentList.updateEntries(input.locateFiles());
		return guiTableUpdate();
	}

	public String[][] locateFile(int entryIndex)
	{
		PlaylistEntry entry = currentList.getEntries().elementAt(entryIndex);
		if (!entry.isURL())
		{
			if (entry.isFound() && !ArrayFunctions.ContainsStringWithPrefix(mediaDir, entry.getAbsoluteFile().getPath(), !fileSystemIsCaseSensitive))
			{
				// do nothing to entries that are found outside of the media library
			}
			else
			{
				entry.findNewLocationFromFileList(mediaLibraryFileList);
			}
		}
		return guiTableUpdate();
	}

	public Vector<MatchedPlaylistEntry> findClosestMatches(LocateClosestMatchesTask input)
	{
		Vector<MatchedPlaylistEntry> result = input.locateClosestMatches();
		Collections.sort(result, new MatchedPlaylistEntryComparator());
		return result;
	}

	public String[][] updateEntryAt(int entryIndex, PlaylistEntry newEntry)
	{
		currentList.getEntries().insertElementAt(newEntry, entryIndex);
		currentList.getEntries().removeElementAt(entryIndex + 1);
		return guiTableUpdate();
	}

	public String[][] randomizePlaylist()
	{
		Vector<PlaylistEntry> result = new Vector<PlaylistEntry>();
		while (currentList.getEntries().size() > 0)
		{
			int index = (int) (Math.random() * currentList.getEntries().size());
			result.add(currentList.getEntries().elementAt(index));
			currentList.getEntries().removeElementAt(index);
		}
		currentList.updateEntries(result);
		return guiTableUpdate();
	}

	public String[][] reversePlaylist()
	{
		Stack<PlaylistEntry> temp = new Stack<PlaylistEntry>();
		while (!currentList.getEntries().isEmpty())
		{
			temp.push(currentList.getEntries().firstElement());
			currentList.getEntries().remove(0);
		}
		while (!temp.empty())
		{
			currentList.getEntries().add(temp.pop());
		}
		return guiTableUpdate();
	}

	public String[][] ascendingFilenameSort()
	{
		Collections.sort(currentList.getEntries(), new AscendingFilenameComparator());
		return guiTableUpdate();
	}

	public String[][] descendingFilenameSort()
	{
		Collections.sort(currentList.getEntries(), new DescendingFilenameComparator());
		return guiTableUpdate();
	}

	public String[][] ascendingStatusSort()
	{
		Collections.sort(currentList.getEntries(), new AscendingStatusComparator());
		return guiTableUpdate();
	}

	public String[][] descendingStatusSort()
	{
		Collections.sort(currentList.getEntries(), new DescendingStatusComparator());
		return guiTableUpdate();
	}

	public String[][] ascendingPathSort()
	{
		Collections.sort(currentList.getEntries(), new AscendingPathComparator());
		return guiTableUpdate();
	}

	public String[][] descendingPathSort()
	{
		Collections.sort(currentList.getEntries(), new DescendingPathComparator());
		return guiTableUpdate();
	}

	public String[][] removeMissing()
	{
		PlaylistEntry tempEntry = null;
		for (int i = 0; i < currentList.getEntries().size(); i++)
		{
			tempEntry = currentList.getEntries().elementAt(i);
			if (!tempEntry.isURL() && !tempEntry.isFound())
			{
				currentList.getEntries().removeElementAt(i--);
			}
		}
		return guiTableUpdate();
	}

	public String[][] updateFileName(int entryIndex, String filename)
	{
		currentList.getEntries().elementAt(entryIndex).setFileName(filename);
		currentList.getEntries().elementAt(entryIndex).recheckFoundStatus();
		return locateFile(entryIndex);
	}

	public String[][] removeDuplicates()
	{
		Vector<PlaylistEntryPosition> sortingList = new Vector<PlaylistEntryPosition>();
		int i = 0;
		while (!currentList.getEntries().isEmpty())
		{
			sortingList.add(new PlaylistEntryPosition(currentList.getEntries().firstElement(), i));
			currentList.getEntries().removeElementAt(0);
			i++;
		}
		// sort by fileName
		Collections.sort(sortingList, new PlaylistEntryPositionFileNameComparator());
		// remove dups
		for (int j = 0; j < sortingList.size() - 1; j++)
		{
			PlaylistEntry temp1 = sortingList.elementAt(j).getPlaylistEntry();
			PlaylistEntry temp2 = sortingList.elementAt(j + 1).getPlaylistEntry();
			if (temp1.getFileName().equals(temp2.getFileName()))
			{
				sortingList.removeElementAt(j + 1);
				j--;
			}
		}
		// sort by position
		Collections.sort(sortingList, new PlaylistEntryPositionIndexComparator());
		// copy back
		while (!sortingList.isEmpty())
		{
			currentList.getEntries().add(sortingList.firstElement().getPlaylistEntry());
			sortingList.removeElementAt(0);
		}
		return guiTableUpdate();
	}

	public String getEntryFileName(int entryIndex)
	{
		return currentList.getEntries().elementAt(entryIndex).getFileName();
	}

	public PlaylistEntry getEntryAt(int entryIndex)
	{
		return currentList.getEntries().elementAt(entryIndex);
	}

	public void savePlaylist() throws Exception
	{
		if (currentList.getFile() != null)
		{
			if (options.getSavePlaylistsWithRelativePaths())
			{
				if (currentList.getType() == PlaylistType.M3U)
				{
					currentList.setEntries((new FileWriter()).writeRelativeM3U(currentList, currentList.getFile()));
				}
				else if (currentList.getType() == PlaylistType.PLS)
				{
					currentList.setEntries((new FileWriter()).writeRelativePLS(currentList, currentList.getFile()));
				}
			}
			else
			{
				if (currentList.getType() == PlaylistType.M3U)
				{
					currentList.setEntries((new FileWriter()).writeM3U(currentList, currentList.getFile()));
				}
				else if (currentList.getType() == PlaylistType.PLS)
				{
					currentList.setEntries((new FileWriter()).writePLS(currentList, currentList.getFile()));
				}
			}
		}
	}

	public void savePlaylist(File destination) throws Exception
	{
		PlaylistType destType = destination.getName().toLowerCase().contains(".m3u") ? PlaylistType.M3U : PlaylistType.PLS;
		if (destType == PlaylistType.M3U)
		{
			if (options.getSavePlaylistsWithRelativePaths())
			{
				currentList.setEntries((new FileWriter()).writeRelativeM3U(currentList, destination));
			}
			else
			{
				currentList.setEntries((new FileWriter()).writeM3U(currentList, destination));
			}
		}
		else if (destType == PlaylistType.PLS)
		{
			if (options.getSavePlaylistsWithRelativePaths())
			{
				currentList.setEntries((new FileWriter()).writeRelativePLS(currentList, destination));
			}
			else
			{
				currentList.setEntries((new FileWriter()).writePLS(currentList, destination));
			}
		}
		currentList.setFile(destination);
		currentList.setType(destType);
		history.add(destination.getCanonicalPath());
		(new FileWriter()).writeMruPlaylists(history);
	}

	public Object[][] insertFile(File fileToInsert, int entryIndex)
	{
		PlaylistEntry entryToInsert = new PlaylistEntry(fileToInsert, "");
		currentList.getEntries().insertElementAt(entryToInsert, entryIndex + 1);
		return guiTableUpdate();
	}

	public Object[][] replaceFile(File fileToInsert, int entryIndex)
	{
		PlaylistEntry entryToInsert = new PlaylistEntry(fileToInsert, "");
		currentList.getEntries().remove(entryIndex);
		currentList.getEntries().insertElementAt(entryToInsert, entryIndex);
		return guiTableUpdate();
	}

	public void switchMediaLibraryToUNCPaths()
	{
		if (mediaDir != null)
		{
			for (int i = 0; i < mediaDir.length; i++)
			{
				UNCFile file = new UNCFile(mediaDir[i]);
				if (file.onNetworkDrive())
				{
					mediaDir[i] = file.getUNCPath();
				}
			}
		}

		if (mediaLibraryDirectoryList != null)
		{
			for (int i = 0; i < mediaLibraryDirectoryList.length; i++)
			{
				UNCFile file = new UNCFile(mediaLibraryDirectoryList[i]);
				if (file.onNetworkDrive())
				{
					mediaLibraryDirectoryList[i] = file.getUNCPath();
				}
			}
		}

		if (mediaLibraryFileList != null)
		{
			for (int i = 0; i < mediaLibraryFileList.length; i++)
			{
				UNCFile file = new UNCFile(mediaLibraryFileList[i]);
				if (file.onNetworkDrive())
				{
					mediaLibraryFileList[i] = file.getUNCPath();
				}

			}
		}
	}
}
