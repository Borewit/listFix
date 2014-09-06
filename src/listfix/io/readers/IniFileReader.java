/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2014 Jeremy Caron
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

package listfix.io.readers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import listfix.io.Constants;
import listfix.io.UNCFile;
import listfix.io.UnicodeInputStream;
import listfix.model.AppOptions;

/**
 * Read in the media library and history files, providing methods for retrieving the data.
 * @author jcaron
 */
public class IniFileReader
{
	private String fname1;
	private String fname2;
	private String[] mediaDirs = new String[0];
	private String[] history = new String[0];
	private String[] mediaLibrary = new String[0];
	private String[] mediaLibraryFiles = new String[0];
	private AppOptions options;

	/**
	 * Constructs the file reader.
	 * @param opts An AppOptions configuration, needed to decide if the library should use UNC paths or not.
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
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

		File in_data2 = new File(fname2);
		if (!in_data2.exists())
		{
			throw new FileNotFoundException(in_data2.getPath());
		}
		else if (in_data2.length() == 0)
		{
			throw new FileNotFoundException("File found, but was of zero size.");
		}
	}

	/**
	 * Read the media directory and history files into memory.
	 * @throws Exception
	 */
	public void readIni() throws Exception
	{
		try (BufferedReader B1 = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(new File(fname1)), "UTF-8"), "UTF8")); 
			 BufferedReader B2 = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(new File(fname2)), "UTF-8"), "UTF8")))
		{
			List<String> tempList = new ArrayList<>();
			String line;
			B1.readLine();
			line = B1.readLine();
			while ((line != null) && (!line.startsWith("[")))
			{
				tempList.add(line);
				line = B1.readLine();
			}
			mediaDirs = new String[tempList.size()];
			tempList.toArray(mediaDirs);
			tempList.clear();
			line = B1.readLine();
			while ((line != null) && (!line.startsWith("[")))
			{
				tempList.add(line);
				line = B1.readLine();
			}
			mediaLibrary = new String[tempList.size()];
			tempList.toArray(mediaLibrary);
			tempList.clear();
			line = B1.readLine();
			while (line != null)
			{
				tempList.add(line);
				line = B1.readLine();
			}
			mediaLibraryFiles = new String[tempList.size()];
			tempList.toArray(mediaLibraryFiles);
			tempList.clear();
			B2.readLine();
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
	}

	/**
	 * Get a list of paths to playlists that have been opened recently.
	 * @return
	 */
	public String[] getHistory()
	{
		return history;
	}

	/**
	 * Get the root media directories the user has specified.
	 * @return
	 */
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

	/**
	 * Get the cached list of all directories in the media library.
	 * @return
	 */
	public String[] getMediaLibraryDirectories()
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

	/**
	 * Get the cached list of all files in the media library.
	 * @return
	 */
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
