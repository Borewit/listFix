

package listfix.controller.tasks;

import listfix.config.MediaLibraryConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * @author jcaron
 */
public class WriteMediaLibraryIniTask extends listfix.controller.Task
{
  private static final Logger _logger = LogManager.getLogger(WriteMediaLibraryIniTask.class);

  private final MediaLibraryConfiguration mediaLibraryConfiguration;

  public WriteMediaLibraryIniTask(MediaLibraryConfiguration mediaLibraryConfiguration)
  {
    this.mediaLibraryConfiguration = mediaLibraryConfiguration;
  }

  /**
   * Run the task. This method is the body of the thread for this task.
   */
  @Override
  public void run()
  {
    try
    {
      this.mediaLibraryConfiguration.write();
    }
    catch (IOException e)
    {
      _logger.error("Error writing configuration to " + this.mediaLibraryConfiguration.getFile().getName(), e);
    }
  }
}
