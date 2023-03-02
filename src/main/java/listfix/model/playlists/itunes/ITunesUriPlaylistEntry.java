

package listfix.model.playlists.itunes;

import listfix.model.playlists.UriPlaylistEntry;

import java.net.URI;


public class ITunesUriPlaylistEntry extends UriPlaylistEntry implements IITunesPlaylistEntry
{
  private final ITunesTrack _track;

  public ITunesUriPlaylistEntry(URI input, ITunesTrack track)
  {
    super(input, "");
    _track = track;
  }

  /**
   * @return the _track
   */
  @Override public ITunesTrack getTrack()
  {
    return _track;
  }

  /**
   *
   * 
   */
  @Override
  public ITunesUriPlaylistEntry clone()
  {
    ITunesUriPlaylistEntry clone = new ITunesUriPlaylistEntry(this.getURI(), this._track);
    super.copyTo(clone);
    return clone;
  }
}
