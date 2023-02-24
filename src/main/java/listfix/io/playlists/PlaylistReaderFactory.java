

package listfix.io.playlists;

import listfix.io.IPlaylistOptions;
import listfix.io.playlists.itunes.ITunesXMLReader;
import listfix.io.playlists.m3u.M3UReader;
import listfix.io.playlists.pls.PLSReader;
import listfix.io.playlists.wpl.WPLReader;
import listfix.io.playlists.xspf.XSPFReader;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.Playlist;

import java.io.IOException;
import java.nio.file.Path;

public class PlaylistReaderFactory
{

  public static IPlaylistReader getPlaylistReader(Path inputFile, IPlaylistOptions playListOptions) throws IOException
  {
    PlaylistType type = Playlist.determinePlaylistTypeFromExtension(inputFile);
    return switch (type)
      {
        case M3U -> new M3UReader(playListOptions, inputFile);
        case PLS -> new PLSReader(playListOptions, inputFile);
        case XSPF -> new XSPFReader(playListOptions, inputFile);
        case WPL -> new WPLReader(playListOptions, inputFile);
        case ITUNES -> new ITunesXMLReader(playListOptions, inputFile);
        default -> throw new IOException("Unsupported playlist type");
      };
  }
}
