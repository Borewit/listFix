package listfix.io.playlists;

import listfix.io.IPlaylistOptions;
import listfix.io.playlists.itunes.ITunesXMLWriter;
import listfix.io.playlists.m3u.M3UWriter;
import listfix.io.playlists.pls.PLSWriter;
import listfix.io.playlists.wpl.WPLWriter;
import listfix.io.playlists.xspf.XSPFWriter;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.Playlist;

import java.io.File;
import java.io.IOException;

public class PlaylistWriterFactory
{
  public static IPlaylistWriter getPlaylistWriter(File inputFile, IPlaylistOptions playListOptions) throws IOException
  {
    PlaylistType type = Playlist.determinePlaylistTypeFromExtension(inputFile);
    return switch (type)
      {
        case M3U -> new M3UWriter(playListOptions);
        case PLS -> new PLSWriter(playListOptions);
        case XSPF -> new XSPFWriter(playListOptions);
        case WPL -> new WPLWriter(playListOptions);
        case ITUNES -> new ITunesXMLWriter(playListOptions);
        default -> throw new IOException("Unsupported playlist type");
      };
  }
}
