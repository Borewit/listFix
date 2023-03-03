package listfix.io.filters;

import listfix.model.playlists.Playlist;

public class AudioFileFilter extends FileExtensionFilterBase
{

  public AudioFileFilter()
  {
    super(Playlist.mediaExtensions);
  }

  @Override
  public String getDescription()
  {
    return "Audio Files and Playlists (*.m3u, *.m3u8, *.pls, *.wpl, *.xspf, *.xml, *.mp3, *.flac, *.aac, *.ogg, *.aiff, *.au, *.wma)";
  }

  @Override
  // Fixes display in linux
  public String toString()
  {
    return getDescription();
  }
}
