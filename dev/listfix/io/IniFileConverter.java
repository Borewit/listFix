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

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import listfix.controller.tasks.WriteMediaLibraryIniTask;
import listfix.model.AppOptions;
import listfix.model.PlaylistHistory;
import listfix.model.enums.AppOptionsEnum;
import listfix.util.ExStack;
import listfix.util.UnicodeUtils;
import listfix.view.support.FontExtensions;
import org.apache.log4j.Logger;

/*
============================================================================
= Author:   Jeremy Caron
= File:     ProcessIniFile.java
= Purpose:  Read in the dirLists.ini file and return
=           a String array containing the directories listed in the file.
============================================================================
 */
public class IniFileConverter
{
	private BufferedReader B1;
	private BufferedReader B2;
	private static String fname1 = Constants.HOME_DIR + Constants.FS + "dirLists.ini";
	private static String fname2 = Constants.HOME_DIR + Constants.FS + "listFixHistory.ini";
	private String[] mediaDirs = new String[0];
	private String[] history = new String[0];
	private String[] mediaLibrary = new String[0];
	private String[] mediaLibraryFiles = new String[0];
	private AppOptions options = new AppOptions();

	private static final Logger _logger = Logger.getLogger(IniFileConverter.class);

	public IniFileConverter() throws FileNotFoundException, UnsupportedEncodingException
	{
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
		String encoding = UnicodeUtils.getEncoding(in_data1);
		if (encoding.equals("UTF-8"))
		{
			B1 = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(in_data1), "UTF-8"), "UTF8"));
		}
		else
		{
			B1 = new BufferedReader(new InputStreamReader(new FileInputStream(in_data1)));
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

		encoding = UnicodeUtils.getEncoding(in_data2);
		if (encoding.equals("UTF-8"))
		{
			B2 = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(in_data2), "UTF-8"), "UTF8"));
		}
		else
		{
			B2 = new BufferedReader(new InputStreamReader(new FileInputStream(in_data2)));
		}
	}
	
	public static boolean conversionRequired()
	{
		return ((new File(fname1)).exists() && (new File(fname2)).exists());
	}

	public void convert()
	{
		try
		{
			readIni();
		}
		catch (Exception e)
		{
			_logger.warn("Error reading ini files in for conversion: " + ExStack.toString(e));
			return;
		}
		WriteMediaLibraryIniTask task = new WriteMediaLibraryIniTask(getMediaDirs(), getMediaLibrary(), getMediaLibraryFiles());
		task.run();
		OptionsWriter.write(options);
		PlaylistHistory tempHistory = new PlaylistHistory(options.getMaxPlaylistHistoryEntries());
		tempHistory.initHistory(getHistory());
		(new FileWriter()).writeMruPlaylists(tempHistory);
		try
		{
			closeFile();
		}
		catch (IOException ex)
		{
			_logger.error(ExStack.toString(ex));
		}
		(new File(fname1)).delete();
		(new File(fname2)).delete();
	}

	private AppOptions getAppOptions()
	{
		return options;
	}

	private void readIni() throws Exception
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

		// Read in app options, but only if the file contains them in this spot...
		// skip first line, contains header
		if (line != null && line.startsWith("[Options]"))
		{
			line = B1.readLine().trim();
			while ((line != null) && (!line.startsWith("[")))
			{
				StringTokenizer tempTizer = new StringTokenizer(line, "=");
				String optionName = tempTizer.nextToken();
				String optionValue = tempTizer.nextToken();
				Integer optionEnum = AppOptions.optionEnumTable.get(optionName);
				if (optionEnum != null)
				{
					if (optionEnum.equals(AppOptionsEnum.AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD))
					{
						options.setAutoLocateEntriesOnPlaylistLoad((Boolean.valueOf(optionValue)).booleanValue());
					}
					else if (optionEnum.equals(AppOptionsEnum.MAX_PLAYLIST_HISTORY_SIZE))
					{
						options.setMaxPlaylistHistoryEntries((new Integer(optionValue)).intValue());
					}
					else if (optionEnum.equals(AppOptionsEnum.SAVE_RELATIVE_REFERENCES))
					{
						options.setSavePlaylistsWithRelativePaths((Boolean.valueOf(optionValue)).booleanValue());
					}
					else if (optionEnum.equals(AppOptionsEnum.AUTO_REFRESH_MEDIA_LIBRARY_ON_LOAD))
					{
						options.setAutoRefreshMediaLibraryOnStartup((Boolean.valueOf(optionValue)).booleanValue());
					}
					else if (optionEnum.equals(AppOptionsEnum.LOOK_AND_FEEL))
					{
						options.setLookAndFeel(optionValue);
					}
					else if (optionEnum.equals(AppOptionsEnum.ALWAYS_USE_UNC_PATHS))
					{
						options.setAlwaysUseUNCPaths((Boolean.valueOf(optionValue)).booleanValue());
					}
					else if (optionEnum.equals(AppOptionsEnum.PLAYLISTS_DIRECTORY))
					{
						options.setPlaylistsDirectory(optionValue);
					}
					else if (optionEnum.equals(AppOptionsEnum.APP_FONT))
					{
						Font temp = FontExtensions.deserialize(optionValue);
						if (temp != null)
						{
							options.setAppFont(temp);
						}
					}
					else if (optionEnum.equals(AppOptionsEnum.MAX_CLOSEST_RESULTS))
					{
						options.setMaxClosestResults((new Integer(optionValue)).intValue());
					}
				}
				line = B1.readLine();
			}
		}

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

	private void closeFile() throws IOException
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

	private String[] getMediaLibrary()
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

	private String[] getMediaLibraryFiles()
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
