/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2010 Jeremy Caron
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

package listfix.controller.tasks;

import java.io.*;
import listfix.model.*;
import listfix.util.UnicodeUtils;
import listfix.view.support.FontExtensions;

public class WriteIniFileTask extends listfix.controller.Task
{
	private final static String fs = System.getProperty("file.separator");
	private final static String br = System.getProperty("line.separator");
	private final static String homeDir = System.getProperty("user.home");
	private String[] mediaDir;
	private String[] mediaLibraryDirList;
	private String[] mediaLibraryFileList;
	private AppOptions options;

	public WriteIniFileTask(String[] m, String[] mldl, String[] mlfl, AppOptions opts)
	{
		mediaDir = m;
		mediaLibraryDirList = mldl;
		mediaLibraryFileList = mlfl;
		options = opts;
	}

	/** Run the task. This method is the body of the thread for this task.  */
	public void run()
	{
		try
		{
			StringBuilder buffer = new StringBuilder();
			if (mediaDir != null)
			{
				buffer.append("[Media Directories]").append(br);
				for (int i = 0; i < mediaDir.length; i++)
				{
					buffer.append(mediaDir[i]).append(br);
				}
			}

			if (options != null)
			{
				buffer.append("[Options]").append(br);
				buffer.append("AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD=").append(Boolean.toString(options.getAutoLocateEntriesOnPlaylistLoad())).append(br);
				buffer.append("MAX_PLAYLIST_HISTORY_SIZE=").append(options.getMaxPlaylistHistoryEntries()).append(br);
				buffer.append("SAVE_RELATIVE_REFERENCES=").append(Boolean.toString(options.getSavePlaylistsWithRelativePaths())).append(br);
				buffer.append("AUTO_REFRESH_MEDIA_LIBRARY_ON_LOAD=").append(Boolean.toString(options.getAutoRefreshMediaLibraryOnStartup())).append(br);
				buffer.append("LOOK_AND_FEEL=").append(options.getLookAndFeel()).append(br);
				buffer.append("ALWAYS_USE_UNC_PATHS=").append(options.getAlwaysUseUNCPaths()).append(br);
				buffer.append("PLAYLISTS_DIRECTORY=").append(options.getPlaylistsDirectory()).append(br);
				buffer.append("APP_FONT=").append(FontExtensions.serialize(options.getAppFont())).append(br);
				buffer.append("MAX_CLOSEST_RESULTS=").append(options.getMaxClosestResults()).append(br);
			}

			if (mediaLibraryDirList != null)
			{
				buffer.append("[Media Library Directories]").append(br);
				for (int i = 0; i < mediaLibraryDirList.length; i++)
				{
					buffer.append(mediaLibraryDirList[i]).append(br);
				}
			}

			if (mediaLibraryDirList != null)
			{
				buffer.append("[Media Library Files]").append(br);
				for (int i = 0; i < mediaLibraryFileList.length; i++)
				{
					buffer.append(mediaLibraryFileList[i]).append(br);
				}
			}

			FileOutputStream outputStream = new FileOutputStream(homeDir + fs + "dirLists.ini");
			Writer osw = new OutputStreamWriter(outputStream, "UTF8");
			BufferedWriter output = new BufferedWriter(osw);
			output.write(UnicodeUtils.getBOM("UTF-8") + buffer.toString());
			output.close();
			outputStream.close();
		}
		catch (IOException e)
		{
			// eat the error and continue
			e.printStackTrace();
		}
	}
}
