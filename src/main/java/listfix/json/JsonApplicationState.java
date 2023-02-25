package listfix.json;

import listfix.config.IApplicationState;

import java.util.TreeMap;
import java.util.TreeSet;

public class JsonApplicationState implements IApplicationState
{
  /**
   * Keep track of playlist opened
   */
  private final TreeSet<String> playlistsOpened = new TreeSet<>();

  /**
   * Keep track of frame positions
   */

  private final TreeMap<String, JsonFrameSettings> framePositions = new TreeMap<>();

  @Override
  public TreeSet<String> getPlaylistsOpened()
  {
    return playlistsOpened;
  }

  @Override
  public TreeMap<String, JsonFrameSettings> getFramePositions()
  {
    return this.framePositions;
  }
}
