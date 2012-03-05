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

package listfix.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.List;

import listfix.model.AppOptions;

/*
============================================================================
= Author:   Jeremy Caron
= File:     ProcessIniFile.java
= Purpose:  Read in the dirLists.ini file and return
=           a String array containing the directories listed in the file.
============================================================================
 */
public class IniFileReader
{
	private BufferedReader B1;
	private BufferedReader B2;
	private String fname1;
	private String fname2;
	private String[] mediaDirs = new String[0];
	private String[] history = new String[0];
	private String[] mediaLibrary = new String[0];
	private String[] mediaLibraryFiles = new String[0];
	private AppOptions options;

	public IniFileReader(AppOptions opts) throws FileNotFoundException, UnsupportedEncodingException
	{
		options = opts;
		fname1 = Constants.MEDIA_LIBRARY_INI;
		fname2 = Constants.HISTORY_INI;
		
		File in_data1 = new File(fname1);
		if (!in_data1.exists())
		{
			throw new FileNotFoundException(in_data1.getPath());
		}
		else if (in_data1.length() == 0)
		{
			throw new FileNotFoundException("File found, but was of zero size.");
		}

		// converting these files to UTF-8, need to handle the old format for the conversion...		
		B1 = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(in_data1), "UTF-8"), "UTF8"));	

		File in_data2 = new File(fname2);
		if (!in_data2.exists())
		{
			throw new FileNotFoundException(in_data2.getPath());
		}
		else if (in_data2.length() == 0)
		{
			throw new FileNotFoundException("File found, but was of zero size.");
		}
		
		B2 = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(in_data2), "UTF-8"), "UTF8"));
	
	}

	public void readIni() throws Exception
	{
		List<String> tempList = new ArrayList<String>();
		// Read in base media directories
		// skip first line, contains header
		String line = B1.readLine();
		line = B1.readLine();
		while ((line != null) && (!line.startsWith("[")))
		{
			tempList.add(line);
			line = B1.readLine();
		}
		mediaDirs = new String[tempList.size()];
		tempList.toArray(mediaDirs);

		tempList.clear();

		// Read in media library directories
		// skip first line, contains header
		line = B1.readLine();
		while ((line != null) && (!line.startsWith("[")))
		{
			tempList.add(line);
			line = B1.readLine();
		}
		mediaLibrary = new String[tempList.size()];
		tempList.toArray(mediaLibrary);
		tempList.clear();

		// Read in media library files
		// skip first line, contains header
		line = B1.readLine();
		while (line != null)
		{
			tempList.add(line);
			line = B1.readLine();
		}
		mediaLibraryFiles = new String[tempList.size()];
		tempList.toArray(mediaLibraryFiles);
		tempList.clear();

		// Read in history...
		line = B2.readLine();
		line = B2.readLine();
		while (line != null)
		{
			tempList.add(line);
			line = B2.readLine();
		}
		history = new String[tempList.size()];
		tempList.toArray(history);
		tempList.clear();
	}

	public void closeFile() throws IOException
	{
		B1.close();
		B2.close();
	}

	public String[] getHistory()
	{
		return history;
	}

	public String[] getMediaDirs()
	{
		if (options.getAlwaysUseUNCPaths())
		{
			String[] result = new String[mediaDirs.length];
			for (int i = 0; i < result.length; i++)
			{
				UNCFile file = new UNCFile(mediaDirs[i]);
				if (file.onNetworkDrive())
				{
					result[i] = file.getUNCPath();
				}
				else
				{
					result[i] = mediaDirs[i];
				}
			}
			return result;
		}
		else
		{
			return mediaDirs;
		}
	}

	public String[] getMediaLibrary()
	{
		if (options.getAlwaysUseUNCPaths())
		{
			String[] result = new String[mediaLibrary.length];
			for (int i = 0; i < result.length; i++)
			{
				UNCFile file = new UNCFile(mediaLibrary[i]);
				if (file.onNetworkDrive())
				{
					result[i] = file.getUNCPath();
				}
				else
				{
					result[i] = mediaLibrary[i];
				}
			}
			return result;
		}
		else
		{
			return mediaLibrary;
		}
	}

	public String[] getMediaLibraryFiles()
	{
		if (options.getAlwaysUseUNCPaths())
		{
			String[] result = new String[mediaLibraryFiles.length];
			for (int i = 0; i < result.length; i++)
			{
				UNCFile file = new UNCFile(mediaLibraryFiles[i]);
				if (file.onNetworkDrive())
				{
					result[i] = file.getUNCPath();
				}
				else
				{
					result[i] = mediaLibraryFiles[i];
				}
			}
			return result;
		}
		else
		{
			return mediaLibraryFiles;
		}
	}
}
