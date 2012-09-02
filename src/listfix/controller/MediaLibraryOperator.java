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

package listfix.controller;

import listfix.io.*;
import listfix.util.*;
import listfix.view.support.ProgressWorker;

public class MediaLibraryOperator
{
	private GUIDriver guiDriver;
	private String[] mediaDir;
	private String[] mediaLibraryDirectoryList;
	private String[] mediaLibraryFileList;
	private String[] mediaDirs;
	private ProgressWorker _observer;

	public MediaLibraryOperator(ProgressWorker observer)
	{
		_observer = observer;
		guiDriver = GUIDriver.getInstance();
	}

	public void addDirectory(String dir)
	{
		if (!guiDriver.hasAddedMediaDirectory())
		{
			mediaDir = new String[1];
			mediaDir[0] = dir;
			DirectoryScanner ds = new DirectoryScanner();
			ds.createMediaLibraryDirectoryAndFileList(mediaDir, _observer);
			if (!_observer.getCancelled())
			{
				_observer.setMessage("Finishing...");
				mediaLibraryDirectoryList = ds.getDirectoryList();
				mediaLibraryFileList = ds.getFileList();
				ds.reset();
				guiDriver.setMediaDirs(mediaDir);
				java.util.Arrays.sort(mediaLibraryDirectoryList);
				guiDriver.setMediaLibraryDirectoryList(mediaLibraryDirectoryList);
				java.util.Arrays.sort(mediaLibraryFileList);
				guiDriver.setMediaLibraryFileList(mediaLibraryFileList);
				(new FileWriter()).writeMediaLibrary(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList);
			}
		}
		else
		{
			String[] tempMediaDir = new String[1];
			tempMediaDir[0] = dir;
			mediaDir = ArrayFunctions.copyArrayAddOneValue(guiDriver.getMediaDirs(), dir);
			DirectoryScanner ds = new DirectoryScanner();
			ds.createMediaLibraryDirectoryAndFileList(tempMediaDir, _observer);
			if (!_observer.getCancelled())
			{
				_observer.setMessage("Finishing...");
				mediaLibraryDirectoryList = ArrayFunctions.mergeArray(guiDriver.getMediaLibraryDirectoryList(), ds.getDirectoryList());
				mediaLibraryFileList = ArrayFunctions.mergeArray(guiDriver.getMediaLibraryFileList(), ds.getFileList());
				ds.reset();
				guiDriver.setMediaDirs(mediaDir);
				java.util.Arrays.sort(mediaLibraryDirectoryList);
				guiDriver.setMediaLibraryDirectoryList(mediaLibraryDirectoryList);
				java.util.Arrays.sort(mediaLibraryFileList);
				guiDriver.setMediaLibraryFileList(mediaLibraryFileList);
				(new FileWriter()).writeMediaLibrary(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList);
			}
		}
	}

	public void refresh()
	{
		mediaDirs = guiDriver.getMediaDirs();
		if (mediaDirs != null)
		{
			DirectoryScanner ds = new DirectoryScanner();
			ds.createMediaLibraryDirectoryAndFileList(guiDriver.getMediaDirs(), _observer);
			if (!_observer.getCancelled())
			{
				_observer.setMessage("Finishing...");
				mediaLibraryDirectoryList = ds.getDirectoryList();
				mediaLibraryFileList = ds.getFileList();
				ds.reset();
				guiDriver.setMediaDirs(mediaDirs);
				java.util.Arrays.sort(mediaLibraryDirectoryList);
				guiDriver.setMediaLibraryDirectoryList(mediaLibraryDirectoryList);
				java.util.Arrays.sort(mediaLibraryFileList);
				guiDriver.setMediaLibraryFileList(mediaLibraryFileList);
				(new FileWriter()).writeMediaLibrary(mediaDirs, mediaLibraryDirectoryList, mediaLibraryFileList);
			}
		}
	}
}
