package listfix.io.playlists;

import listfix.io.IPlaylistOptions;

import java.nio.charset.Charset;
import java.nio.file.Path;

public abstract class PlaylistReader implements IPlaylistReader
{
  protected final IPlaylistOptions playListOptions;
  protected final Path playlistPath;
  protected Charset encoding;

  public PlaylistReader(IPlaylistOptions playListOptions, Path playlistPath) {
    this.playListOptions = playListOptions;
    this.playlistPath = playlistPath;
  }

  public Path getPath()
  {
    return this.playlistPath;
  }

  @Override
  public Charset getEncoding()
  {
    return this.encoding;
  }
}
