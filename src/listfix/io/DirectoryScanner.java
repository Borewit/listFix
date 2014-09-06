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

package listfix.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import listfix.view.support.ProgressWorker;

/**
 * Creates a list of the indexed files and subdirectories contained in a list of input directories.
 * @author jcaron
 */

public class DirectoryScanner
{
	private List<String> thisDirList;
	private List<String> thisFileList;
	private int recursiveCount = 0;

	/**
	 *
	 * @param baseDirs
	 * @param task
	 */
	public void createMediaLibraryDirectoryAndFileList(String[] baseDirs, ProgressWorker task)
	{
		this.reset();
		for (int i = 0; i < baseDirs.length; i++)
		{
			if (new File(baseDirs[i]).exists())
			{
				thisDirList.add(baseDirs[i]);
				this.recursiveDir(baseDirs[i], task);
			}
		}
	}

	private void recursiveDir(String baseDir, ProgressWorker task)
	{
		recursiveCount++;
		if (!task.getCancelled())
		{
			task.setMessage("<html><body>Scanning Directory #" + recursiveCount + "<BR>" + (baseDir.length() < 70 ? baseDir : baseDir.substring(0, 70) + "...") + "</body></html>");

			File mediaDir = new File(baseDir);
			String[] entryList = mediaDir.list();
			List<String> fileList = new ArrayList<>();
			List<String> dirList = new ArrayList<>();
			StringBuilder s = new StringBuilder();

			if (entryList != null)
			{
				File tempFile;
				for (int i = 0; i < entryList.length; i++)
				{
					s.append(baseDir);
					if (!baseDir.endsWith(Constants.FS))
					{
						s.append(Constants.FS);
					}
					s.append(entryList[i]);
					tempFile = new File(s.toString());
					if (tempFile.isDirectory())
					{
						dirList.add(s.toString());
					}
					else
					{
						if (FileUtils.IsMediaFile(tempFile))
						{
							fileList.add(s.toString());
						}
					}
					s.setLength(0);
				}
			}

			Collections.sort(fileList);
			Collections.sort(dirList);

			for (String file : fileList)
			{
				thisFileList.add(file);
			}

			for (String dir : dirList)
			{
				thisDirList.add(dir);
				recursiveDir(dir, task);
			}

			fileList.clear();
			dirList.clear();
		}
		else
		{
			return;
		}
	}

	/**
	 *
	 */
	public void reset()
	{
		recursiveCount = 0;
		thisDirList = new ArrayList<>();
		thisFileList = new ArrayList<>();
	}

	/**
	 *
	 * @return
	 */
	public String[] getFileList()
	{
		String[] result = new String[thisFileList.size()];
		thisFileList.toArray(result);
		return result;
	}

	/**
	 *
	 * @return
	 */
	public String[] getDirectoryList()
	{
		String[] result = new String[thisDirList.size()];
		thisDirList.toArray(result);
		return result;
	}
}
