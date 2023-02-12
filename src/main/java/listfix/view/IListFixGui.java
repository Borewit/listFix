package listfix.view;

import listfix.config.IApplicationConfiguration;
import java.io.File;

public interface IListFixGui
{
  void openPlaylist(File file);

  IApplicationConfiguration getApplicationConfiguration();
}
