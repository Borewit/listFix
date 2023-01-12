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

import listfix.config.MediaLibraryConfiguration;
import listfix.io.*;
import listfix.util.*;
import listfix.view.support.ProgressWorker;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author jcaron
 */
public class MediaLibraryOperator
{
  private static final Logger _logger = Logger.getLogger(MediaLibraryOperator.class);
  private GUIDriver guiDriver;
  private MediaLibraryConfiguration mediaLibraryConfiguration;
  private String[] mediaDirs;
  private ProgressWorker _observer;

  /**
   * @param observer
   */
  public MediaLibraryOperator(ProgressWorker observer)
  {
    _observer = observer;
    guiDriver = GUIDriver.getInstance();
  }

  /**
   * @param dir
   */
  public void addDirectory(String dir)
  {
    final Set<String> mediaDir = this.mediaLibraryConfiguration.getConfig().getMediaDirs();
    mediaDir.add(dir);
    DirectoryScanner ds = new DirectoryScanner();
    ds.createMediaLibraryDirectoryAndFileList(mediaDir, _observer);
    if (!_observer.getCancelled())
    {
      _observer.setMessage("Finishing...");
      this.mediaLibraryConfiguration.getConfig().setMediaLibraryDirectories(new TreeSet<>(ds.getDirectoryList()));
      this.mediaLibraryConfiguration.getConfig().setMediaLibraryFiles(new TreeSet<>(ds.getFileList()));
      ds.reset();
      try
      {
        this.mediaLibraryConfiguration.write();
      }
      catch (IOException e)
      {
        _logger.error(ExStack.toString(e));
      }
    }
  }

  /**
   *
   */
  public void refresh()
  {
    mediaDirs = guiDriver.getMediaDirs();
    if (mediaDirs != null)
    {
      DirectoryScanner ds = new DirectoryScanner();
      ds.createMediaLibraryDirectoryAndFileList(this.mediaLibraryConfiguration.getConfig().getMediaDirs(), _observer);
      if (!_observer.getCancelled())
      {
        _observer.setMessage("Finishing...");
        ds.reset();
        try
        {
          this.mediaLibraryConfiguration.write();
        }
        catch (IOException e)
        {
          _logger.error(ExStack.toString(e));
        }
      }
    }
  }
}
