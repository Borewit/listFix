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

package listfix.model;

import java.awt.Font;
import java.io.File;
import javax.swing.UIManager;

import listfix.io.Constants;
import listfix.util.OperatingSystem;

/**
 *
 * @author jcaron
 */
public class AppOptions
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
	private Font appFont = new Font("SansSerif", 0, 11);
	private int maxClosestResults = 20;
	private String ignoredSmallWords = "an, and, dsp, in, my, of, the, to";
	private boolean caseInsensitiveExactMatching = !Constants.FILE_SYSTEM_IS_CASE_SENSITIVE;
	
	// Define option constants
	public static final String SAVE_RELATIVE_REFERENCES = "SAVE_RELATIVE_REFERENCES";
	public static final String AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD = "AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD";
	public static final String MAX_PLAYLIST_HISTORY_SIZE = "MAX_PLAYLIST_HISTORY_SIZE";
	public static final String AUTO_REFRESH_MEDIA_LIBRARY_ON_LOAD = "AUTO_REFRESH_MEDIA_LIBRARY_ON_LOAD";
	public static final String LOOK_AND_FEEL = "LOOK_AND_FEEL";
	public static final String ALWAYS_USE_UNC_PATHS = "ALWAYS_USE_UNC_PATHS";
	public static final String PLAYLISTS_DIRECTORY = "PLAYLISTS_DIRECTORY";
	public static final String APP_FONT = "APP_FONT";
	public static final String MAX_CLOSEST_RESULTS = "MAX_CLOSEST_RESULTS";
	public static final String IGNORED_SMALL_WORDS = "IGNORED_SMALL_WORDS";
	public static final String CASE_INSENSITIVE_EXACT_MATCHING =  "CASE_INSENSITIVE_EXACT_MATCHING";

	/**
	 *
	 * @return
	 */
	public boolean getAutoLocateEntriesOnPlaylistLoad()
	{
		return autoLocateEntriesOnPlaylistLoad;
	}

	/**
	 *
	 * @param autoLocateEntriesOnPlaylistLoad
	 */
	public void setAutoLocateEntriesOnPlaylistLoad(boolean autoLocateEntriesOnPlaylistLoad)
	{
		this.autoLocateEntriesOnPlaylistLoad = autoLocateEntriesOnPlaylistLoad;
	}

	/**
	 *
	 * @return
	 */
	public int getMaxPlaylistHistoryEntries()
	{
		return maxPlaylistHistoryEntries;
	}

	/**
	 *
	 * @param lookAndFeel
	 */
	public void setLookAndFeel(String lookAndFeel)
	{
		this.lookAndFeel = lookAndFeel;
	}

	/**
	 *
	 * @return
	 */
	public String getLookAndFeel()
	{
		return lookAndFeel;
	}

	/**
	 *
	 * @param maxPlaylistHistoryEntries
	 */
	public void setMaxPlaylistHistoryEntries(int maxPlaylistHistoryEntries)
	{
		this.maxPlaylistHistoryEntries = maxPlaylistHistoryEntries;
	}

	/**
	 *
	 * @param autoRefreshMediaLibraryOnStartup
	 */
	public void setAutoRefreshMediaLibraryOnStartup(boolean autoRefreshMediaLibraryOnStartup)
	{
		this.autoRefreshMediaLibraryOnStartup = autoRefreshMediaLibraryOnStartup;
	}

	/**
	 *
	 * @return
	 */
	public boolean getAutoRefreshMediaLibraryOnStartup()
	{
		return autoRefreshMediaLibraryOnStartup;
	}

	/**
	 *
	 * @return
	 */
	public boolean getSavePlaylistsWithRelativePaths()
	{
		return savePlaylistsWithRelativePaths;
	}

	/**
	 *
	 * @param savePlaylistsWithRelativePaths
	 */
	public void setSavePlaylistsWithRelativePaths(boolean savePlaylistsWithRelativePaths)
	{
		this.savePlaylistsWithRelativePaths = savePlaylistsWithRelativePaths;
	}

	/**
	 *
	 * @return
	 */
	public boolean getAlwaysUseUNCPaths()
	{
		return alwaysUseUNCPaths;
	}

	/**
	 *
	 * @param alwaysUseUNCPaths
	 */
	public void setAlwaysUseUNCPaths(boolean alwaysUseUNCPaths)
	{
		this.alwaysUseUNCPaths = alwaysUseUNCPaths;
	}

	/**
	 *
	 * @return
	 */
	public String getPlaylistsDirectory()
	{
		return playlistsDirectory;
	}

	/**
	 *
	 * @param playlistsDirectory
	 */
	public void setPlaylistsDirectory(String playlistsDirectory)
	{
		if (new File(playlistsDirectory).exists())
		{
			this.playlistsDirectory = playlistsDirectory;
		}
	}

	/**
	 * @return the appFont
	 */ 
	public Font getAppFont()
	{
		return appFont;
	}

	/**
	 * @param appFont the appFont to set
	 */ 
	public void setAppFont(Font appFont)
	{
		this.appFont = appFont;
	}

	/**
	 * @return the maxClosestResults
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
	 * @return the ignoredSmallWords
	 */
	public String getIgnoredSmallWords()
	{
		return ignoredSmallWords;
	}

	/**
	 * @param ignoredSmallWords the ignoredSmallWords to set
	 */
	public void setIgnoredSmallWords(String ignoredSmallWords)
	{
		this.ignoredSmallWords = ignoredSmallWords;
	}

	/**
	 * @return the caseInsensitiveExactMatching
	 */
	public boolean getCaseInsensitiveExactMatching()
	{
		return caseInsensitiveExactMatching;
	}

	/**
	 * @param caseInsensitiveExactMatching the caseInsensitiveExactMatching to set
	 */
	public void setCaseInsensitiveExactMatching(boolean caseInsensitiveExactMatching)
	{
		this.caseInsensitiveExactMatching = caseInsensitiveExactMatching;
	}
}
