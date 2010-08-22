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

package listfix.model;

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
	private final static String fs = System.getProperty("file.separator");
	// line separator
	private final static String br = System.getProperty("line.separator");
	// is the file system case sensitive?
	private static final boolean fileSystemIsCaseSensitive = File.separatorChar == '/';
	// The list of folders we know don't exist.
	public static List<String> nonExistentDirectories = new ArrayList<String>();
	// A list of folders we know DO exist.
	public static List<String> existingDirectories = new ArrayList<String>();
	// The root folder all the entries in a relative playlist are relative to.
	public static String basePath = "";
	// This entry's path.
	private String path = "";
	// This entry's extra info.
	private String extInf = "";
	// This entry's file name.
	private String fileName = "";
	// This entry's File object.
	private File thisFile = null;
	// This entry's absolute file.
	private File absoluteFile = null;
	// The entry's URI (for URLs).
	private URI thisURI = null;
	// The title of the entry
	private String title = "";
	// The length of the track
	private String length = "-1";

    private enum Status
    {
        Unknown,
        Missing,
        Found
    }
    private Status _status = Status.Unknown;
    private boolean _isFixed;

	// Construct a URL entry.
	public PlaylistEntry(URI uri, String extra)
	{
		thisURI = uri;
		extInf = extra;
		parseExtraInfo(extra);
	}

	// Construct a URL entry.
	public PlaylistEntry(URI uri, String t, String l)
	{
		thisURI = uri;
		title = t;
		length = l;
		extInf = "#EXTINF:" + l + "," + t;
	}

	// Construct a file-based entry.
	public PlaylistEntry(String p, String f, String extra, File parentPath)
	{
		path = p;
		fileName = f;
		extInf = extra;
		parseExtraInfo(extra);
		thisFile = new File(path, fileName);

		// should we skip the exists check?
		if (skipExistsCheck())
		{
            _status = Status.Missing;
		}
		// if we should check, do so...
		else if (this.exists())
		{
			// file was found in its current location
            _status = Status.Found;
			if (thisFile.isAbsolute())
			{
				absoluteFile = thisFile;
			}
			else
			{
				absoluteFile = new File(thisFile.getAbsolutePath());
			}
		}
		else
		{
			// file was not found
			if (!thisFile.isAbsolute())
			{
				// if the test file was relative, try making it absolute.
				absoluteFile = new File(parentPath, p + fs + f);
				if (absoluteFile.exists())
				{
                    _status = Status.Found;
				}
				else
				{
                    _status = Status.Missing;
				}
			}
			else
			{
                _status = Status.Missing;
			}
		}
	}

	// Same as above but with a file object as input
	public PlaylistEntry(File input, String extra)
	{
		fileName = input.getName();
		path = input.getPath().substring(0, input.getPath().indexOf(fileName));
		extInf = extra;
		parseExtraInfo(extra);
		thisFile = input;
		if (skipExistsCheck())
		{
            _status = Status.Missing;
		}
		else if (this.exists())
		{
			_status = Status.Found;
			if (thisFile.isAbsolute())
			{
				absoluteFile = thisFile;
			}
			else
			{
				absoluteFile = new File(thisFile.getAbsolutePath());
			}
		}
		else
		{
			if (!thisFile.isAbsolute())
			{
				absoluteFile = new File(basePath, input.getPath());
				if (absoluteFile.exists())
				{
					_status = Status.Found;
				}
				else
				{
					_status = Status.Missing;
				}
			}
			else
			{
				_status = Status.Missing;
			}
		}
	}

	// Same as above but with a file object as input
	public PlaylistEntry(File input, String t, String l)
	{
		fileName = input.getName();
		path = input.getPath().substring(0, input.getPath().indexOf(fileName));
		extInf = "#EXTINF:" + l + "," + t;
		title = t;
		length = l;
		thisFile = input;
		if (skipExistsCheck())
		{
			_status = Status.Missing;
		}
		else if (this.exists())
		{
			_status = Status.Found;
			if (thisFile.isAbsolute())
			{
				absoluteFile = thisFile;
			}
			else
			{
				absoluteFile = new File(thisFile.getAbsolutePath());
			}
		}
		else
		{
			if (!thisFile.isAbsolute())
			{
				absoluteFile = new File(basePath, input.getPath());
				if (absoluteFile.exists())
				{
					_status = Status.Found;
				}
				else
				{
					_status = Status.Missing;
				}
			}
			else
			{
				_status = Status.Missing;
			}
		}
	}

	public void markFixedIfFound()
    {
        if (_status == Status.Found)
            _isFixed = true;
    }

	public String getPath()
	{
		return this.isURL() ? "" : path;
	}

	public File getFile()
	{
		return thisFile;
	}

	public String getFileName()
	{
		return this.isURL() ? thisURI.toString() : fileName;
	}

	public String getExtInf()
	{
		return extInf;
	}

	// check the file system for existence if we don't already know the file exists.
	private boolean exists()
	{
		return isFound() || thisFile.exists();
	}

	private void recheckFoundStatus()
	{
        if (thisFile.exists() && thisFile.isFile())
        {
            _status = Status.Found;
        }
        else
        {
            _status = Status.Missing;
            _isFixed = false;
        }
	}

	public URI getURI()
	{
		return thisURI;
	}

	public File getAbsoluteFile()
	{
		return absoluteFile;
	}

	public void setPath(String input)
	{
		path = input;
		thisFile = new File(path, fileName);
        resetAbsoluteFile();
        recheckFoundStatus();
	}

	public void setFileName(String input)
	{
		fileName = input;
		thisFile = new File(path, fileName);
	}

	private void setFile(File input)
	{
		thisFile = input;
		fileName = input.getName();
		path = input.getPath().substring(0, input.getPath().indexOf(fileName));
        resetAbsoluteFile();
	}

    private void resetAbsoluteFile()
    {
        if (thisFile.isAbsolute())
            absoluteFile = thisFile;
        else
            absoluteFile = new File(thisFile.getAbsolutePath());
    }

	private boolean skipExistsCheck()
	{
		String[] emptyPaths = new String[nonExistentDirectories.size()];
		nonExistentDirectories.toArray(emptyPaths);
		return isFound() || this.isURL() || ArrayFunctions.ContainsStringWithPrefix(emptyPaths, path, false);
	}

	public boolean isFound()
	{
		return _status == Status.Found;
	}

    public boolean isFixed()
    {
        return _isFixed;
    }

	public boolean isURL()
	{
		return thisURI != null && thisFile == null;
	}

	public boolean isRelative()
	{
		return !this.isURL() && thisFile != null && !thisFile.isAbsolute();
	}

	// Try to open the file with the "default" MP3 player (only works on some systems).
	public void play() throws IOException, InterruptedException 
	{
		File absFile = null;
		if (this.isURL())
		{
			BrowserLauncher.launch(this.thisURI.toString());
		}
		else
		{
			if (this.isFound())
			{
				if (this.isRelative())
				{
					absFile = this.absoluteFile.getCanonicalFile();
				}
				else
				{
					absFile = this.thisFile.getCanonicalFile();
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
			result.append(br);
		}
		if (!this.isURL())
		{
			if (!this.isRelative())
			{
				if (this.getPath().endsWith(fs))
				{
					result.append(this.getPath());
					result.append(this.getFileName());
				}
				else
				{
					result.append(this.getPath());
					result.append(fs);
					result.append(this.getFileName());
				}
			}
			else
			{
				String tempPath = thisFile.getPath();
				if (tempPath.substring(0, tempPath.indexOf(fileName)).equals(fs))
				{
					result.append(fileName);
				}
				else
				{
					result.append(thisFile.getPath());
				}
			}
		}
		else
		{
			result.append(thisURI.toString());
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
				if (this.getPath().endsWith(fs))
				{
					result.append(this.getPath());
					result.append(this.getFileName());
				}
				else
				{
					result.append(this.getPath());
					result.append(fs);
					result.append(this.getFileName());
				}
			}
			else
			{
				String tempPath = thisFile.getPath();
				if (tempPath.substring(0, tempPath.indexOf(fileName)).equals(fs))
				{
					result.append(fileName);
				}
				else
				{
					result.append(thisFile.getPath());
				}
			}
		}
		else
		{
			result.append("File").append(index).append("=").append(thisURI.toString());
		}
		result.append(br);

		// set the title
		result.append("Title").append(index).append("=").append(title).append(br);

		// set the length
		result.append("Length").append(index).append("=").append(length).append(br);
		
		return result.toString();
	}

	public void findNewLocationFromFileList(String[] fileList)
	{
		int searchResult = -1;
		String trimmedFileName = fileSystemIsCaseSensitive ? fileName.trim() : fileName.trim().toLowerCase();
		for (int i = 0; i < fileList.length; i++)
		{
			if (fileSystemIsCaseSensitive ? fileList[i].endsWith(trimmedFileName) : fileList[i].toLowerCase().endsWith(trimmedFileName))
			{
				searchResult = i;
				break;
			}
		}
		if (searchResult >= 0)
		{
			File foundFile = new File(fileList[searchResult]);
			this.setFile(foundFile);
            _status = Status.Found;
            _isFixed = true;
			return;
		}
        _status = Status.Missing;
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
                matches.add(new MatchedPlaylistEntry(mediaFile, score));
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
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @return the length
	 */
	public String getLength()
	{
		return length;
	}

	private void parseExtraInfo(String extra)
	{

		extra = extra.replaceFirst("#EXTINF:", "");
		if (extra != null && extra.length() > 0)
		{
			if (extra.contains(","))
			{
				String[] split = extra.split(",");
				if (split != null && split.length > 1)
				{
					length = split[0];
					title = split[1];
				}
				else if (split != null && split.length == 1)
				{
					// assume it's a title?
					title = split[0];
				}
			}
			else
			{
				title = extra;
			}
		}
	}
}
