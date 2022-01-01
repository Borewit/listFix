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

package listfix.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import listfix.exceptions.MediaDirNotFoundException;
import listfix.io.writers.FileWriter;
import listfix.io.IniFileConverter;
import listfix.io.readers.IniFileReader;
import listfix.io.readers.OptionsReader;
import listfix.io.UNCFile;
import listfix.model.AppOptions;
import listfix.model.PlaylistHistory;
import listfix.util.ArrayFunctions;
import listfix.util.ExStack;

import org.apache.log4j.Logger;

/**
 *
 * @author jcaron
 */
public final class GUIDriver
{
  private boolean showMediaDirWindow = false;
  private String[] mediaDir = null;
  private String[] mediaLibraryDirectoryList = null;
  private String[] mediaLibraryFileList = null;
  private AppOptions options = new AppOptions();
  private PlaylistHistory history = new PlaylistHistory(options.getMaxPlaylistHistoryEntries());

  /**
   *
   */
  public static final boolean FILE_SYSTEM_IS_CASE_SENSITIVE = File.separatorChar == '/';

  private static final Logger _logger = Logger.getLogger(GUIDriver.class);
  private static GUIDriver _instance;

  /**
   *
   * @return
   */
  public static GUIDriver getInstance()
  {
    if (_instance == null)
    {
      _instance = new GUIDriver();
    }
    return _instance;
  }

  private GUIDriver()
  {
    try
    {
      if (IniFileConverter.conversionRequired())
      {
        (new IniFileConverter()).convert();
      }
      (new FileWriter()).writeDefaultIniFilesIfNeeded();

      options = OptionsReader.read();
      IniFileReader initReader = new IniFileReader(options);
      initReader.readIni();
      mediaDir = initReader.getMediaDirs();
      history = new PlaylistHistory(options.getMaxPlaylistHistoryEntries());
      history.initHistory(initReader.getHistory());
      mediaLibraryDirectoryList = initReader.getMediaLibraryDirectories();
      mediaLibraryFileList = initReader.getMediaLibraryFiles();

      for (String dir : mediaDir)
      {
        if (!new File(dir).exists())
        {
          this.removeMediaDir(dir);
        }
      }

      if (!hasAddedMediaDirectory())
      {
        showMediaDirWindow = true;
      }
    }
    catch (Exception e)
    {
      showMediaDirWindow = true;

      // This happens by design the first time the app is executed, so to minimize confusion, we disable console logging when we distrubte listfix
       _logger.error(ExStack.toString(e));
    }
  }

  /**
   *
   * @return
   */
  public AppOptions getAppOptions()
  {
    return options;
  }

  /**
   *
   * @param opts
   */
  public void setAppOptions(AppOptions opts)
  {
    options = opts;
  }

  /**
   *
   * @return
   */
  public boolean hasAddedMediaDirectory()
  {
    return mediaDir.length != 0;
  }

  /**
   *
   * @return
   */
  public String[] getMediaDirs()
  {
    if (mediaDir.length > 0)
    {
      return mediaDir;
    }
    return new String[] {"Please Add A Media Directory..."};
  }

  /**
   *
   * @param value
   */
  public void setMediaDirs(String[] value)
  {
    mediaDir = value;
  }

  /**
   *
   * @return
   */
  public String[] getMediaLibraryDirectoryList()
  {
    return mediaLibraryDirectoryList;
  }

  /**
   *
   * @param value
   */
  public void setMediaLibraryDirectoryList(String[] value)
  {
    mediaLibraryDirectoryList = value;
  }

  /**
   *
   * @return
   */
  public String[] getMediaLibraryFileList()
  {
    return mediaLibraryFileList;
  }

  /**
   *
   * @param value
   */
  public void setMediaLibraryFileList(String[] value)
  {
    mediaLibraryFileList = value;
  }

  /**
   *
   * @return
   */
  public boolean getShowMediaDirWindow()
  {
    return showMediaDirWindow;
  }

  /**
   *
   * @return
   */
  public PlaylistHistory getHistory()
  {
    return history;
  }

  /**
   *
   */
  public void clearM3UHistory()
  {
    history.clearHistory();
    (new FileWriter()).writeMruPlaylists(history);
  }

  /**
   *
   * @return
   */
  public String[] getRecentM3Us()
  {
    return history.getFilenames();
  }

  /**
   *
   * @param dir
   * @return
   * @throws MediaDirNotFoundException
   */
  public String[] removeMediaDir(String dir) throws MediaDirNotFoundException
  {
    boolean found = false;
    int i = 0;
    int length = mediaDir.length;
    if (mediaDir.length == 1)
    {
      while ((i < length) && (!found))
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
        (new FileWriter()).writeMediaLibrary(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList);
      }
      else
      {
        throw new MediaDirNotFoundException(dir + " was not in the media directory list.");
      }
      return mediaDir;
    }
    else
    {
      while ((i < length) && (!found))
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
        List<String> mldVector = new ArrayList<>(Arrays.asList(mediaLibraryDirectoryList));
        List<String> toRemove = new ArrayList<>();
        for (String toTest : mldVector)
        {
          if (toTest.startsWith(dir))
          {
            toRemove.add(toTest);
          }
        }
        mldVector.removeAll(toRemove);
        mediaLibraryDirectoryList = mldVector.toArray(new String[mldVector.size()]);

        // Clear this out for the next run.
        toRemove.clear();

        List<String> mlfVector = new ArrayList<>(Arrays.asList(mediaLibraryFileList));
        for (String toTest : mlfVector)
        {
          if (toTest.startsWith(dir))
          {
            toRemove.add(toTest);
          }
        }
        mlfVector.removeAll(toRemove);
        mediaLibraryFileList = mlfVector.toArray(new String[mlfVector.size()]);
        (new FileWriter()).writeMediaLibrary(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList);
      }
      else
      {
        throw new MediaDirNotFoundException(dir + " was not in the media directory list.");
      }
      return mediaDir;
    }
  }

  /**
   *
   */
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

  /**
   *
   */
  public void switchMediaLibraryToMappedDrives()
  {
    if (mediaDir != null)
    {
      for (int i = 0; i < mediaDir.length; i++)
      {
        UNCFile file = new UNCFile(mediaDir[i]);
        if (file.onNetworkDrive())
        {
          mediaDir[i] = file.getDrivePath();
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
          mediaLibraryDirectoryList[i] = file.getDrivePath();
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
          mediaLibraryFileList[i] = file.getDrivePath();
        }

      }
    }
  }
}
