package listfix.model;

import listfix.model.playlists.Playlist;
import listfix.view.support.ProgressAdapter;

public interface IRepairItem
{

  void repair(BatchRepairItem item, Playlist list, ProgressAdapter<String> task);
}
