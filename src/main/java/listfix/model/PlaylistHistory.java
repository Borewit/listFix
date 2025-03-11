package listfix.model;

import listfix.config.PlaylistHistoryConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;

public class PlaylistHistory
{

  private PlaylistHistoryConfiguration playlistHistoryConfiguration;
  private final List<String> playlists = new ArrayList<>();
  private int limit = 0;

  /**
   * Creates a new instance of PlaylistHistory.
   */
  public PlaylistHistory(int x)
  {
    limit = x;
  }

  public void setCapacity(int maxPlaylistHistoryEntries)
  {
    limit = maxPlaylistHistoryEntries;
    if (limit < playlists.size())
    {
      ((ArrayList<?>) playlists).subList(limit, playlists.size()).clear();
    }
  }

  protected int getLimit() // added to assist testing
  {
    return limit;
  }

  protected List<String> getPlaylists() // added to assist testing
  {
    return playlists;
  }

  /**
   * Normalize the filename or path string to NFC form.
   *
   * @param filename String to normalize.
   * @return Normalized string.
   */
  private static String normalizeFilename(String filename) {
    return filename == null ? null : Normalizer.normalize(filename, Form.NFC);
  }

  /**
   * Initializes the history from a list of file names.
   * Checks file existence using the raw file names.
   *
   * @param input List of file names.
   */
  public void initHistory(List<String> input)
  {
    int i = 0;
    for (String fileName : input)
    {
      // Use raw file name for existence check
      File testFile = new File(fileName);
      if (testFile.exists())
      {
        playlists.add(fileName);
      }
      i++;
      if (i >= limit)
        break;
    }
  }

  /**
   * Adds a filename to the history.
   * Uses the raw filename for existence check, but compares entries
   * using normalized forms.
   *
   * @param filename The filename to add.
   */
  public void add(String filename)
  {
    // Check existence with raw filename
    File testFile = new File(filename);
    if (testFile.exists())
    {
      String normalizedInput = normalizeFilename(filename);
      int index = -1;
      for (int i = 0; i < playlists.size(); i++)
      {
        String normalizedExisting = normalizeFilename(playlists.get(i));
        if (normalizedExisting.equals(normalizedInput))
        {
          index = i;
          break;
        }
      }
      if (index > -1)
      {
        String temp = playlists.remove(index);
        playlists.add(0, temp);
      }
      else
      {
        if (playlists.size() < limit)
        {
          playlists.add(0, filename);
        }
        else
        {
          playlists.remove(limit - 1);
          playlists.add(0, filename);
        }
      }
    }
  }

  public String[] getFilenames()
  {
    String[] result = new String[playlists.size()];
    for (int i = 0; i < playlists.size(); i++)
    {
      result[i] = playlists.get(i);
    }
    return result;
  }

  public void clearHistory()
  {
    playlists.clear();
  }

  /**
   * Load last opened playlists from disk.
   */
  public void load() throws IOException
  {
    this.playlistHistoryConfiguration = PlaylistHistoryConfiguration.load();
    this.initHistory(this.playlistHistoryConfiguration.getConfig().getRecentPlaylists());
  }

  public void write() throws IOException
  {
    this.playlistHistoryConfiguration.write();
  }
}
