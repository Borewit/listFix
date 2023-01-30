package listfix.json;

// [Recent Playlists]

import java.util.ArrayList;
import java.util.List;

public class JsonPlayListHistory
{
  public List<String> recentPlaylists = new ArrayList();

  public List<String> getRecentPlaylists()
  {
    return this.recentPlaylists;
  }
}
