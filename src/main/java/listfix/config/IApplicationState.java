package listfix.config;

import listfix.json.JsonFrameSettings;

import java.util.TreeMap;
import java.util.TreeSet;

public interface IApplicationState
{
  TreeSet<String> getPlaylistsOpened();

  TreeMap<String, JsonFrameSettings> getFramePositions();
}
