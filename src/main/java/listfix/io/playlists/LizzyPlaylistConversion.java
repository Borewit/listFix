package listfix.io.playlists;

import christophedelory.content.Content;
import christophedelory.playlist.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LizzyPlaylistConversion
{
  private static final SpecificPlaylistFactory specificPlaylistFactory = SpecificPlaylistFactory.getInstance();

  /**
   * Converts a collection of files to a Lizzy Playlist
   *
   * @param targetPlaylistType Target type, e.g. ".mp3"
   * @param listOfFile         Collection of files
   * @return Lizzy Playlist
   */
  public static SpecificPlaylist toPlaylist(String targetPlaylistType, Collection<String> listOfFile)
  {
    SpecificPlaylistProvider playlistProvider = specificPlaylistFactory.findProviderByExtension(targetPlaylistType);
    try
    {
      return playlistProvider.toSpecificPlaylist(convertToPlaylist(listOfFile));
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts Lizzy playlist to list of files
   *
   * @param playlist Lizzy playlist
   * @return List of files
   */
  public static List<String> toListOfFiles(Playlist playlist)
  {
    return Arrays.stream(playlist.getRootSequence().getComponents()).map(component -> {
        if (component instanceof Media)
        {
          Media media = (Media) component;
          Content content = media.getSource();
          return content == null ? null : content.toString();
        }
        return null;
      }).filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * Read playlist from InputStream
   * Does not close the inputStream!
   *
   * @param sourcePlaylistType Source playlist type, e.g. ".mp3"
   * @param inputStream        Stream to read from
   * @return Lizzy Playlist
   */
  public static SpecificPlaylist readPlaylistFromInputStream(String sourcePlaylistType, InputStream inputStream)
  {
    SpecificPlaylistProvider playlistProvider = specificPlaylistFactory.findProviderByExtension(sourcePlaylistType);
    try
    {
      return playlistProvider.readFrom(inputStream, null);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  /**
   * Convert list of files to Lizzy Playlist
   *
   * @param listOfFile List of path as strings
   * @return Lizzy Playlist
   */
  private static Playlist convertToPlaylist(Collection<String> listOfFile)
  {
    Playlist playlist = new Playlist();

    listOfFile.stream()
      .map(filePath -> {
        final Media media = new Media();
        final Content content = new Content(filePath);
        media.setSource(content);
        return media;
      })
      .forEach(media -> playlist.getRootSequence().addComponent(media));

    return playlist;
  }
}
