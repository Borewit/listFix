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

package listfix.controller;

import java.io.*;
import java.util.*;

import listfix.exceptions.*;
import listfix.io.FileWriter;
import listfix.io.IniFileReader;
import listfix.io.UNCFile;
import listfix.model.*;
import listfix.util.ArrayFunctions;

public class GUIDriver
{
	private boolean showMediaDirWindow = false;
	private String[] mediaDir = null;
	private String[] mediaLibraryDirectoryList = null;
	private String[] mediaLibraryFileList = null;
	private AppOptions options = new AppOptions();
	private M3UHistory history = new M3UHistory(options.getMaxPlaylistHistoryEntries());
	public static final boolean fileSystemIsCaseSensitive = File.separatorChar == '/';

    public static GUIDriver getInstance()
    {
        if (_instance == null)
            _instance = new GUIDriver();
        return _instance;
    }
    private static GUIDriver _instance;

	private GUIDriver()
	{
		try
		{
			(new FileWriter()).writeDefaultIniFilesIfNeeded();
			IniFileReader initReader = new IniFileReader();
			initReader.readIni();
			options = initReader.getAppOptions();
			mediaDir = initReader.getMediaDirs();
			history = new M3UHistory(options.getMaxPlaylistHistoryEntries());
			history.initHistory(initReader.getHistory());
			mediaLibraryDirectoryList = initReader.getMediaLibrary();
			mediaLibraryFileList = initReader.getMediaLibraryFiles();

			for (String dir : mediaDir)
			{
				if (!new File(dir).exists())
				{
					this.removeMediaDir(dir);
				}
			}

			if (mediaDir.length == 0)
			{
				showMediaDirWindow = true;
			}
		}
		catch (Exception e)
		{
			showMediaDirWindow = true;
			e.printStackTrace();
		}
	}

	public AppOptions getAppOptions()
	{
		return options;
	}

	public void setAppOptions(AppOptions opts)
	{
		options = opts;
	}

	public String[] getMediaDirs()
	{
		return mediaDir;
	}

	public void setMediaDirs(String[] value)
	{
		mediaDir = value;
	}

	public String[] getMediaLibraryDirectoryList()
	{
		return mediaLibraryDirectoryList;
	}

	public void setMediaLibraryDirectoryList(String[] value)
	{
		mediaLibraryDirectoryList = value;
	}

	public String[] getMediaLibraryFileList()
	{
		return mediaLibraryFileList;
	}

	public void setMediaLibraryFileList(String[] value)
	{
		mediaLibraryFileList = value;
	}

	public boolean getShowMediaDirWindow()
	{
		return showMediaDirWindow;
	}

	public M3UHistory getHistory()
	{
		return history;
	}

	public void clearM3UHistory()
	{
		history.clearHistory();
		(new FileWriter()).writeMruPlaylists(history);
	}

	public String[] getRecentM3Us()
	{
		return history.getM3UFilenames();
	}

	public String[] removeMediaDir(String dir) throws MediaDirNotFoundException
	{
		boolean found = false;
		int i = 0;
		int length = mediaDir.length;
		if (mediaDir.length == 1)
		{
			while ((i < length) && (found != true))
			{
				if (mediaDir[i].equals(dir))
				{
					mediaDir = ArrayFunctions.removeItem(mediaDir, i);
					found = true;
				}
				else
				{
					i++;
				}
			}
			if (found)
			{
				mediaLibraryDirectoryList = new String[0];
				mediaLibraryFileList = new String[0];
				(new FileWriter()).writeIni(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList, options);
			}
			else
			{
				throw new MediaDirNotFoundException(dir + " was not in the media directory list.");
			}
			return mediaDir;
		}
		else
		{
			while ((i < length) && (found != true))
			{
				if (mediaDir[i].equals(dir))
				{
					mediaDir = ArrayFunctions.removeItem(mediaDir, i);
					found = true;
				}
				else
				{
					i++;
				}
			}
			if (found)
			{
				List<String> mldVector = new ArrayList<String>(Arrays.asList(mediaLibraryDirectoryList));
				List<String> toRemove = new ArrayList<String>();
				for (String toTest : mldVector)
				{
					if (toTest.startsWith(dir))
					{
						toRemove.add(toTest);
					}
				}
				mldVector.removeAll(toRemove);
				mediaLibraryDirectoryList = mldVector.toArray(new String[mldVector.size()]);
				mldVector = null;
				toRemove.clear();

				List<String> mlfVector = new ArrayList<String>(Arrays.asList(mediaLibraryFileList));
				for (String toTest : mlfVector)
				{
					if (toTest.startsWith(dir))
					{
						toRemove.add(toTest);
					}
				}
				mlfVector.removeAll(toRemove);
				mediaLibraryFileList = mlfVector.toArray(new String[mlfVector.size()]);
				mlfVector = null;

				(new FileWriter()).writeIni(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList, options);
			}
			else
			{
				throw new MediaDirNotFoundException(dir + " was not in the media directory list.");
			}
			return mediaDir;
		}
	}

	public void switchMediaLibraryToUNCPaths()
	{
		if (mediaDir != null)
		{
			for (int i = 0; i < mediaDir.length; i++)
			{
				UNCFile file = new UNCFile(mediaDir[i]);
				if (file.onNetworkDrive())
				{
					mediaDir[i] = file.getUNCPath();
				}
			}
		}

		if (mediaLibraryDirectoryList != null)
		{
			for (int i = 0; i < mediaLibraryDirectoryList.length; i++)
			{
				UNCFile file = new UNCFile(mediaLibraryDirectoryList[i]);
				if (file.onNetworkDrive())
				{
					mediaLibraryDirectoryList[i] = file.getUNCPath();
				}
			}
		}

		if (mediaLibraryFileList != null)
		{
			for (int i = 0; i < mediaLibraryFileList.length; i++)
			{
				UNCFile file = new UNCFile(mediaLibraryFileList[i]);
				if (file.onNetworkDrive())
				{
					mediaLibraryFileList[i] = file.getUNCPath();
				}

			}
		}
	}
}