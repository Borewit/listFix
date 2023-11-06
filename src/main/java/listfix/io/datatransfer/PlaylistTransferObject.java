package listfix.io.datatransfer;

import io.github.borewit.lizzy.playlist.Playlist;
import io.github.borewit.lizzy.playlist.PlaylistFormat;
import io.github.borewit.lizzy.playlist.SpecificPlaylist;
import listfix.io.playlists.LizzyPlaylistUtil;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Used to serialize / deserialize playlist to memory from drag and drop
 */
public class PlaylistTransferObject
{
  public static DataFlavor M3uPlaylistDataFlavor = new DataFlavor("audio/mpegurl", "M3U Playlist");

  private static final PlaylistFormat playlistFormat = PlaylistFormat.m3u;

  public static void serialize(Playlist playlist, OutputStream outputStream) throws IOException
  {
    SpecificPlaylist specificPlaylist = LizzyPlaylistUtil.toPlaylist(playlistFormat, playlist);
    try
    {
      specificPlaylist.writeTo(outputStream);
    }
    catch (Exception e)
    {
      throw new IOException("Failed write playlist", e);
    }
  }

  public static Playlist deserialize(InputStream input)
  {
    SpecificPlaylist playlist = LizzyPlaylistUtil.readPlaylistFromInputStream(playlistFormat, input);
    return playlist.toPlaylist();
  }
}
