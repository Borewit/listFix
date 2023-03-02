

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
  private final int _score;

  public PotentialPlaylistEntryMatch(Path track, int score, Path playlist, IPlaylistOptions playlistOptions)
  {
    this.thisEntry = new FilePlaylistEntry(track, playlist);
    this.playlistOptions = playlistOptions;
    this.thisEntry.setFixed(true);
    this._score = score;
  }

  public int getScore()
  {
    return _score;
  }

  public PlaylistEntry getPlaylistFile()
  {
    return thisEntry;
  }

  public IPlaylistOptions getPlaylistOptions()
  {
    return playlistOptions;
  }

  /**
   * Used to render value in combo-box
   * @return Matching score & track-file-name
   */
  @Override
  public String toString() {
    return String.format("%d: %s", this._score, thisEntry.getTrackFileName());
  }
}
