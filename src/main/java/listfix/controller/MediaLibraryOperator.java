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
    final Set<String> mediaDir = this.mediaLibraryConfiguration.getConfig().getMediaDirectories();
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


  public void refresh()
  {
    DirectoryScanner ds = new DirectoryScanner();
    ds.createMediaLibraryDirectoryAndFileList(this.mediaLibraryConfiguration.getConfig().getMediaDirectories(), _observer);
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
