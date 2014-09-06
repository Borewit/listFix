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

package listfix.io.writers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import listfix.io.Constants;
import listfix.model.AppOptions;
import listfix.util.ExStack;
import listfix.view.support.FontExtensions;

import org.apache.log4j.Logger;

/**
 *
 * @author jcaron
 */
public class OptionsWriter
{
	private static final Logger _logger = Logger.getLogger(OptionsWriter.class);

	/**
	 * Write out the default options file if the OPTIONS_INI file is missing or empty.
	 */
	public static void writeDefaults()
	{
		File test = new File(Constants.OPTIONS_INI);
		if (!test.exists() || (test.exists() && test.length() == 0))
		{
			write(new AppOptions());
		}
	}

	/**
	 * Write out the given AppOptions to disk.
	 * @param options
	 */
	public static void write(AppOptions options)
	{
		File test = new File(Constants.OPTIONS_INI);
		FileOutputStream outputStream;
		BufferedWriter output;
		try
		{
			StringBuilder buffer = new StringBuilder();
			outputStream = new FileOutputStream(test);
			Writer osw = new OutputStreamWriter(outputStream, "UTF8");
			output = new BufferedWriter(osw);
			OptionsWriter.appendOptionsText(buffer, options);
			output.write(buffer.toString());
			output.close();
			outputStream.close();
		}
		catch (Exception e)
		{
			// eat the error and continue
			_logger.error(ExStack.toString(e));
		}
	}
	
	private static void appendOptionsText(StringBuilder buffer, AppOptions options)
	{
		buffer.append("[Options]").append(Constants.BR);
		buffer.append(AppOptions.AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD).append("=").append(Boolean.toString(options.getAutoLocateEntriesOnPlaylistLoad())).append(Constants.BR);
		buffer.append(AppOptions.MAX_PLAYLIST_HISTORY_SIZE).append("=").append(options.getMaxPlaylistHistoryEntries()).append(Constants.BR);
		buffer.append(AppOptions.SAVE_RELATIVE_REFERENCES).append("=").append(Boolean.toString(options.getSavePlaylistsWithRelativePaths())).append(Constants.BR);
		buffer.append(AppOptions.AUTO_REFRESH_MEDIA_LIBRARY_ON_LOAD).append("=").append(Boolean.toString(options.getAutoRefreshMediaLibraryOnStartup())).append(Constants.BR);
		buffer.append(AppOptions.LOOK_AND_FEEL).append("=").append(options.getLookAndFeel()).append(Constants.BR);
		buffer.append(AppOptions.ALWAYS_USE_UNC_PATHS).append("=").append(Boolean.toString(options.getAlwaysUseUNCPaths())).append(Constants.BR);
		buffer.append(AppOptions.PLAYLISTS_DIRECTORY).append("=").append(options.getPlaylistsDirectory()).append(Constants.BR);
		buffer.append(AppOptions.APP_FONT).append("=").append(FontExtensions.serialize(options.getAppFont())).append(Constants.BR);
		buffer.append(AppOptions.MAX_CLOSEST_RESULTS).append("=").append(options.getMaxClosestResults()).append(Constants.BR);
		buffer.append(AppOptions.IGNORED_SMALL_WORDS).append("=").append(options.getIgnoredSmallWords()).append(Constants.BR);
		buffer.append(AppOptions.CASE_INSENSITIVE_EXACT_MATCHING).append("=").append(options.getCaseInsensitiveExactMatching()).append(Constants.BR);
	}
}
