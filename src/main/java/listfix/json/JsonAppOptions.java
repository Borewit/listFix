package listfix.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jgoodies.looks.windows.WindowsLookAndFeel;
import java.awt.*;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.*;
import listfix.config.IAppOptions;
import listfix.io.Constants;
import listfix.io.IPlaylistOptions;
import listfix.util.OperatingSystem;

public class JsonAppOptions implements IPlaylistOptions, IAppOptions
{
  // Init default values.
  private boolean savePlaylistsWithRelativePaths = false;
  private boolean autoLocateEntriesOnPlaylistLoad = false;
  private boolean autoRefreshMediaLibraryOnStartup = false;
  private boolean alwaysUseUNCPaths = false;
  private int maxPlaylistHistoryEntries = 5;
  private String lookAndFeel = OperatingSystem.isWindows() ? WindowsLookAndFeel.class.getName() : UIManager.getSystemLookAndFeelClassName();

  @Deprecated // Replaced by playlistDirectories
  private String playlistsDirectory;

  private final TreeSet<String> playlistDirectories;
  @JsonSerialize(using = JsonFontSerializer.class)
  @JsonDeserialize(using = JsonFontDeserializer.class)
  private Font appFont = new Font("SansSerif", Font.PLAIN, 11);
  private int maxClosestResults = 20;
  private String ignoredSmallWords = "an, and, dsp, in, my, of, the, to";
  private boolean caseInsensitiveExactMatching = !Constants.FILE_SYSTEM_IS_CASE_SENSITIVE;

  public JsonAppOptions() {
    this.playlistDirectories = new TreeSet<>();
  }

  @Override public boolean getAutoLocateEntriesOnPlaylistLoad()
  {
    return autoLocateEntriesOnPlaylistLoad;
  }

  public void setAutoLocateEntriesOnPlaylistLoad(boolean autoLocateEntriesOnPlaylistLoad)
  {
    this.autoLocateEntriesOnPlaylistLoad = autoLocateEntriesOnPlaylistLoad;
  }

  @Override public int getMaxPlaylistHistoryEntries()
  {
    return maxPlaylistHistoryEntries;
  }

  public void setLookAndFeel(String lookAndFeel)
  {
    this.lookAndFeel = lookAndFeel;
  }

  @Override public String getLookAndFeel()
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

  @Override public boolean getAutoRefreshMediaLibraryOnStartup()
  {
    return autoRefreshMediaLibraryOnStartup;
  }

  @Override public boolean getSavePlaylistsWithRelativePaths()
  {
    return savePlaylistsWithRelativePaths;
  }

  public void setSavePlaylistsWithRelativePaths(boolean savePlaylistsWithRelativePaths)
  {
    this.savePlaylistsWithRelativePaths = savePlaylistsWithRelativePaths;
  }

  @Override public boolean getAlwaysUseUNCPaths()
  {
    return alwaysUseUNCPaths;
  }

  public void setAlwaysUseUNCPaths(boolean alwaysUseUNCPaths)
  {
    this.alwaysUseUNCPaths = alwaysUseUNCPaths;
  }


  @Override
  public String getPlaylistsDirectory()
  {
    return this.playlistsDirectory;
  }

  public void setPlaylistsDirectory(String playlistsDirectory)
  {
    this.playlistsDirectory = playlistsDirectory;
  }

  @Override
  public Set<String> getPlaylistDirectories() {
    return this.playlistDirectories;
  }

  /**
   * @return The appFont
   */
  @Override public Font getAppFont()
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
  @Override public int getMaxClosestResults()
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
  @Override public String getIgnoredSmallWords()
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
  @Override public boolean getCaseInsensitiveExactMatching()
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
