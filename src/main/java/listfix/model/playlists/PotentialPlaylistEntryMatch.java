package listfix.model.playlists;

import java.nio.file.Path;

/**
 * Serves to model the closest match on a single playlist entry.
 *
 * @author jcaron
 */

public class PotentialPlaylistEntryMatch
{
  private final Path track;
  private final String trackFolder;
  private final String trackFilename;
  private final int _score;

  public PotentialPlaylistEntryMatch(Path track, int score)
  {
    this.track = track;
    this.trackFilename = track.getFileName().toString();
    this.trackFolder = track.getParent() == null ? "" : track.getParent().toString();
    this._score = score;
  }

  public int getScore()
  {
    return _score;
  }

  public Path getTrack()
  {
    return this.track;
  }

  /**
   * Used to render value in combo-box
   *
   * @return Matching score & track-file-name
   */
  @Override
  public String toString()
  {
    return String.format("%d: %s", this._score, trackFilename);
  }

  public String getTrackFolder()
  {
    return this.trackFolder;
  }
}
