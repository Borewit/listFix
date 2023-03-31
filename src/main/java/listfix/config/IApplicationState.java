package listfix.config;

import listfix.json.JsonFrameSettings;

import java.util.List;
import java.util.TreeMap;

public interface IApplicationState
{
  List<String> getPlaylistsOpened();

  TreeMap<String, JsonFrameSettings> getFramePositions();
}
