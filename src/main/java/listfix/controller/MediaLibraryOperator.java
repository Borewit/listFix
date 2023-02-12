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
import listfix.io.DirectoryScanner;
import listfix.view.support.ProgressWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * @author jcaron
 */
public class MediaLibraryOperator
{
  private static final Logger _logger = LogManager.getLogger(MediaLibraryOperator.class);
  private final MediaLibraryConfiguration mediaLibraryConfiguration;
  private final ProgressWorker _observer;

  public MediaLibraryOperator(ProgressWorker observer)
  {
    this._observer = observer;
    this.mediaLibraryConfiguration = ListFixController.getInstance().getMediaLibraryConfiguration();
  }

  public void addDirectory(String dir)
  {
    final Set<String> mediaDir = this.mediaLibraryConfiguration.getConfig().getDirectories();
    mediaDir.add(dir);
    DirectoryScanner ds = new DirectoryScanner();
    ds.createMediaLibraryDirectoryAndFileList(mediaDir, _observer);
    if (!_observer.getCancelled())
    {
      _observer.setMessage("Finishing...");
      replaceSetValues(this.mediaLibraryConfiguration.getConfig().getNestedDirectories(), ds.getDirectoryList());
      replaceSetValues(this.mediaLibraryConfiguration.getConfig().getNestedMediaFiles(), ds.getFileList());
      ds.reset();
      try
      {
        this.mediaLibraryConfiguration.write();
      }
      catch (IOException e)
      {
        _logger.error("Error", e);
      }
    }
  }

  private static void replaceSetValues(Set<String> set, Collection<String> newValues) {
    set.clear();
    set.addAll(newValues);
  }

  /**
   *
   */
  public void refresh()
  {
    DirectoryScanner ds = new DirectoryScanner();
    ds.createMediaLibraryDirectoryAndFileList(this.mediaLibraryConfiguration.getConfig().getDirectories(), _observer);
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
        _logger.error("Error", e);
      }
    }
  }
}
