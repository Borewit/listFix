package listfix.io.playlists;

import io.github.borewit.lizzy.playlist.*;
import listfix.io.FileUtils;
import listfix.io.filters.SpecificPlaylistFileFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.*;
import java.util.stream.Collectors;

public class LizzyPlaylistUtil
{
  /**
   * Playlist extensions
   */
  public static final Set<String> playlistExtensions;
  private static final SpecificPlaylistFactory specificPlaylistFactory;

  static
  {
    specificPlaylistFactory = SpecificPlaylistFactory.getInstance();

    HashSet<String> extensions = new HashSet<>();
    specificPlaylistFactory.getProviders().stream()
        .map(SpecificPlaylistProvider::getContentTypes)
        .forEach(contentTypes -> {
          Arrays.stream(contentTypes)
              .forEach(contentType -> extensions
                  .addAll(Arrays.stream(contentType.getExtensions())
                      .map(ext -> ext.startsWith(".") ? ext.substring(1) : ext)
                      .collect(Collectors.toList())));
        });
    playlistExtensions = extensions;
  }

  /**
   * Convert Lizzy specific playlist providers in.
   *
   * @return List of PlaylistExtensionFilters representing all context types.
   */
  public static List<SpecificPlaylistFileFilter> getPlaylistExtensionFilters()
  {
    List<SpecificPlaylistFileFilter> filters = new ArrayList<>();
    specificPlaylistFactory.getProviders().stream()
        .forEach(provider -> {
          Arrays.stream(provider.getContentTypes()).forEach(contentType -> {
            SpecificPlaylistFileFilter filter = new SpecificPlaylistFileFilter(provider, contentType);
            filters.add(filter);
          });
        });
    return filters;
  }

  /**
   * Converts a collection of files to a Lizzy Playlist.
   *
   * @param playlistFormat Lizzy playlist format ID.
   * @param playlist       Collection of files.
   * @return Lizzy Playlist.
   */
  public static SpecificPlaylist toPlaylist(PlaylistFormat playlistFormat, Playlist playlist)
  {
    SpecificPlaylistProvider playlistProvider = specificPlaylistFactory.getProvider(playlistFormat);
    if (playlistProvider == null)
      throw new RuntimeException("Cannot not find provider ");
    try
    {
      return playlistProvider.toSpecificPlaylist(playlist);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  /**
   * Read playlist from InputStream.
   * Does not close the inputStream!
   *
   * @param playlistFormat Source playlist type, e.g. "mp3".
   * @param inputStream    Stream to read from.
   * @return Lizzy Playlist.
   */
  public static SpecificPlaylist readPlaylistFromInputStream(PlaylistFormat playlistFormat, InputStream inputStream)
  {
    SpecificPlaylistProvider specificPlaylistProvider = specificPlaylistFactory.getProvider(playlistFormat);
    try
    {
      SpecificPlaylist specificPlaylist = specificPlaylistProvider.readFrom(inputStream);
      if (specificPlaylist == null)
      {
        throw new IOException("Failed to load playlist");
      }
      return specificPlaylist;
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public static SpecificPlaylist readPlaylist(Path playlistPath) throws IOException
  {
    return specificPlaylistFactory.readFrom(playlistPath);
  }

  public static SpecificPlaylist writeNewPlaylist(Playlist playlist, Path path, PlaylistFormat playlistFormat, OpenOption... options) throws IOException
  {
    try (OutputStream outputStream = Files.newOutputStream(path, options))
    {
      SpecificPlaylist specificPlaylist = specificPlaylistFactory.getProvider(playlistFormat).toSpecificPlaylist(playlist);
      specificPlaylist.writeTo(outputStream);
      return specificPlaylist;
    }
  }

  public static boolean providerAccepts(SpecificPlaylistProvider provider, String filename) {
    // For internal extension matching, normalize the filename.
    String normalizedFilename = filename == null ? null : Normalizer.normalize(filename, Form.NFC);
    return Arrays.stream(provider.getContentTypes()).anyMatch(types -> types.matchExtension(normalizedFilename));
  }

  public static String getPreferredExtensionFor(PlaylistFormat playlistFormat)
  {
    return specificPlaylistFactory.getProvider(playlistFormat).getContentTypes()[0].getExtensions()[0];
  }

  /**
   * Determine if the path provided is a playlist.
   *
   * @param path Path to test.
   * @return True if the path is a file and ends with a playlist extension.
   */
  public static boolean isPlaylist(Path path)
  {
    String extension = FileUtils.getFileExtension(path.toString());
    return extension != null && playlistExtensions.contains(extension.toLowerCase());
  }

  public static SpecificPlaylistProvider getProvider(PlaylistFormat format)
  {
    return specificPlaylistFactory.getProvider(format);
  }
}
