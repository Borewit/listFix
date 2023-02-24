

package listfix.io.playlists.pls;

import listfix.io.Constants;
import listfix.io.IPlaylistOptions;
import listfix.io.playlists.PlaylistReader;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.FilePlaylistEntry;
import listfix.model.playlists.PlaylistEntry;
import listfix.model.playlists.UriPlaylistEntry;
import listfix.util.OperatingSystem;
import listfix.util.UnicodeUtils;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads in a PLS file and returns a List containing PlaylistEntries that represent the files & URIs in the playlist.
 * @author jcaron
 */
public class PLSReader extends PlaylistReader
{
  private List<PlaylistEntry> results = new ArrayList<>();
  private static final PlaylistType type = PlaylistType.PLS;
  private static final Logger _logger = LogManager.getLogger(PLSReader.class);

  public PLSReader(IPlaylistOptions playListOptions, Path plsFile) throws FileNotFoundException
  {
    super(playListOptions, plsFile);
    try
    {
      this.encoding = UnicodeUtils.getEncoding(plsFile);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<PlaylistEntry> readPlaylist(IProgressObserver<String> observer) throws IOException
  {
    // Definition of the PLS format can be found @ http://gonze.com/playlists/playlist-format-survey.html#PLS

    // Init a progress adapter if we have a progress observer.
    ProgressAdapter<String> progress = ProgressAdapter.wrap(observer);

    // Load the PLS file into memory (it's basically a glorified INI | java properties file).
    PLSProperties propBag = new PLSProperties();
    if (encoding.equals(StandardCharsets.UTF_8))
    {
      propBag.load(new InputStreamReader(new FileInputStream(playlistPath.toFile()), StandardCharsets.UTF_8));
    }
    else
    {
      propBag.load(new FileInputStream(playlistPath.toFile()));
    }

    // Find out how many entries we have to process.
    int entries = Integer.parseInt((propBag.getProperty("NumberOfEntries", "0")));

    // Set the total if we have an observer.
    progress.setTotal(entries);

    // Loop over the entries and process each in turn.
    for (int i = 1; i <= entries; i++)
    {
      if (observer != null)
      {
        if (observer.getCancelled())
        {
          // Bail out if the user cancelled.
          return null;
        }
      }
      processEntry(propBag, i);
      // Step forward if we have an observer.
      progress.setCompleted(i);
    }
    return results;
  }

  @Override
  public List<PlaylistEntry> readPlaylist() throws IOException
  {
    readPlaylist(null);
    return results;
  }

  private void processEntry(PLSProperties propBag, int index)
  {
    String file = propBag.getProperty("File" + index, "");
    String title = propBag.getProperty("Title" + index, "");
    String length = propBag.getProperty("Length" + index, "");
    long duration = -1;
    duration = Long.parseLong(length) * 1000L;

    if (file.contains("://"))
    {
      try
      {
        results.add(new UriPlaylistEntry(new URI(file), title, duration));
      }
      catch (URISyntaxException ex)
      {
        _logger.error(ex);
      }
    }
    else
    {
      // We have to perform FS conversion here...
      // if there are no FS instances in this string, look for the one from the other file system
      if (!file.contains(Constants.FS))
        if (OperatingSystem.isLinux() || OperatingSystem.isMac())
        {
          file = file.replace("\\", Constants.FS);
        }
        else if (OperatingSystem.isWindows())
        {
          file = file.replace("/", Constants.FS);
        }
      results.add(new FilePlaylistEntry(Path.of(file), title, duration, playlistPath));
    }
  }

  @Override
  public PlaylistType getPlaylistType()
  {
    return type;
  }
}
