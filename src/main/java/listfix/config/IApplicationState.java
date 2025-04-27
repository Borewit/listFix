package listfix.config;

import java.util.List;
import java.util.TreeMap;
import listfix.json.JsonFrameSettings;

public interface IApplicationState {
  List<String> getPlaylistsOpened();

  TreeMap<String, JsonFrameSettings> getFramePositions();

  Integer getActivePlaylistIndex();

  void setActivePlaylistIndex(Integer activePlaylistIndex);
}
