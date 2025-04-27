package listfix.config;

import java.awt.*;
import listfix.io.IPlaylistOptions;

public interface IAppOptions extends IPlaylistOptions {
  boolean getAutoLocateEntriesOnPlaylistLoad();

  int getMaxPlaylistHistoryEntries();

  String getLookAndFeel();

  void setLookAndFeel(String lookAndFeelClassName);

  boolean getAutoRefreshMediaLibraryOnStartup();

  /** Returns configured application font. */
  Font getAppFont();

  IApplicationState getApplicationState();
}
