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

import listfix.model.enums.PlaylistEntryStatus;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import listfix.comparators.MatchedPlaylistEntryComparator;

import listfix.io.BrowserLauncher;
import listfix.io.FileLauncher;
import listfix.util.*;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;

public class PlaylistEntry implements Cloneable
{
	// file separator
	private final static String FILE_SEPARATOR = System.getProperty("file.separator");
	// line separator
	private final static String BR = System.getProperty("line.separator");
	// is the file system case sensitive?
	private static final boolean FILE_SYSTEM_IS_CASE_SENSITIVE = File.separatorChar == '/';
	// The list of folders we know don't exist.
	public static List<String> NonExistentDirectories = new ArrayList<String>();
	// A list of folders we know DO exist.
	public static List<String> ExistingDirectories = new ArrayList<String>();
	// The root folder all the entries in a relative playlist are relative to.
	public static String BasePath = "";
	// This entry's _path.
	private String _path = "";
	// This entry's extra info.
	private String _extInf = "";
	// This entry's file name.
	private String _fileName = "";
	// This entry's File object.
	private File _thisFile = null;
	// This entry's absolute file.
	private File _absoluteFile = null;
	// The entry's URI (for URLs).
	private URI _thisURI = null;
	// The _title of the entry
	private String _title = "";
	// The _length of the track
	private String _length = "-1";
	// Status of this item.
	private PlaylistEntryStatus _status = PlaylistEntryStatus.Unknown;
	// Has this item been fixed?
	private boolean _isFixed;

	/**
	 * @return the _status
	 */
	public PlaylistEntryStatus getStatus()
	{
		return _status;
	}

	// Construct a URL entry.
	public PlaylistEntry(URI uri, String extra)
	{
		_thisURI = uri;
		_extInf = extra;
		parseExtraInfo(extra);
	}

	// Construct a URL entry.
	public PlaylistEntry(URI uri, String t, String l)
	{
		_thisURI = uri;
		_title = t;
		_length = l;
		_extInf = "#EXTINF:" + l + "," + t;
	}

	// Construct a file-based entry.
	public PlaylistEntry(String p, String f, String extra, File parentPath)
	{
		_path = p;
		_fileName = f;
		_extInf = extra;
		parseExtraInfo(extra);
		_thisFile = new File(_path, _fileName);

		// should we skip the exists check?
		if (skipExistsCheck())
		{
			_status = PlaylistEntryStatus.Missing;
		}
		// if we should check, do so...
		else if (this.exists())
		{
			// file was found in its current location
			_status = PlaylistEntryStatus.Found;
			if (_thisFile.isAbsolute())
			{
				_absoluteFile = _thisFile;
			}
			else
			{
				_absoluteFile = new File(_thisFile.getAbsolutePath());
			}
		}
		else
		{
			// file was not found
			if (!_thisFile.isAbsolute())
			{
				// if the test file was relative, try making it absolute.
				_absoluteFile = new File(parentPath, p + FILE_SEPARATOR + f);
				if (_absoluteFile.exists())
				{
					_status = PlaylistEntryStatus.Found;
				}
				else
				{
					_status = PlaylistEntryStatus.Missing;
				}
			}
			else
			{
				_status = PlaylistEntryStatus.Missing;
			}
		}
	}

	// Same as above but with a file object as input
	public PlaylistEntry(File input, String extra)
	{
		_fileName = input.getName();
		_path = input.getPath().substring(0, input.getPath().indexOf(_fileName));
		_extInf = extra;
		parseExtraInfo(extra);
		_thisFile = input;
		if (skipExistsCheck())
		{
			_status = PlaylistEntryStatus.Missing;
		}
		else if (this.exists())
		{
			_status = PlaylistEntryStatus.Found;
			if (_thisFile.isAbsolute())
			{
				_absoluteFile = _thisFile;
			}
			else
			{
				_absoluteFile = new File(_thisFile.getAbsolutePath());
			}
		}
		else
		{
			if (!_thisFile.isAbsolute())
			{
				_absoluteFile = new File(BasePath, input.getPath());
				if (_absoluteFile.exists())
				{
					_status = PlaylistEntryStatus.Found;
				}
				else
				{
					_status = PlaylistEntryStatus.Missing;
				}
			}
			else
			{
				_status = PlaylistEntryStatus.Missing;
			}
		}
	}

	// Same as above but with a file object as input
	public PlaylistEntry(File input, String t, String l)
	{
		_fileName = input.getName();
		_path = input.getPath().substring(0, input.getPath().indexOf(_fileName));
		_extInf = "#EXTINF:" + l + "," + t;
		_title = t;
		_length = l;
		_thisFile = input;
		if (skipExistsCheck())
		{
			_status = PlaylistEntryStatus.Missing;
		}
		else if (this.exists())
		{
			_status = PlaylistEntryStatus.Found;
			if (_thisFile.isAbsolute())
			{
				_absoluteFile = _thisFile;
			}
			else
			{
				_absoluteFile = new File(_thisFile.getAbsolutePath());
			}
		}
		else
		{
			if (!_thisFile.isAbsolute())
			{
				_absoluteFile = new File(BasePath, input.getPath());
				if (_absoluteFile.exists())
				{
					_status = PlaylistEntryStatus.Found;
				}
				else
				{
					_status = PlaylistEntryStatus.Missing;
				}
			}
			else
			{
				_status = PlaylistEntryStatus.Missing;
			}
		}
	}

	public void markFixedIfFound()
	{
		if (getStatus() == PlaylistEntryStatus.Found)
		{
			_isFixed = true;
		}
	}

	public String getPath()
	{
		return this.isURL() ? "" : _path;
	}

	public File getFile()
	{
		return _thisFile;
	}

	public String getFileName()
	{
		return this.isURL() ? _thisURI.toString() : _fileName;
	}

	public String getExtInf()
	{
		return _extInf;
	}

	// check the file system for existence if we don't already know the file exists.
	private boolean exists()
	{
		return isFound() || _thisFile.exists();
	}

	private void recheckFoundStatus()
	{
		if (_thisFile.exists() && _thisFile.isFile())
		{
			_status = PlaylistEntryStatus.Found;
		}
		else
		{
			_status = PlaylistEntryStatus.Missing;
			_isFixed = false;
		}
	}

	public URI getURI()
	{
		return _thisURI;
	}

	public File getAbsoluteFile()
	{
		return _absoluteFile;
	}

	public void setPath(String input)
	{
		_path = input;
		_thisFile = new File(_path, _fileName);
		resetAbsoluteFile();
		recheckFoundStatus();
	}

	public void setFileName(String input)
	{
		_fileName = input;
		_thisFile = new File(_path, _fileName);
		resetAbsoluteFile();
		recheckFoundStatus();
	}

	private void setFile(File input)
	{
		_thisFile = input;
		_fileName = input.getName();
		_path = input.getPath().substring(0, input.getPath().indexOf(_fileName));
		resetAbsoluteFile();
		recheckFoundStatus();
	}

	private void resetAbsoluteFile()
	{
		if (_thisFile.isAbsolute())
		{
			_absoluteFile = _thisFile;
		}
		else
		{
			_absoluteFile = new File(_thisFile.getAbsolutePath());
		}
	}

	private boolean skipExistsCheck()
	{
		String[] emptyPaths = new String[NonExistentDirectories.size()];
		NonExistentDirectories.toArray(emptyPaths);
		return isFound() || this.isURL() || ArrayFunctions.ContainsStringWithPrefix(emptyPaths, _path, false);
	}

	public boolean isFound()
	{
		return getStatus() == PlaylistEntryStatus.Found;
	}

	public boolean isFixed()
	{
		return _isFixed;
	}

	public void setFixed(boolean fixed)
	{
		_isFixed = fixed;
	}

	public boolean isURL()
	{
		return _thisURI != null && _thisFile == null;
	}

	public boolean isRelative()
	{
		return !this.isURL() && _thisFile != null && !_thisFile.isAbsolute();
	}

	// Try to open the file with the "default" MP3 player (only works on some systems).
	public void play() throws IOException, InterruptedException
	{
		File absFile = null;
		if (this.isURL())
		{
			BrowserLauncher.launch(this._thisURI.toString());
		}
		else
		{
			if (this.isFound())
			{
				if (this.isRelative())
				{
					absFile = this._absoluteFile.getCanonicalFile();
				}
				else
				{
					absFile = this._thisFile.getCanonicalFile();
				}
				FileLauncher.launch(absFile);
			}
		}
	}

	public String toM3UString()
	{
		StringBuilder result = new StringBuilder();
		if (!(this.getExtInf() == null) && !(this.getExtInf().equals("")))
		{
			result.append(this.getExtInf());
			result.append(BR);
		}
		if (!this.isURL())
		{
			if (!this.isRelative())
			{
				if (this.getPath().endsWith(FILE_SEPARATOR))
				{
					result.append(this.getPath());
					result.append(this.getFileName());
				}
				else
				{
					result.append(this.getPath());
					result.append(FILE_SEPARATOR);
					result.append(this.getFileName());
				}
			}
			else
			{
				String tempPath = _thisFile.getPath();
				if (tempPath.substring(0, tempPath.indexOf(_fileName)).equals(FILE_SEPARATOR))
				{
					result.append(_fileName);
				}
				else
				{
					result.append(_thisFile.getPath());
				}
			}
		}
		else
		{
			result.append(_thisURI.toString());
		}
		return result.toString();
	}

	public String toPLSString(int index)
	{
		StringBuilder result = new StringBuilder();

		// set the file
		if (!this.isURL())
		{
			result.append("File").append(index).append("=");
			if (!this.isRelative())
			{
				if (this.getPath().endsWith(FILE_SEPARATOR))
				{
					result.append(this.getPath());
					result.append(this.getFileName());
				}
				else
				{
					result.append(this.getPath());
					result.append(FILE_SEPARATOR);
					result.append(this.getFileName());
				}
			}
			else
			{
				String tempPath = _thisFile.getPath();
				if (tempPath.substring(0, tempPath.indexOf(_fileName)).equals(FILE_SEPARATOR))
				{
					result.append(_fileName);
				}
				else
				{
					result.append(_thisFile.getPath());
				}
			}
		}
		else
		{
			result.append("File").append(index).append("=").append(_thisURI.toString());
		}
		result.append(BR);

		// set the _title
		result.append("Title").append(index).append("=").append(_title).append(BR);

		// set the _length
		result.append("Length").append(index).append("=").append(_length).append(BR);

		return result.toString();
	}

	public void findNewLocationFromFileList(String[] fileList)
	{
		int searchResult = -1;
		String trimmedFileName = FILE_SYSTEM_IS_CASE_SENSITIVE ? _fileName.trim() : _fileName.trim().toLowerCase();
		for (int i = 0; i < fileList.length; i++)
		{
			if (FILE_SYSTEM_IS_CASE_SENSITIVE ? fileList[i].endsWith(trimmedFileName) : fileList[i].toLowerCase().endsWith(trimmedFileName))
			{
				searchResult = i;
				break;
			}
		}
		if (searchResult >= 0)
		{
			File foundFile = new File(fileList[searchResult]);
			this.setFile(foundFile);
			_status = PlaylistEntryStatus.Found;
			_isFixed = true;
			return;
		}
		_status = PlaylistEntryStatus.Missing;
	}

	public List<MatchedPlaylistEntry> findClosestMatches(String[] mediaFiles, IProgressObserver observer)
	{
		ProgressAdapter progress = ProgressAdapter.wrap(observer);
		progress.setTotal(mediaFiles.length);

		List<MatchedPlaylistEntry> matches = new ArrayList<MatchedPlaylistEntry>();
		String entryName = getFileName().replaceAll("\'", "");
		for (String mediaFilePath : mediaFiles)
		{
			progress.stepCompleted();

			File mediaFile = new File(mediaFilePath);
			int score = FileNameTokenizer.score(entryName, mediaFile.getName().replaceAll("\'", ""));
			if (score > 0)
			{
				matches.add(new MatchedPlaylistEntry(mediaFile, score));
			}
		}
		Collections.sort(matches, new MatchedPlaylistEntryComparator());
		return matches;
	}

	@Override
	public Object clone()
	{
		PlaylistEntry result = null;
		if (!this.isURL())
		{
			result = new PlaylistEntry(new File(this.getFile().getPath()), this.getTitle(), this.getLength());
		}
		else
		{
			try
			{
				result = new PlaylistEntry(new URI(this.getURI().toString()), this.getTitle(), this.getLength());
			}
			catch (Exception e)
			{
				//eat the error for now.
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * @return the _title
	 */
	public String getTitle()
	{
		return _title;
	}

	/**
	 * @return the _length
	 */
	public String getLength()
	{
		return _length;
	}

	private void parseExtraInfo(String extra)
	{
		if (extra != null && extra.length() > 0)
		{
			extra = extra.replaceFirst("#EXTINF:", "");
			if (extra.contains(","))
			{
				String[] split = extra.split(",");
				if (split != null && split.length > 1)
				{
					_length = split[0];
					_title = split[1];
				}
				else if (split != null && split.length == 1)
				{
					// assume it's a _title?
					_title = split[0];
				}
			}
			else
			{
				_title = extra;
			}
		}
	}
}
