package listfix.config;

import listfix.io.IPlayListOptions;

import java.awt.*;

public interface IAppOptions extends IPlayListOptions
{
  boolean getAutoLocateEntriesOnPlaylistLoad();

  int getMaxPlaylistHistoryEntries();

  String getLookAndFeel();
  boolean getAutoRefreshMediaLibraryOnStartup();

  String getPlaylistsDirectory();

  /**
   * @return the appFont
   */
  Font getAppFont();
}
