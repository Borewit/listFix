package listfix.model.playlists;

import listfix.model.enums.PlaylistEntryStatus;

import java.net.URI;

public class UriPlaylistEntry extends PlaylistEntry
{
  // The entry's URI (for URLs).
  private final URI uri;

  /**
   * Copy constructor for a URI entry
   */
  public UriPlaylistEntry(URI uri, String title, long length, Playlist list)
  {
    this(uri);
    _length = length;
    _extInf = "#EXTINF:" + convertDurationToSeconds(length) + "," + title;
    _playlist = list;
  }

  /**
   * Construct a PLS/XSPF URL entry.
   */
  public UriPlaylistEntry(URI uri, String title, long length)
  {
    this(uri);
    _title = title;
    _length = length;
    _extInf = "#EXTINF:" + convertDurationToSeconds(length) + "," + title;
  }

  /**
   * Construct a WPL URL entry
   */
  public UriPlaylistEntry(URI uri, String extra, String cid, String tid)
  {
    this(uri, extra);
    _cid = cid;
    _tid = tid;
  }

  public UriPlaylistEntry(URI uri, String extra)
  {
    this(uri);
    this.parseExtraInfo(extra);
    _extInf = extra;
  }
  protected UriPlaylistEntry(URI uri)
  {
    this.uri = uri;
  }

  public URI getURI()
  {
    return this.uri;
  }

  @Override
  public UriPlaylistEntry clone()
  {
    UriPlaylistEntry urlPlayListEntry = new UriPlaylistEntry(this.uri);
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

  public String trackPathToString() {
    return this.uri.toString();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof UriPlaylistEntry &&
      this.uri.equals(((UriPlaylistEntry)other).uri);
  }

  @Override
  public int hashCode() {
    return this.uri.hashCode();
  }
}
