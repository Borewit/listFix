/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2010 Jeremy Caron
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

package listfix.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import listfix.model.AppOptions;
import listfix.view.support.FontExtensions;

/**
 *
 * @author jcaron
 */
public class OptionsWriter
{
	public static void appendOptionsText(StringBuilder buffer, AppOptions options)
	{
		buffer.append("[Options]").append(Constants.BR);
		buffer.append("AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD=").append(Boolean.toString(options.getAutoLocateEntriesOnPlaylistLoad())).append(Constants.BR);
		buffer.append("MAX_PLAYLIST_HISTORY_SIZE=").append(options.getMaxPlaylistHistoryEntries()).append(Constants.BR);
		buffer.append("SAVE_RELATIVE_REFERENCES=").append(Boolean.toString(options.getSavePlaylistsWithRelativePaths())).append(Constants.BR);
		buffer.append("AUTO_REFRESH_MEDIA_LIBRARY_ON_LOAD=").append(Boolean.toString(options.getAutoRefreshMediaLibraryOnStartup())).append(Constants.BR);
		buffer.append("LOOK_AND_FEEL=").append(options.getLookAndFeel()).append(Constants.BR);
		buffer.append("ALWAYS_USE_UNC_PATHS=").append(Boolean.toString(options.getAlwaysUseUNCPaths())).append(Constants.BR);
		buffer.append("PLAYLISTS_DIRECTORY=").append(options.getPlaylistsDirectory()).append(Constants.BR);
		buffer.append("APP_FONT=").append(FontExtensions.serialize(options.getAppFont())).append(Constants.BR);
		buffer.append("MAX_CLOSEST_RESULTS=").append(options.getMaxClosestResults()).append(Constants.BR);
	}

	public static void writeDefaults()
	{
		File test = new File(Constants.OPTIONS_INI);
		if (!test.exists() || (test.exists() && test.length() == 0))
		{
			write(new AppOptions());
		}
	}

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
			e.printStackTrace();
		}
	}
}
