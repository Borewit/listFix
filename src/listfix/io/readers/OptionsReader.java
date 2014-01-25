/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2012 Jeremy Caron
 * 
 *  This file is part of listFix().
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.io.readers;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import listfix.io.Constants;
import listfix.io.UnicodeInputStream;
import listfix.model.AppOptions;
import listfix.util.ExStack;
import listfix.view.support.FontExtensions;

import org.apache.log4j.Logger;

/**
 * Reads the core options file, turning it into an AppOptions object.
 * @author jcaron
 */
public class OptionsReader
{
	private static final Logger _logger = Logger.getLogger(OptionsReader.class);

	/**
	 * Performs the de-serialization of the app options back into memory.
	 * @return
	 */
	public static AppOptions read()
	{
		AppOptions options = new AppOptions();
		try
		{
			try (BufferedReader B1 = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(new File(Constants.OPTIONS_INI)), "UTF-8"), "UTF8")))
			{
				String line = B1.readLine();
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
						if (optionName != null)
						{
							if (optionName.equalsIgnoreCase(AppOptions.AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD))
							{
								options.setAutoLocateEntriesOnPlaylistLoad((Boolean.valueOf(optionValue)).booleanValue());
							}
							else if (optionName.equalsIgnoreCase(AppOptions.MAX_PLAYLIST_HISTORY_SIZE))
							{
								options.setMaxPlaylistHistoryEntries((new Integer(optionValue)).intValue());
							}
							else if (optionName.equalsIgnoreCase(AppOptions.SAVE_RELATIVE_REFERENCES))
							{
								options.setSavePlaylistsWithRelativePaths((Boolean.valueOf(optionValue)).booleanValue());
							}
							else if (optionName.equalsIgnoreCase(AppOptions.AUTO_REFRESH_MEDIA_LIBRARY_ON_LOAD))
							{
								options.setAutoRefreshMediaLibraryOnStartup((Boolean.valueOf(optionValue)).booleanValue());
							}
							else if (optionName.equalsIgnoreCase(AppOptions.LOOK_AND_FEEL))
							{
								options.setLookAndFeel(optionValue);
							}
							else if (optionName.equalsIgnoreCase(AppOptions.ALWAYS_USE_UNC_PATHS))
							{
								options.setAlwaysUseUNCPaths((Boolean.valueOf(optionValue)).booleanValue());
							}
							else if (optionName.equalsIgnoreCase(AppOptions.PLAYLISTS_DIRECTORY))
							{
								options.setPlaylistsDirectory(optionValue);
							}
							else if (optionName.equalsIgnoreCase(AppOptions.APP_FONT))
							{
								Font temp = FontExtensions.deserialize(optionValue);
								if (temp != null)
								{
									options.setAppFont(temp);
								}
							}
							else if (optionName.equalsIgnoreCase(AppOptions.MAX_CLOSEST_RESULTS))
							{
								options.setMaxClosestResults((new Integer(optionValue)).intValue());
							}
							else if (optionName.equalsIgnoreCase(AppOptions.IGNORED_SMALL_WORDS))
							{
								options.setIgnoredSmallWords(optionValue);
							}
							else if (optionName.equalsIgnoreCase(AppOptions.CASE_INSENSITIVE_EXACT_MATCHING))
							{
								options.setCaseInsensitiveExactMatching((Boolean.valueOf(optionValue)).booleanValue());
							}
						}
						line = B1.readLine();
					}
				}
			}
		}
		catch (IOException | NumberFormatException ex)
		{
			_logger.error(ExStack.toString(ex));
		}
		return options;
	}
}
