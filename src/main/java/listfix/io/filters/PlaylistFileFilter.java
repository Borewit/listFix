package listfix.io.filters;

import listfix.model.playlists.Playlist;

import java.io.FileFilter;

/**
 * A FileFilter that accepts our currently supported playlist types, and directories.
 */
public class PlaylistFileFilter extends FileExtensionFilterBase implements FileFilter
{
  public PlaylistFileFilter()
  {
    super(Playlist.playlistExtensions);
  }

  @Override
  public String getDescription()
  {
    return "Playlists (*.m3u, *.m3u8, *.pls, *.wpl, *.xspf, *.xml)";
  }

  @Override
  public String toString()
  {
    return getDescription();
  }
}

