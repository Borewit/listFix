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

package listfix.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import listfix.config.IAppOptions;
import listfix.io.Constants;
import listfix.io.IPlaylistOptions;
import listfix.util.OperatingSystem;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author jcaron
 */
public class JsonAppOptions implements IPlaylistOptions, IAppOptions
{
  private static final String EMPTY_STRING = "None Selected";

  // Init default values.
  private boolean savePlaylistsWithRelativePaths = false;
  private boolean autoLocateEntriesOnPlaylistLoad = false;
  private boolean autoRefreshMediaLibraryOnStartup = false;
  private boolean alwaysUseUNCPaths = false;
  private int maxPlaylistHistoryEntries = 5;
  private String lookAndFeel = OperatingSystem.isWindows() ? com.jgoodies.looks.windows.WindowsLookAndFeel.class.getName() : UIManager.getSystemLookAndFeelClassName();
  private String playlistsDirectory = EMPTY_STRING;
  @JsonSerialize(using = JsonFontSerializer.class)
  @JsonDeserialize(using = JsonFontDeserializer.class)
  private Font appFont = new Font("SansSerif", Font.PLAIN, 11);
  private int maxClosestResults = 20;
  private String ignoredSmallWords = "an, and, dsp, in, my, of, the, to";
  private boolean caseInsensitiveExactMatching = !Constants.FILE_SYSTEM_IS_CASE_SENSITIVE;

  public boolean getAutoLocateEntriesOnPlaylistLoad()
  {
    return autoLocateEntriesOnPlaylistLoad;
  }

  public void setAutoLocateEntriesOnPlaylistLoad(boolean autoLocateEntriesOnPlaylistLoad)
  {
    this.autoLocateEntriesOnPlaylistLoad = autoLocateEntriesOnPlaylistLoad;
  }

  public int getMaxPlaylistHistoryEntries()
  {
    return maxPlaylistHistoryEntries;
  }

  public void setLookAndFeel(String lookAndFeel)
  {
    this.lookAndFeel = lookAndFeel;
  }

  public String getLookAndFeel()
  {
    return lookAndFeel;
  }

  public void setMaxPlaylistHistoryEntries(int maxPlaylistHistoryEntries)
  {
    this.maxPlaylistHistoryEntries = maxPlaylistHistoryEntries;
  }

  public void setAutoRefreshMediaLibraryOnStartup(boolean autoRefreshMediaLibraryOnStartup)
  {
    this.autoRefreshMediaLibraryOnStartup = autoRefreshMediaLibraryOnStartup;
  }

  public boolean getAutoRefreshMediaLibraryOnStartup()
  {
    return autoRefreshMediaLibraryOnStartup;
  }

  public boolean getSavePlaylistsWithRelativePaths()
  {
    return savePlaylistsWithRelativePaths;
  }

  public void setSavePlaylistsWithRelativePaths(boolean savePlaylistsWithRelativePaths)
  {
    this.savePlaylistsWithRelativePaths = savePlaylistsWithRelativePaths;
  }

  public boolean getAlwaysUseUNCPaths()
  {
    return alwaysUseUNCPaths;
  }

  public void setAlwaysUseUNCPaths(boolean alwaysUseUNCPaths)
  {
    this.alwaysUseUNCPaths = alwaysUseUNCPaths;
  }


  public String getPlaylistsDirectory()
  {
    return playlistsDirectory;
  }

  public void setPlaylistsDirectory(String playlistsDirectory)
  {
    if (new File(playlistsDirectory).exists())
    {
      this.playlistsDirectory = playlistsDirectory;
    }
  }

  /**
   * @return The appFont
   */
  public Font getAppFont()
  {
    return appFont;
  }

  /**
   * @param appFont The appFont to set
   */
  public void setAppFont(Font appFont)
  {
    this.appFont = appFont;
  }

  /**
   * @return The maxClosestResults
   */
  public int getMaxClosestResults()
  {
    return maxClosestResults;
  }

  /**
   * @param maxClosestResults the maxClosestResults to set
   */
  public void setMaxClosestResults(int maxClosestResults)
  {
    this.maxClosestResults = maxClosestResults;
  }

  /**
   * @return The ignoredSmallWords
   */
  public String getIgnoredSmallWords()
  {
    return ignoredSmallWords;
  }

  /**
   * @param ignoredSmallWords The ignoredSmallWords to set
   */
  public void setIgnoredSmallWords(String ignoredSmallWords)
  {
    this.ignoredSmallWords = ignoredSmallWords;
  }

  /**
   * @return The caseInsensitiveExactMatching
   */
  public boolean getCaseInsensitiveExactMatching()
  {
    return caseInsensitiveExactMatching;
  }

  /**
   * @param caseInsensitiveExactMatching The caseInsensitiveExactMatching to set
   */
  public void setCaseInsensitiveExactMatching(boolean caseInsensitiveExactMatching)
  {
    this.caseInsensitiveExactMatching = caseInsensitiveExactMatching;
  }
}
