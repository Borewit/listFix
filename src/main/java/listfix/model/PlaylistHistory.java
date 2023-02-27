

package listfix.model;

/**
 * @author jcaron
 */

import listfix.config.PlaylistHistoryConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jcaron
 */
public class PlaylistHistory
{

  private PlaylistHistoryConfiguration playlistHistoryConfiguration;
  private final List<String> playlists = new ArrayList<>();
  private int limit = 0;

  /**
   * Creates a new instance of PlaylistHistory
   *
   * 
   */
  public PlaylistHistory(int x)
  {
    limit = x;
  }

  /**
   * 
   */
  public void setCapacity(int maxPlaylistHistoryEntries)
  {
    limit = maxPlaylistHistoryEntries;
    if (limit < playlists.size())
    {
      ((ArrayList) playlists).subList(limit, playlists.size()).clear();
    }
  }

  /**
   * 
   */
  protected int getLimit() // added to assist testing
  {
    return limit;
  }

  /**
   * 
   */
  protected List<String> getPlaylists() // added to assist testing
  {
    return playlists;
  }

  /**
   * 
   */
  public void initHistory(List<String> input)
  {
    int i = 0;
    for (String fileName : input)
    {
      File testFile = new File(fileName);
      if (testFile.exists())
      {
        playlists.add(fileName);
      }
      i++;
      if (i > limit)
        break;
    }
  }

  /**
   * 
   */
  public void add(String filename)
  {
    File testFile = new File(filename);
    if (testFile.exists())
    {
      int index = playlists.indexOf(filename);
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

  /**
   * 
   */
  public String[] getFilenames()
  {
    String[] result = new String[playlists.size()];
    for (int i = 0; i < playlists.size(); i++)
    {
      result[i] = (String) playlists.get(i);
    }
    return result;
  }


  public void clearHistory()
  {
    playlists.clear();
  }

  /**
   * Load last opened playlists from disk
   *
   * 
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
