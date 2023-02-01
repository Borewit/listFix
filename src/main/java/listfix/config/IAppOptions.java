package listfix.config;

import listfix.io.IPlaylistOptions;

import java.awt.*;

public interface IAppOptions extends IPlaylistOptions
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
