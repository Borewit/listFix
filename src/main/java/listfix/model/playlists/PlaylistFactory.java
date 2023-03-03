package listfix.model.playlists;

import listfix.io.IPlaylistOptions;
import listfix.io.playlists.IPlaylistReader;
import listfix.io.playlists.PlaylistReaderFactory;
import listfix.io.playlists.itunes.ITunesXMLReader;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.itunes.ITunesPlaylist;
import listfix.view.support.IProgressObserver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;


public class PlaylistFactory
{
  public static Playlist getPlaylist(File playlistFile, IProgressObserver observer, IPlaylistOptions filePathOptions) throws IOException
  {
    return getPlaylist(playlistFile.toPath(), observer, filePathOptions);
  }

  public static Playlist getPlaylist(Path playlistPath, IProgressObserver observer, IPlaylistOptions filePathOptions) throws IOException
  {
    IPlaylistReader playlistProcessor = PlaylistReaderFactory.getPlaylistReader(playlistPath, filePathOptions);
    List<PlaylistEntry> entries = playlistProcessor.readPlaylist(observer);

    if (playlistProcessor.getPlaylistType() == PlaylistType.ITUNES)
    {
      return new ITunesPlaylist(playlistPath.toFile(), entries, ((ITunesXMLReader) playlistProcessor).getLibrary(), filePathOptions);
    }
    else
    {
      return new Playlist(filePathOptions, playlistPath.toFile(), playlistProcessor.getPlaylistType(), entries);
    }
  }
}
