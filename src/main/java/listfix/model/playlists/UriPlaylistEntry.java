package listfix.model.playlists;

import io.github.borewit.lizzy.playlist.Media;
import listfix.model.enums.PlaylistEntryStatus;

import java.net.URI;
import java.net.URISyntaxException;

public class UriPlaylistEntry extends PlaylistEntry
{
  // The entry's URI (for URLs).
  private final URI uri;

  public UriPlaylistEntry(Playlist playlist, Media media)
  {
    super(playlist, media);
    try
    {
      this.uri = media.getSource().getURI();
    }
    catch (URISyntaxException e)
    {
      throw new RuntimeException(e);
    }
  }

  public URI getURI()
  {
    return this.uri;
  }

  @Override
  public UriPlaylistEntry clone()
  {
    UriPlaylistEntry urlPlayListEntry = new UriPlaylistEntry(this.playlist, this.media);
    this.copyTo(urlPlayListEntry);
    return urlPlayListEntry;
  }

  @Override
  protected boolean exists()
  {
    return false;
  }

  @Override
  public void recheckFoundStatus()
  {
    _status = PlaylistEntryStatus.Missing;
    _isFixed = false;
  }

  @Override
  public String getTrackFolder()
  {
    return "";
  }

  @Override
  public String getTrackFileName()
  {
    return this.uri.toString();
  }

  @Override
  public boolean isURL()
  {
    return true;
  }

  @Override
  public boolean isRelative()
  {
    return false;
  }

  @Override
  public boolean equals(Object other)
  {
    return other instanceof UriPlaylistEntry &&
      this.uri.equals(((UriPlaylistEntry) other).uri);
  }

  @Override
  public int hashCode()
  {
    return this.uri.hashCode();
  }
}
