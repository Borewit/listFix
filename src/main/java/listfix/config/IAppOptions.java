package listfix.config;

import listfix.io.IPlaylistOptions;

import java.awt.*;

public interface IAppOptions extends IPlaylistOptions
{
  boolean getAutoLocateEntriesOnPlaylistLoad();

  int getMaxPlaylistHistoryEntries();

  String getLookAndFeel();

  void setLookAndFeel(String lookAndFeelClassName);

  boolean getAutoRefreshMediaLibraryOnStartup();

  String getPlaylistsDirectory();

  /**
   * Returns configured application font.
   */
  Font getAppFont();

  IApplicationState getApplicationState();
}
