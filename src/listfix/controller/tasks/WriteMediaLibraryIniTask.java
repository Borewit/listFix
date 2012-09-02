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

package listfix.controller.tasks;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import listfix.io.Constants;
import listfix.util.ExStack;
import listfix.util.UnicodeUtils;
import org.apache.log4j.Logger;

public class WriteMediaLibraryIniTask extends listfix.controller.Task
{
	private String[] mediaDir;
	private String[] mediaLibraryDirList;
	private String[] mediaLibraryFileList;

	private static final Logger _logger = Logger.getLogger(WriteMediaLibraryIniTask.class);

	public WriteMediaLibraryIniTask(String[] m, String[] mldl, String[] mlfl)
	{
		mediaDir = m;
		mediaLibraryDirList = mldl;
		mediaLibraryFileList = mlfl;
	}

	/** Run the task. This method is the body of the thread for this task.  */
	@Override
	public void run()
	{
		try
		{
			StringBuilder buffer = new StringBuilder();
			if (mediaDir != null)
			{
				buffer.append("[Media Directories]").append(Constants.BR);
				for (int i = 0; i < mediaDir.length; i++)
				{
					buffer.append(mediaDir[i]).append(Constants.BR);
				}
			}

			if (mediaLibraryDirList != null)
			{
				buffer.append("[Media Library Directories]").append(Constants.BR);
				for (int i = 0; i < mediaLibraryDirList.length; i++)
				{
					buffer.append(mediaLibraryDirList[i]).append(Constants.BR);
				}
			}

			if (mediaLibraryDirList != null)
			{
				buffer.append("[Media Library Files]").append(Constants.BR);
				for (int i = 0; i < mediaLibraryFileList.length; i++)
				{
					buffer.append(mediaLibraryFileList[i]).append(Constants.BR);
				}
			}

			FileOutputStream outputStream = new FileOutputStream(Constants.MEDIA_LIBRARY_INI);
			Writer osw = new OutputStreamWriter(outputStream, "UTF8");
			BufferedWriter output = new BufferedWriter(osw);
			output.write(UnicodeUtils.getBOM("UTF-8") + buffer.toString());
			output.close();
			outputStream.close();
		}
		catch (IOException e)
		{
			// eat the error and continue
			_logger.error(ExStack.toString(e));
		}
	}
}
