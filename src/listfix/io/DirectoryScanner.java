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

	private static boolean endsWithIndexedExtension(String input)
	{
		input = input.toLowerCase();
		return (input.endsWith(".mp3") || input.endsWith(".wma")
			|| input.endsWith(".flac") || input.endsWith(".ogg")
			|| input.endsWith(".wav")  || input.endsWith(".midi")
			|| input.endsWith(".cda")  || input.endsWith(".mpg")
			|| input.endsWith(".mpeg") || input.endsWith(".m2v")
			|| input.endsWith(".avi")  || input.endsWith(".m4v")
			|| input.endsWith(".flv")  || input.endsWith(".mid")
			|| input.endsWith(".mp2")  || input.endsWith(".mp1")
			|| input.endsWith(".aac")  || input.endsWith(".asx")
			|| input.endsWith(".m4a")  || input.endsWith(".mp4")
			|| input.endsWith(".m4v")  || input.endsWith(".nsv")
			|| input.endsWith(".aiff") || input.endsWith(".au")
			|| input.endsWith(".wmv")  || input.endsWith(".asf")
			|| input.endsWith(".mpc"));
	}

	private void recursiveDir(String baseDir, ProgressWorker task)
	{
		recursiveCount++;
		if (!task.getCancelled())
		{
			task.setMessage("<html><body>Scanning Directory #" + recursiveCount + "<BR>" + (baseDir.length() < 70 ? baseDir : baseDir.substring(0, 70) + "...") + "</body></html>");

			File mediaDir = new File(baseDir);
			String[] entryList = mediaDir.list();
			List<String> fileList = new ArrayList<String>();
			List<String> dirList = new ArrayList<String>();
			StringBuilder s = new StringBuilder();

			if (entryList != null)
			{
				File tempFile = null;
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
						if (endsWithIndexedExtension(s.toString()))
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

	public void reset()
	{
		recursiveCount = 0;
		thisDirList = new ArrayList<String>();
		thisFileList = new ArrayList<String>();
	}

	public String[] getFileList()
	{
		String[] result = new String[thisFileList.size()];
		thisFileList.toArray(result);
		return result;
	}

	public String[] getDirectoryList()
	{
		String[] result = new String[thisDirList.size()];
		thisDirList.toArray(result);
		return result;
	}
}
