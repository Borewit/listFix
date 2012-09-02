/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2012 Jeremy Caron
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
import java.util.regex.Pattern;
import listfix.comparators.MatchedPlaylistEntryComparator;
import listfix.controller.GUIDriver;

import listfix.io.Constants;
import listfix.util.*;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;
import org.apache.log4j.Logger;

public class PlaylistEntry implements Cloneable
{
	// logger
	private static final Logger _logger = Logger.getLogger(PlaylistEntry.class);
	// The list of folders we know don't exist.
	public static List<String> NonExistentDirectories = new ArrayList<String>();
	// A list of folders we know DO exist.
	public static List<String> ExistingDirectories = new ArrayList<String>();
	// The root folder all the entries in a relative playlist are relative to.
	public String BasePath = "";
	// This entry's _path.
	private String _path = ".";
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
	// The file this entry belongs to.
	private File _playlist;
	// WPL
	private String _cid = "";
	private String _tid = "";
	
	private static final Pattern APOS_PATTERN = Pattern.compile("\'");
	private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("(([a-z])([A-Z]))");

	/**
	 * @return the _status
	 */
	public PlaylistEntryStatus getStatus()
	{
		return _status;
	}

	// Construct an M3U URL entry.
	public PlaylistEntry(URI uri, String extra)
	{
		_thisURI = uri;
		_extInf = extra;
		parseExtraInfo(extra);
	}

	// Construct a WPL URL entry
	public PlaylistEntry(URI uri, String extra, String cid, String tid)
	{
		this(uri, extra);
		_cid = cid;
		_tid = tid;		
	}

	// Construct a PLS URL entry.
	public PlaylistEntry(URI uri, String t, String l)
	{
		_thisURI = uri;
		_title = t;
		_length = l;
		_extInf = "#EXTINF:" + l + "," + t;
	}

	// Construct an M3U path & file-name based entry.
	public PlaylistEntry(String p, String f, String extra, File list)
	{
		_path = p;
		_fileName = f;
		_extInf = extra;
		parseExtraInfo(extra);
		_thisFile = new File(_path, _fileName);
		_playlist = list;
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
				_absoluteFile = new File(list.getParentFile(), p + Constants.FS + f);
				if (_absoluteFile.exists())
				{
					_status = PlaylistEntryStatus.Found;
					// _path = parentPath.getPath();
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

	// Another WPL constructor
	public PlaylistEntry(String p, String f, String extra, File list, String cid, String tid)
	{
		this(p, f, extra, list);
		_cid = cid;
		_tid = tid;
	}
	
	// Construct a M3U file-based entry, used during save among other things
	public PlaylistEntry(File input, String extra, File list)
	{
		_fileName = input.getName();
		_path = input.getPath().substring(0, input.getPath().indexOf(_fileName));
		_thisFile = input;
		// this parsing is insufficient when going from windows to linux... special handling is necessary.
		if (!OperatingSystem.isWindows() && _path.isEmpty() && input.getName().indexOf(Constants.FS) < 0)
		{
			_fileName = input.getName().substring(input.getName().lastIndexOf("\\") + 1);
			_path = input.getPath().substring(0, input.getPath().indexOf(_fileName));
			_thisFile = new File(_path, _fileName);
		}
		_extInf = extra;
		parseExtraInfo(extra);
		_playlist = list;
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
				_absoluteFile = new File(list.getParentFile(), _path + _fileName);
				if (_absoluteFile.exists())
				{
					_status = PlaylistEntryStatus.Found;
					// _path = parentPath.getPath();
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

	// WPL constructor to hang on to random WPL-specific bits
	public PlaylistEntry(File input, String extra, File list, String cid, String tid)
	{
		this(input, extra, list);
		_cid = cid;
		_tid = tid;
	}
	
	// PLS constructor, which has file references, a title, and a length
	public PlaylistEntry(File input, String t, String l, File list)
	{
		_fileName = input.getName();
		_path = input.getPath().substring(0, input.getPath().indexOf(_fileName));
		_thisFile = input;
		// this parsing is insufficient when going from windows to linux... special handling is necessary.
		if (!OperatingSystem.isWindows() && _path.isEmpty() && input.getName().indexOf(Constants.FS) < 0)
		{
			_fileName = input.getName().substring(input.getName().lastIndexOf("\\") + 1);
			_path = input.getPath().substring(0, input.getPath().indexOf(_fileName));
			_thisFile = new File(_path, _fileName);
		}
		_extInf = "#EXTINF:" + l + "," + t;
		_title = t;
		_length = l;
		_playlist = list;
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
				_absoluteFile = new File(list.getParentFile(), _path + _fileName);
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

	// Copy constructor...
	public PlaylistEntry(PlaylistEntry entry)
	{
		_fileName = entry._fileName;
		_path = entry._path;
		_extInf = entry._extInf;
		_title = entry._title;
		_length = entry._length;
		_thisFile = entry._thisFile;
		_playlist = entry._playlist;
		_status = entry._status;
		_absoluteFile = entry._absoluteFile;
		_thisURI = entry._thisURI;
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
	
	public String getCID()
	{
		return _cid;
	}

	public String getTID()
	{
		return _tid;
	}
	
	// check the file system for existence if we don't already know the file exists.
	private boolean exists()
	{
		return isFound() || _thisFile.exists();
	}

	private void recheckFoundStatus()
	{
		if (_thisFile.isFile() && _thisFile.exists())
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
		return isFound() || this.isURL() || ArrayFunctions.ContainsStringPrefixingAnotherString(emptyPaths, _path, false);
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
		if (this.isFound() || this.isURL())
		{
			List<PlaylistEntry> tempList = new ArrayList<PlaylistEntry>();
			tempList.add(this);
			(new Playlist(tempList)).play();
		}
	}

	public String toM3UString()
	{
		StringBuilder result = new StringBuilder();
		if (!(this.getExtInf() == null) && !(this.getExtInf().equals("")))
		{
			result.append(this.getExtInf());
			result.append(Constants.BR);
		}
		if (!this.isURL())
		{
			if (!this.isRelative())
			{
				if (this.getPath().endsWith(Constants.FS))
				{
					result.append(this.getPath());
					result.append(this.getFileName());
				}
				else
				{
					result.append(this.getPath());
					result.append(Constants.FS);
					result.append(this.getFileName());
				}
			}
			else
			{
				String tempPath = _thisFile.getPath();
				if (tempPath.substring(0, tempPath.indexOf(_fileName)).equals(Constants.FS))
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
				if (this.getPath().endsWith(Constants.FS))
				{
					result.append(this.getPath());
					result.append(this.getFileName());
				}
				else
				{
					result.append(this.getPath());
					result.append(Constants.FS);
					result.append(this.getFileName());
				}
			}
			else
			{
				String tempPath = _thisFile.getPath();
				if (tempPath.substring(0, tempPath.indexOf(_fileName)).equals(Constants.FS))
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
		result.append(Constants.BR);

		// set the _title
		result.append("Title").append(index).append("=").append(_title).append(Constants.BR);

		// set the _length
		result.append("Length").append(index).append("=").append(_length).append(Constants.BR);

		return result.toString();
	}

	public String toWPLString()
	{
		StringBuilder result = new StringBuilder();
		if (!this.isURL())
		{
			if (!this.isRelative())
			{
				if (this.getPath().endsWith(Constants.FS))
				{
					result.append(this.getPath());
					result.append(this.getFileName());
				}
				else
				{
					result.append(this.getPath());
					result.append(Constants.FS);
					result.append(this.getFileName());
				}
			}
			else
			{
				String tempPath = _thisFile.getPath();
				if (tempPath.substring(0, tempPath.indexOf(_fileName)).equals(Constants.FS))
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
	
	public boolean findNewLocationFromFileList(String[] fileList)
	{
		int searchResult = -1;
		String trimmedFileName = Constants.FILE_SYSTEM_IS_CASE_SENSITIVE ? _fileName.trim() : _fileName.trim().toLowerCase();		
		String candidateFileName = "";
		int lastFileSeparatorIndex;
		for (int i = 0; i < fileList.length; i++)
		{			
			// JCaron - 2012.04.22
			// Get the filename from the media library entry that we're looking at so we can compare it directly to the filename we're searching for.
			// Need the last index of the file separator character on the current system to accomplish this.
			// A direct comparison is not only faster, but prevents considering "01 - test.mp3" an exact match for "1 - test.mp3" like the logic in the else block below did previously.
			lastFileSeparatorIndex = fileList[i].lastIndexOf(Constants.FS);
			if (lastFileSeparatorIndex >= 0)
			{
				candidateFileName = fileList[i].substring(lastFileSeparatorIndex + 1);
				if (Constants.FILE_SYSTEM_IS_CASE_SENSITIVE ? candidateFileName.equals(trimmedFileName) : candidateFileName.toLowerCase().equals(trimmedFileName))
				{
					searchResult = i;
					break;
				}
			}
			else
			{
				// JCaron - 2012.04.22
				// In theory, this code should be unreachable.  The list of files passed in should always be absolute, 
				// which would mean including at least one OS-specific file separator character somewhere in the string.  But, the logic below
				// has served us well for years, so it's a reasonable fallback should this case somehow arise.
				candidateFileName = fileList[i];
				if (Constants.FILE_SYSTEM_IS_CASE_SENSITIVE ? candidateFileName.endsWith(trimmedFileName) : candidateFileName.toLowerCase().endsWith(trimmedFileName))
				{
					searchResult = i;
					break;
				}
			}
		}
		if (searchResult >= 0)
		{
			this.setFile(new File(fileList[searchResult]));
			_status = PlaylistEntryStatus.Found;
			_isFixed = true;
			return true;
		}
		return false;
	}

	public List<MatchedPlaylistEntry> findClosestMatches(String[] mediaFiles, IProgressObserver observer)
	{
		List<MatchedPlaylistEntry> matches = new ArrayList<MatchedPlaylistEntry>();
		ProgressAdapter progress = ProgressAdapter.wrap(observer);
		progress.setTotal(mediaFiles.length);

		matches.clear();
		
		// Remove apostrophes and addAt spaces between lowercase and capital letters so we can tokenize by camel case.
		String entryName = CAMEL_CASE_PATTERN.matcher(APOS_PATTERN.matcher(getFileName()).replaceAll("")).replaceAll("$2 $3").toLowerCase();
		File mediaFile = null;
		int score = 0;
		for (String mediaFilePath : mediaFiles)
		{
			if (observer == null || !observer.getCancelled())
			{
				progress.stepCompleted();

				mediaFile = new File(mediaFilePath);
				
				// Remove apostrophes and addAt spaces between lowercase and capital letters so we can tokenize by camel case.
				score = FileNameTokenizer.score(entryName, CAMEL_CASE_PATTERN.matcher(APOS_PATTERN.matcher(mediaFile.getName()).replaceAll("")).replaceAll("$2 $3").toLowerCase());
				if (score > 0)
				{
					// Only keep the top X highest-rated matches (default is 20), anything more than that has a good chance of using too much memory
					// on systems w/ huge media libraries, too little RAM, or when fixing excessively large playlists (the things you have to worry
					// about when people run your software on ancient PCs in Africa =])
					if (matches.size() < GUIDriver.getInstance().getAppOptions().getMaxClosestResults())
					{
						matches.add(new MatchedPlaylistEntry(mediaFile, score, _playlist));
					}
					else
					{
						if (matches.get(GUIDriver.getInstance().getAppOptions().getMaxClosestResults() - 1).getScore() < score)
						{
							matches.set(GUIDriver.getInstance().getAppOptions().getMaxClosestResults() - 1, new MatchedPlaylistEntry(mediaFile, score, _playlist));
						}
					}
					Collections.sort(matches, new MatchedPlaylistEntryComparator());
				}
			}
			else
			{
				return null;
			}
		}
		return matches;
	}

	@Override
	public Object clone()
	{
		PlaylistEntry result = null;
		if (!this.isURL())
		{
			result = new PlaylistEntry(this);
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
				_logger.warn(ExStack.toString(e));
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

	public boolean updatePathToMediaLibraryIfFoundOutside()
	{
		if (_status == PlaylistEntryStatus.Found
			&& !ArrayFunctions.ContainsStringPrefixingAnotherString(GUIDriver.getInstance().getMediaDirs(), _path, !GUIDriver.fileSystemIsCaseSensitive))
		{
			return findNewLocationFromFileList(GUIDriver.getInstance().getMediaLibraryFileList());
		}
		return false;
	}

	public void setPlaylist(File list)
	{
		_playlist = list;
	}
}
