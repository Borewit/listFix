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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import listfix.model.*;
import listfix.io.*;

public class CopyFilesTask extends listfix.controller.Task
{
	private Playlist list;
	private File destination;

	/** Creates new CopyFilesTask */
	public CopyFilesTask(Playlist x, File y)
	{
		list = x;
		destination = y;
	}

	/** Run the task. This method is the body of the thread for this task.  */
	@Override
	public void run()
	{
		PlaylistEntry tempEntry = null;
		File fileToCopy = null;
		File dest = null;
		String fs = System.getProperty("file.separator");
		for (int i = 0; i < list.size(); i++)
		{
			tempEntry = list.get(i);
			if (!tempEntry.isURL())
			{
				fileToCopy = tempEntry.getAbsoluteFile();
				if (tempEntry.isFound()) // && fileToCopy.exists())
				{
					dest = new File(destination.getPath() + fs + tempEntry.getFileName());
					try
					{
						FileCopier.copy(new FileInputStream(fileToCopy), new FileOutputStream(dest));
					}
					catch (IOException e)
					{
						// eat the error and continue
						e.printStackTrace();
					}
				}
			}
			this.notifyObservers((int) ((double) i / (double) (list.size() - 1) * 100.0));
		}
	}
}
