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
import java.io.IOException;
import java.util.Set;

import listfix.config.ApplicationOptionsConfiguration;
import listfix.config.MediaLibraryConfiguration;
import listfix.json.JsonAppOptions;
import listfix.model.PlaylistHistory;

import listfix.util.ExStack;

import org.apache.log4j.Logger;

/**
 * @author jcaron
 */
public final class GUIDriver
{
  private boolean showMediaDirWindow = false;
  private MediaLibraryConfiguration mediaLibraryConfiguration;
  private ApplicationOptionsConfiguration applicationOptionsConfiguration;
  private PlaylistHistory history;

  /**
   *
   */
  public static final boolean FILE_SYSTEM_IS_CASE_SENSITIVE = File.separatorChar == '/';

  private static final Logger _logger = Logger.getLogger(GUIDriver.class);
  private static GUIDriver _instance;

  /**
   * @return
   */
  public static GUIDriver getInstance()
  {
    if (_instance == null) {
      _instance = new GUIDriver();
    }
    return _instance;
  }

  private GUIDriver()
  {
    try {
      // Load / initialize application configuration
      this.mediaLibraryConfiguration = MediaLibraryConfiguration.load();
      this.applicationOptionsConfiguration = ApplicationOptionsConfiguration.load();

      this.history = new PlaylistHistory(this.applicationOptionsConfiguration.getConfig().getMaxPlaylistHistoryEntries());
      this.history.load();

      mediaLibraryConfiguration.cleanNonExistingMediaDirectories();

      if (!hasAddedMediaDirectory()) {
        showMediaDirWindow = true;
      }
    } catch (Exception e) {
      showMediaDirWindow = true;

      // This happens by design the first time the app is executed, so to minimize confusion, we disable console logging when we distrubte listfix
      _logger.error(ExStack.toString(e));
    }
  }

  /**
   * @return
   */
  public ApplicationOptionsConfiguration getApplicationConfiguration()
  {
    return this.applicationOptionsConfiguration;
  }

  public JsonAppOptions getOptions()
  {
    return this.applicationOptionsConfiguration.getConfig();
  }

  /**
   * @param opts
   */
  public void setAppOptions(JsonAppOptions opts)
  {
    this.applicationOptionsConfiguration.setConfig(opts);
  }

  /**
   * @return
   */
  public boolean hasAddedMediaDirectory()
  {
    return !this.mediaLibraryConfiguration.getConfig().getMediaDirs().isEmpty();
  }

  /**
   * @return
   */
  public String[] getMediaDirs()
  {
    Set<String> mediaDir = this.mediaLibraryConfiguration.getConfig().getMediaDirs();
    if (mediaDir.isEmpty()) {
      return new String[]{"Please Add A Media Directory..."};
    }
    return mediaDir.toArray(new String[0]);
  }

  public MediaLibraryConfiguration getMediaLibrarConfiguration()
  {
    return this.mediaLibraryConfiguration;
  }

  /**
   * @return
   */
  public Set<String> getMediaLibraryFileList()
  {
    return this.mediaLibraryConfiguration.getConfig().getMediaLibraryFiles();
  }

  /**
   * @return
   */
  public boolean getShowMediaDirWindow()
  {
    return showMediaDirWindow;
  }

  /**
   * @return
   */
  public PlaylistHistory getHistory()
  {
    return history;
  }

  /**
   *
   */
  public void clearM3UHistory() throws IOException
  {
    this.history.clearHistory();
    this.history.write();
  }

  /**
   * @return
   */
  public String[] getRecentM3Us()
  {
    return history.getFilenames();
  }

  /**
   *
   */
  public void switchMediaLibraryToMappedDrives()
  {

  }
}
