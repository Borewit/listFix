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
import java.net.URI;
import java.util.*;

import listfix.io.BrowserLauncher;
import listfix.io.FileLauncher;
import listfix.util.*;

public class PlaylistEntry implements Cloneable
{
	// file separator
	private final static String fs = System.getProperty("file.separator");
	// line separator
	private final static String br = System.getProperty("line.separator");
	// is the file system case sensitive?
	private static final boolean fileSystemIsCaseSensitive = File.separatorChar == '/';
	// The list of folders we know don't exist.
	public static Vector<String> nonExistentDirectories = new Vector<String>();
	// A list of folders we know DO exist.
	public static Vector<String> existingDirectories = new Vector<String>();
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
	// This entry's "lost/found" status as should be displyed in the UI.
	private String message = "Unknown";
	// This entry's lost/found flag.
	private boolean found = false;
	// The title of the entry
	private String title = "";
	// The length of the track
	private String length = "-1";

	// Construct a URL entry.
	public PlaylistEntry(URI uri, String extra)
	{
		thisURI = uri;
		extInf = extra;
		extra = extra.replaceFirst("#EXTINF:", "");
		if (extra != null && extra.length() > 0)
		{
			if (extra.contains(","))
			{
				String[] split = extra.split(",");
				length = split[0];
				title = split[1];
			}
			else
			{
				title = extra;
			}
		}
		message = "URL";
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
	public PlaylistEntry(String p, String f, String extra)
	{
		path = p;
		fileName = f;
		extInf = extra;
		extra = extra.replaceFirst("#EXTINF:", "");
		if (extra != null && extra.length() > 0)
		{
			if (extra.contains(","))
			{
				String[] split = extra.split(",");
				length = split[0];
				title = split[1];
			}
			else
			{
				title = extra;
			}
		}
		thisFile = new File(path, fileName);

		// should we skip the exists check?
		if (skipExistsCheck())
		{
			message = "Not Found";
		}
		// if we should check, do so...
		else if (this.exists())
		{
			// file was found in its current location
			message = "Found!";
			found = true;
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
				absoluteFile = new File(basePath, p + fs + f);
				if (absoluteFile.exists())
				{
					message = "Found!";
					found = true;
				}
				else
				{
					message = "Not Found";
				}
			}
			else
			{
				message = "Not Found";
			}
		}
	}

	// Same as above but with a file object as input
	public PlaylistEntry(File input, String extra)
	{
		fileName = input.getName();
		path = input.getPath().substring(0, input.getPath().indexOf(fileName));
		extInf = extra;
		extra = extra.replaceFirst("#EXTINF:", "");
		if (extra != null && extra.length() > 0)
		{
			if (extra.contains(","))
			{
				String[] split = extra.split(",");
				length = split[0];
				title = split[1];
			}
			else
			{
				title = extra;
			}
		}
		thisFile = input;
		if (skipExistsCheck())
		{
			message = "Not Found";
		}
		else if (this.exists())
		{
			message = "Found!";
			found = true;
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
					message = "Found!";
					found = true;
				}
				else
				{
					message = "Not Found";
				}
			}
			else
			{
				message = "Not Found";
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
			message = "Not Found";
		}
		else if (this.exists())
		{
			message = "Found!";
			found = true;
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
					message = "Found!";
					found = true;
				}
				else
				{
					message = "Not Found";
				}
			}
			else
			{
				message = "Not Found";
			}
		}
	}

	public String getMessage()
	{
		return message;
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
	public boolean exists()
	{
		return found || thisFile.exists();
	}

	public boolean recheckFoundStatus()
	{
		found = thisFile.exists();
		return found;
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
	}

	public void setFileName(String input)
	{
		fileName = input;
		thisFile = new File(path, fileName);
	}

	public void setMessage(String x)
	{
		message = x;
	}

	private void setFile(File input)
	{
		thisFile = input;
		fileName = input.getName();
		path = input.getPath().substring(0, input.getPath().indexOf(fileName));
	}

	public boolean skipExistsCheck()
	{
		String[] emptyPaths = new String[nonExistentDirectories.size()];
		nonExistentDirectories.copyInto(emptyPaths);
		return found || this.isURL() || ArrayFunctions.ContainsStringWithPrefix(emptyPaths, path, false);
	}

	public boolean isFound()
	{
		return found;
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
	public void play() throws Exception
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
			result.append("File" + index + "=");
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
			result.append("File" + index + "=" + thisURI.toString());
		}
		result.append(br);

		// set the title
		result.append("Title" + index + "=" + title + br);

		// set the length
		result.append("Length" + index + "=" + length + br);
		
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
			this.setMessage("Found!");
			found = true;
			return;
		}
		this.setMessage("Not Found");
		found = false;
	}

	@Override
	public Object clone()
	{
		PlaylistEntry result = null;
		if (!this.isURL())
		{
			result = new PlaylistEntry(new File(this.getFile().getPath()), this.getExtInf());
		}
		else
		{
			try
			{
				result = new PlaylistEntry(new URI(this.getURI().toString()), this.getExtInf());
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
}
