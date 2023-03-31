package listfix.json;

import listfix.config.IApplicationState;

import java.util.ArrayList;
import java.util.TreeMap;

public class JsonApplicationState implements IApplicationState
{
  /**
   * Keep track of playlist opened
   */
  private final ArrayList<String> playlistsOpened = new ArrayList<>();

  /**
   * Keep track of frame positions
   */

  private final TreeMap<String, JsonFrameSettings> framePositions = new TreeMap<>();

  @Override
  public ArrayList<String> getPlaylistsOpened()
  {
    return playlistsOpened;
  }

  @Override
  public TreeMap<String, JsonFrameSettings> getFramePositions()
  {
    return this.framePositions;
  }
}
