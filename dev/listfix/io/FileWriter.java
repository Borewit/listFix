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

package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     FileWriter.java
= Purpose:  Provides methods for writing out the ini files for this program.
============================================================================
 */
import java.io.*;

import listfix.controller.tasks.WriteMediaLibraryIniTask;
import listfix.model.AppOptions;
import listfix.model.PlaylistHistory;
import listfix.util.UnicodeUtils;
import org.apache.log4j.Logger;

public class FileWriter
{
	private static final Logger _logger = Logger.getLogger(FileWriter.class);

	public void writeDefaultIniFilesIfNeeded()
	{
		BufferedWriter output;
		FileOutputStream outputStream;

		File testDir = new File(Constants.DATA_DIR);
		if (!testDir.exists())
		{
			testDir.mkdir();
		}

		File testFile = new File(Constants.MEDIA_LIBRARY_INI);
		if (!testFile.exists() || (testFile.exists() && testFile.length() == 0))
		{
			try
			{
				StringBuilder buffer = new StringBuilder();
				AppOptions options = new AppOptions();
				outputStream = new FileOutputStream(Constants.MEDIA_LIBRARY_INI);
				Writer osw = new OutputStreamWriter(outputStream, "UTF8");
				output = new BufferedWriter(osw);
				buffer.append("[Media Directories]").append(Constants.BR);
				buffer.append("[Media Library Directories]").append(Constants.BR);
				buffer.append("[Media Library Files]").append(Constants.BR);
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

		OptionsWriter.writeDefaults();

		testFile = new File(Constants.HISTORY_INI);
		if (!testFile.exists() || (testFile.exists() && testFile.length() == 0))
		{
			try
			{
				outputStream = new FileOutputStream(Constants.HISTORY_INI);
				Writer osw = new OutputStreamWriter(outputStream, "UTF8");
				output = new BufferedWriter(osw);
				output.write(UnicodeUtils.getBOM("UTF-8") + "[Recent Playlists]" + Constants.BR);
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

	public void writeMruPlaylists(PlaylistHistory history)
	{
		try
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append("[Recent Playlists]").append(Constants.BR);
			String[] filenames = history.getFilenames();
			for (int i = 0; i < filenames.length; i++)
			{
				buffer.append(filenames[i]).append(Constants.BR);
			}
			FileOutputStream outputStream = new FileOutputStream(Constants.HISTORY_INI);
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

	public void writeMediaLibrary(String[] mediaDir, String[] mediaLibraryDirList, String[] mediaLibraryFileList)
	{
		try
		{
			WriteMediaLibraryIniTask thisTask = new WriteMediaLibraryIniTask(mediaDir, mediaLibraryDirList, mediaLibraryFileList);
			thisTask.start();
		}
		catch (Exception e)
		{
			// eat the error and continue
			e.printStackTrace();
		}
	}
}
        