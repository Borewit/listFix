

package listfix.model.playlists;

import listfix.io.IPlaylistOptions;

import java.nio.file.Path;

/**
 * Serves to model a closest match on a single playlist entry.
 * @author jcaron
 */

public class PotentialPlaylistEntryMatch
{
  private final PlaylistEntry thisEntry;
  private final IPlaylistOptions playlistOptions;
  private int _score = 0;

  public PotentialPlaylistEntryMatch(Path track, int score, Path playlist, IPlaylistOptions playlistOptions)
  {
    this.thisEntry = new FilePlaylistEntry(track, playlist);
    this.playlistOptions = playlistOptions;
    this.thisEntry.setFixed(true);
    this._score = score;
  }

  /**
   *
   * @return
   */
  public int getScore()
  {
    return _score;
  }

  /**
   *
   * @return
   */
  public PlaylistEntry getPlaylistFile()
  {
    return thisEntry;
  }

  public IPlaylistOptions getPlaylistOptions()
  {
    return playlistOptions;
  }
}
