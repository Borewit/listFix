/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2009 Jeremy Caron
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

import java.util.Vector;
import listfix.model.PlaylistEntry;

public class LocateFilesTask extends listfix.controller.Task
{
	private Vector<PlaylistEntry> entries;
	private String[] mediaLibraryFileList;

	/** Creates new LocateFilesTask */
	public LocateFilesTask(Vector<PlaylistEntry> playlistEntries, String[] files)
	{
		entries = playlistEntries;
		mediaLibraryFileList = files;
	}

	/** Run the task. This method is the body of the thread for this task.  */
	@Override
	public void run()
	{
		PlaylistEntry tempEntry = null;
		for (int i = 0; i < entries.size(); i++)
		{
			tempEntry = entries.elementAt(i);
			if (!tempEntry.isURL())
			{
				if (tempEntry.exists())
				{
					tempEntry.setMessage("Found!");
				}
				else
				{
					tempEntry.findNewLocationFromFileList(mediaLibraryFileList);
				}
			}
			this.notifyObservers((int) ((double) i / (double) (entries.size() - 1) * 100.0));
		}
		this.notifyObservers(100);
	}

	public Vector<PlaylistEntry> locateFiles()
	{
		return entries;
	}
}
