

package listfix.model.playlists.itunes;

import listfix.model.playlists.FilePlaylistEntry;

import java.nio.file.Path;


public class ITunesFilePlaylistEntry extends FilePlaylistEntry implements  IITunesPlaylistEntry
{
  private final ITunesTrack _track;

  public ITunesFilePlaylistEntry(Path input, String title, long length, Path playlistPath, ITunesTrack track)
  {
    super(input, title, length, playlistPath);
    _track = track;
  }

  /**
   * @return the _track
   */
  public ITunesTrack getTrack()
  {
    return _track;
  }

  /**
   *
   * @return
   */
  @Override
  public ITunesFilePlaylistEntry clone()
  {
    ITunesFilePlaylistEntry clone = new ITunesFilePlaylistEntry(this.trackPath, this._title, this._length, this.playlistPath, _track);
    super.copyTo(clone);
    return clone;
  }
}
