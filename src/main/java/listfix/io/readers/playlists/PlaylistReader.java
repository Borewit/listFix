package listfix.io.readers.playlists;

import listfix.io.IPlayListOptions;

import java.io.File;

public abstract class PlaylistReader implements IPlaylistReader
{
  protected final IPlayListOptions playListOptions;

  protected final File playlistFile;

  public PlaylistReader(IPlayListOptions playListOptions, File playlistFile) {
    this.playListOptions = playListOptions;
    this.playlistFile = playlistFile;
  }

}
