/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2014 Jeremy Caron
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
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringTokenizer;

import listfix.io.Constants;
import listfix.model.AppOptions;
import listfix.util.ExStack;
import listfix.view.support.FontExtensions;

import org.apache.log4j.Logger;

/**
 * Reads the core options file, turning it into an AppOptions object.
 *
 * @author jcaron
 */
public class OptionsReader
{
	private static final Logger _logger = Logger.getLogger(OptionsReader.class);

	/**
	 * Performs the de-serialization of the app options back into memory.
	 *
	 * @return
	 */
	public static AppOptions read()
	{
		AppOptions options = new AppOptions();
		if ((new File(Constants.OPTIONS_INI)).exists())
		{
			try
			{
				List<String> lines = Files.readAllLines(Paths.get(Constants.OPTIONS_INI), StandardCharsets.UTF_8);
				int i = 0;
				String line = lines.get(i++);

				// Read in app options, but only if the file contains them in this spot...
				// skip first line, contains header
				if (line != null && line.startsWith("[Options]"))
				{
					line = lines.get(i++).trim();
					while ((line != null) && i < lines.size() && (!line.startsWith("[")))
					{
						StringTokenizer tempTizer = new StringTokenizer(line, "=");
						String optionName = tempTizer.nextToken();
						String optionValue = tempTizer.nextToken();
						if (optionName != null)
						{
							if (optionName.equalsIgnoreCase(AppOptions.AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD))
							{
								options.setAutoLocateEntriesOnPlaylistLoad((Boolean.valueOf(optionValue)));
							}
							else if (optionName.equalsIgnoreCase(AppOptions.MAX_PLAYLIST_HISTORY_SIZE))
							{
								options.setMaxPlaylistHistoryEntries((new Integer(optionValue)));
							}
							else if (optionName.equalsIgnoreCase(AppOptions.SAVE_RELATIVE_REFERENCES))
							{
								options.setSavePlaylistsWithRelativePaths((Boolean.valueOf(optionValue)));
							}
							else if (optionName.equalsIgnoreCase(AppOptions.AUTO_REFRESH_MEDIA_LIBRARY_ON_LOAD))
							{
								options.setAutoRefreshMediaLibraryOnStartup((Boolean.valueOf(optionValue)));
							}
							else if (optionName.equalsIgnoreCase(AppOptions.LOOK_AND_FEEL))
							{
								options.setLookAndFeel(optionValue);
							}
							else if (optionName.equalsIgnoreCase(AppOptions.ALWAYS_USE_UNC_PATHS))
							{
								options.setAlwaysUseUNCPaths((Boolean.valueOf(optionValue)));
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
								options.setMaxClosestResults((new Integer(optionValue)));
							}
							else if (optionName.equalsIgnoreCase(AppOptions.IGNORED_SMALL_WORDS))
							{
								options.setIgnoredSmallWords(optionValue);
							}
							else if (optionName.equalsIgnoreCase(AppOptions.CASE_INSENSITIVE_EXACT_MATCHING))
							{
								options.setCaseInsensitiveExactMatching((Boolean.valueOf(optionValue)));
							}
						}
						line = lines.get(i++);
					}
				}

			}
			catch (IOException | NumberFormatException ex)
			{
				_logger.error(ExStack.toString(ex));
			}
		}
		return options;
	}
}
