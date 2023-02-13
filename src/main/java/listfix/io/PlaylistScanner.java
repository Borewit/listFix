package listfix.io;

import listfix.model.playlists.Playlist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * To create a list of all the playlists found in a given directory and its subdirectories.
 */
public class PlaylistScanner
{

  public static List<Path> getAllPlaylists(Path directory) throws IOException
  {
    List<Path> result = new ArrayList<>();
    getAllPlaylists(result, Files.list(directory));
    return result;
  }

  private static void getAllPlaylists(List<Path> result, Stream<Path> directory)
  {
    directory
      .forEach(path -> {
        if (Files.isDirectory(path))
        {
          try
          {
            getAllPlaylists(result, Files.list(path));
          }
          catch (IOException e)
          {
            throw new RuntimeException(e);
          }
        }
        else
        {
          if (Playlist.isPlaylist(path))
          {
            result.add(path);
          }
        }
      });
  }
}
