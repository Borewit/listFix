package listfix.json;

import java.util.ArrayList;
import java.util.TreeMap;
import listfix.config.IApplicationState;

public class JsonApplicationState implements IApplicationState {
  /** Keep track of playlist opened */
  private final ArrayList<String> playlistsOpened = new ArrayList<>();

  /** Index of active playlist */
  private Integer activePlaylistIndex = null;

  /** Keep track of frame positions */
  private final TreeMap<String, JsonFrameSettings> framePositions = new TreeMap<>();

  @Override
  public ArrayList<String> getPlaylistsOpened() {
    return playlistsOpened;
  }

  @Override
  public TreeMap<String, JsonFrameSettings> getFramePositions() {
    return this.framePositions;
  }

  public Integer getActivePlaylistIndex() {
    return activePlaylistIndex;
  }

  public void setActivePlaylistIndex(Integer activePlaylistIndex) {
    this.activePlaylistIndex = activePlaylistIndex;
  }
}
