package listfix.io.playlists;

import listfix.io.IPlaylistOptions;

import java.nio.file.Path;

public abstract class PlaylistReader implements IPlaylistReader
{
  protected final IPlaylistOptions playListOptions;

  protected final Path playlistPath;

  public PlaylistReader(IPlaylistOptions playListOptions, Path playlistPath) {
    this.playListOptions = playListOptions;
    this.playlistPath = playlistPath;
  }

  public Path getPath()
  {
    return this.playlistPath;
  }

}
