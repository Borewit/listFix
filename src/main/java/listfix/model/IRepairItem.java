package listfix.model;

import listfix.model.playlists.Playlist;
import listfix.view.support.IProgressObserver;

public interface IRepairItem {
  void repair(BatchRepairItem item, Playlist list, IProgressObserver<String> progressObserver);
}
