package listfix.model.playlists;

import listfix.io.IPlaylistOptions;
import listfix.view.support.IProgressObserver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PlaylistFactory
{
  public static Playlist getPlaylist(File playlistFile, IProgressObserver<String> observer, IPlaylistOptions filePathOptions) throws IOException
  {
    return getPlaylist(playlistFile.toPath(), observer, filePathOptions);
  }

  public static Playlist getPlaylist(Path playlistPath, IProgressObserver<String> observer, IPlaylistOptions playListOptions) throws IOException
  {
    return Playlist.load(playlistPath, observer, playListOptions);
  }
}
