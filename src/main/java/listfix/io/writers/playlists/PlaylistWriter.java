package listfix.io.writers.playlists;

import listfix.io.IPlayListOptions;

public abstract class PlaylistWriter implements IPlaylistWriter
{
  protected final IPlayListOptions playListOptions;

  public PlaylistWriter(IPlayListOptions playListOptions)
  {
    this.playListOptions = playListOptions;
  }
}
