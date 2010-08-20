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

/**
 *
 * @author  jcaron
 * @version 
 */
import listfix.io.*;
import listfix.controller.*;

public class UpdateMediaLibraryTask extends listfix.controller.Task
{
	private GUIDriver guiDriver;
	private String[] mediaDirs;
	private String[] mediaLibraryDirectoryList;
	private String[] mediaLibraryFileList;

	public UpdateMediaLibraryTask(GUIDriver gd)
	{
		guiDriver = gd;
		mediaDirs = gd.getMediaDirs();
	}

	/** Run the task. This method is the body of the thread for this task.  */
	@Override
	public void run()
	{
		if (mediaDirs != null)
		{
			DirectoryScanner ds = new DirectoryScanner();
			ds.createMediaLibraryDirectoryAndFileList(guiDriver.getMediaDirs(), this);
			this.setMessage("Finishing...");
			mediaLibraryDirectoryList = ds.getDirectoryList();
			mediaLibraryFileList = ds.getFileList();
			ds.reset();
			java.util.Arrays.sort(mediaDirs);
			guiDriver.setMediaDirs(mediaDirs);
			java.util.Arrays.sort(mediaLibraryDirectoryList);
			guiDriver.setMediaLibraryDirectoryList(mediaLibraryDirectoryList);
			java.util.Arrays.sort(mediaLibraryFileList);
			guiDriver.setMediaLibraryFileList(mediaLibraryFileList);
			this.notifyObservers(100);
			(new FileWriter()).writeIni(mediaDirs, mediaLibraryDirectoryList, mediaLibraryFileList, guiDriver.getAppOptions());
		}
	}
}
