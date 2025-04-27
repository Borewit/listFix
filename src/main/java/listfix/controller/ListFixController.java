package listfix.controller;

import java.io.File;
import java.io.IOException;
import listfix.config.*;
import listfix.json.JsonAppOptions;
import listfix.model.PlaylistHistory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ListFixController implements IApplicationConfiguration {
  private boolean showMediaDirWindow;
  private MediaLibraryConfiguration mediaLibraryConfiguration;
  private ApplicationOptionsConfiguration applicationOptionsConfiguration;
  private MissingFileCacheConfiguration missingFileCacheConfiguration;
  private PlaylistHistory history;

  public static final boolean FILE_SYSTEM_IS_CASE_SENSITIVE = File.separatorChar == '/';

  private final Logger logger = LogManager.getLogger(ListFixController.class);
  private static ListFixController _instance;

  /** Thread safe access to singleton */
  public static synchronized ListFixController getInstance() {
    if (_instance == null) {
      _instance = new ListFixController();
    }
    return _instance;
  }

  private ListFixController() {
    try {
      // Load / initialize application configuration
      this.mediaLibraryConfiguration = MediaLibraryConfiguration.load();
      this.applicationOptionsConfiguration = ApplicationOptionsConfiguration.load();
      this.missingFileCacheConfiguration = MissingFileCacheConfiguration.load();

      this.history = new PlaylistHistory(
          this.applicationOptionsConfiguration.getConfig().getMaxPlaylistHistoryEntries());
      this.history.load();

      mediaLibraryConfiguration.cleanNonExistingMediaDirectories();

      showMediaDirWindow = this.getMediaLibrary().getNestedDirectories().isEmpty();
    } catch (Exception e) {
      showMediaDirWindow = true;

      // This happens by design the first time the app is executed, so to minimize
      // confusion, we
      // disable console logging when we distribute listFix()
      this.logger.error("Error initializing", e);
    }
  }

  public ApplicationOptionsConfiguration getApplicationConfiguration() {
    return this.applicationOptionsConfiguration;
  }

  @Override
  public IAppOptions getAppOptions() {
    return this.applicationOptionsConfiguration.getConfig();
  }

  public void setAppOptions(JsonAppOptions opts) {
    this.applicationOptionsConfiguration.setConfig(opts);
  }

  @Override
  public IMediaLibrary getMediaLibrary() {
    return this.mediaLibraryConfiguration.getConfig();
  }

  @Override
  public IApplicationState getApplicationState() {
    return this.applicationOptionsConfiguration.getConfig().getApplicationState();
  }

  public MediaLibraryConfiguration getMediaLibraryConfiguration() {
    return this.mediaLibraryConfiguration;
  }

  public boolean getShowMediaDirWindow() {
    return this.showMediaDirWindow;
  }

  public PlaylistHistory getHistory() {
    return history;
  }

  public void clearM3UHistory() throws IOException {
    this.history.clearHistory();
    this.history.write();
  }

  public String[] getRecentM3Us() {
    return history.getFilenames();
  }

  public void switchMediaLibraryToMappedDrives() {
  }

  public MissingFileCacheConfiguration getMissingFileCacheConfiguration() {
    return missingFileCacheConfiguration;
  }
}
