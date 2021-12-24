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

package listfix.io.writers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import listfix.controller.tasks.WriteMediaLibraryIniTask;
import listfix.io.Constants;
import listfix.model.AppOptions;
import listfix.model.PlaylistHistory;
import listfix.util.ExStack;
import listfix.util.UnicodeUtils;

import org.apache.log4j.Logger;

/**
 * Provides methods for writing out the ini files for this program.
 * @author jcaron
 */
public class FileWriter
{
	private static final Logger _logger = Logger.getLogger(FileWriter.class);

	/**
	 * If the application's being started for the first time, this method will write out the default ini files.
	 */
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
				_logger.error(ExStack.toString(e));
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
				_logger.error(ExStack.toString(e));
			}
		}
	}

	/**
	 * Writes out the MRU playlists.
	 * @param history
	 */
	public void writeMruPlaylists(PlaylistHistory history)
	{
		try
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append("[Recent Playlists]").append(Constants.BR);
			String[] filenames = history.getFilenames();
			for (String filename : filenames)
			{
				buffer.append(filename).append(Constants.BR);
			}
			try (FileOutputStream outputStream = new FileOutputStream(Constants.HISTORY_INI))
			{
				Writer osw = new OutputStreamWriter(outputStream, "UTF8");
				try (BufferedWriter output = new BufferedWriter(osw))
				{
					output.write(UnicodeUtils.getBOM("UTF-8") + buffer.toString());
				}
			}
		}
		catch (IOException e)
		{
			// eat the error and continue
			_logger.error(ExStack.toString(e));
		}
	}

	/**
	 * Spawns a background task to write the media library to disk.
	 * @param mediaDir
	 * @param mediaLibraryDirList
	 * @param mediaLibraryFileList
	 */
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
			_logger.error(ExStack.toString(e));
		}
	}
}
        