package listfix.io.datatransfer;

import christophedelory.playlist.SpecificPlaylist;
import listfix.io.playlists.LizzyPlaylistConversion;

import java.awt.datatransfer.DataFlavor;
import java.io.*;
import java.util.List;

/**
 * Used to serialize / deserialize playlist to memory from drag and drop
 */
public class PlaylistTransferObject
{
  public static DataFlavor M3uPlaylistDataFlavor = new DataFlavor("audio/mpegurl", "M3U Playlist");

  private static final String playlistType = ".m3u"; // ToDo: use MIME

  public static void serialize(List<String> entryList, OutputStream outputStream) throws IOException
  {
    SpecificPlaylist specificPlaylist = LizzyPlaylistConversion.toPlaylist(playlistType, entryList);
    try
    {
      specificPlaylist.writeTo(outputStream, null);
    }
    catch (Exception e)
    {
      throw new IOException("Failed write playlist", e);
    }
  }

  public static List<String> deserialize(InputStream input)
  {
    SpecificPlaylist playlist = LizzyPlaylistConversion.readPlaylistFromInputStream(".m3u", input); // ToDo: use MIME
    return LizzyPlaylistConversion.toListOfFiles(playlist.toPlaylist());
  }
}
