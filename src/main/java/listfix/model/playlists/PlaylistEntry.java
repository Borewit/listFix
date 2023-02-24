package listfix.model.playlists;

import listfix.comparators.MatchedPlaylistEntryComparator;
import listfix.io.IPlaylistOptions;
import listfix.model.enums.PlaylistEntryStatus;
import listfix.util.FileNameTokenizer;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author jcaron
 */
public abstract class PlaylistEntry implements Cloneable
{
  // logger
  /**
   * The list of folders we know don't exist.
   */
  public static List<String> NonExistentDirectories = new ArrayList<>();

  /**
   * A list of folders we know DO exist.
   */
  public static List<String> ExistingDirectories = new ArrayList<>();

  // This entry's extra info.
  protected String _extInf = "";

  // The _title of the entry
  protected String _title = "";

  // The _length of the track
  protected long _length = -1;

  // Status of this item.
  protected PlaylistEntryStatus _status = PlaylistEntryStatus.Exists;

  // Has this item been fixed?
  protected boolean _isFixed;

  // The file this entry belongs to.
  protected Playlist _playlist;

  // WPL
  protected String _cid = "";
  protected String _tid = "";

  private static final Pattern APOS_PATTERN = Pattern.compile("\'");
  private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("(([a-z])([A-Z]))");
  protected String _trackId;

  /**
   * @return the _status
   */
  public PlaylistEntryStatus getStatus()
  {
    return _status;
  }

  // Construct an M3U URL entry.

  protected PlaylistEntry()
  {
  }

  protected void copyTo(PlaylistEntry target)
  {
    target._extInf = this._extInf;
    target._title = this._title;
    target._length = this._length;
    target._playlist = this._playlist;
    target._status = this._status;
  }

  public void markFixedIfFound()
  {
    if (getStatus() == PlaylistEntryStatus.Found)
    {
      _isFixed = true;
    }
  }

  public abstract String getTrackFolder();

  public abstract String getTrackFileName();

  public String getExtInf()
  {
    return _extInf;
  }

  public String getCID()
  {
    return _cid;
  }

  public String getTID()
  {
    return _tid;
  }

  // check the file system for existence if we don't already know the file exists.
  protected abstract boolean exists();

  public abstract void recheckFoundStatus();

  public boolean isFound()
  {
    return getStatus() == PlaylistEntryStatus.Found;
  }

  public boolean isFixed()
  {
    return _isFixed;
  }

  public void setFixed(boolean fixed)
  {
    _isFixed = fixed;
  }

  public abstract boolean isURL();

  public abstract boolean isRelative();

  /**
   * Used to serialize track path to playlist
   */
  public abstract String trackPathToString();

  // Try to open the file with the "default" MP3 player (only works on some systems).

  public void play(IPlaylistOptions playListOptions) throws Exception
  {
    if (this.isFound() || this.isURL())
    {
      List<PlaylistEntry> tempList = new ArrayList<>();
      tempList.add(this);
      Playlist playlist = new Playlist(playListOptions, tempList);
      playlist.play();
    }
  }

  public List<PotentialPlaylistEntryMatch> findClosestMatches(Collection<String> mediaFiles, IProgressObserver<String> observer, IPlaylistOptions playListOptions)
  {
    List<PotentialPlaylistEntryMatch> matches = new ArrayList<>();
    ProgressAdapter<String> progress = ProgressAdapter.wrap(observer);
    progress.setTotal(mediaFiles.size());

    matches.clear();

    // Remove apostrophes and addAt spaces between lowercase and capital letters so we can tokenize by camel case.
    String entryName = CAMEL_CASE_PATTERN.matcher(APOS_PATTERN.matcher(getTrackFileName()).replaceAll("")).replaceAll("$2 $3").toLowerCase();
    File mediaFile;
    int score;
    for (String mediaFilePath : mediaFiles)
    {
      if (observer == null || !observer.getCancelled())
      {
        progress.stepCompleted();

        mediaFile = new File(mediaFilePath);

        // Remove apostrophes and addAt spaces between lowercase and capital letters so we can tokenize by camel case.
        score = (new FileNameTokenizer(playListOptions)).score(entryName, CAMEL_CASE_PATTERN.matcher(APOS_PATTERN.matcher(mediaFile.getName()).replaceAll("")).replaceAll("$2 $3").toLowerCase());
        if (score > 0)
        {
          // Only keep the top X highest-rated matches (default is 20), anything more than that has a good chance of using too much memory
          // on systems w/ huge media libraries, too little RAM, or when fixing excessively large playlists (the things you have to worry
          // about when people run your software on ancient PCs in Africa =])
          if (matches.size() < playListOptions.getMaxClosestResults())
          {
            matches.add(new PotentialPlaylistEntryMatch(mediaFile.toPath(), score, _playlist.getFile().toPath(), _playlist.getPlayListOptions()));
          }
          else
          {
            if (matches.get(playListOptions.getMaxClosestResults() - 1).getScore() < score)
            {
              matches.set(playListOptions.getMaxClosestResults() - 1, new PotentialPlaylistEntryMatch(mediaFile.toPath(), score, _playlist.getFile().toPath(), _playlist.getPlayListOptions()));
            }
          }
          matches.sort(new MatchedPlaylistEntryComparator());
        }
      }
      else
      {
        return null;
      }
    }
    return matches;
  }

  public abstract Object clone();

  /**
   * @return the title
   */
  public String getTitle()
  {
    return _title;
  }

  /**
   * @return the length, in milliseconds
   */
  public long getLength()
  {
    return _length;
  }

  protected void parseExtraInfo(String extra)
  {
    if (extra != null && extra.length() > 0)
    {
      extra = extra.replaceFirst("#EXTINF:", "");
      if (extra.contains(","))
      {
        String[] split = extra.split(",");
        if (split.length > 1)
        {
          try
          {
            // extra info comes from M3Us, so this comes in a seconds
            // and needs conversion to milliseconds.
            _length = Long.parseLong(split[0]) * 1000L;
          }
          catch (Exception e)
          {
            // ignore and move on...
          }
          _title = split[1];
        }
        else if (split.length == 1)
        {
          // assume it's a _title?
          _title = split[0];
        }
      }
      else
      {
        _title = extra;
      }
    }
  }

  public void setPlaylist(Playlist list)
  {
    _playlist = list;
  }

  /**
   * Some playlist types support this notion, and it must be tracked up until the list is saved in another format if so.
   *
   * @return the _trackId
   */
  public String getTrackId()
  {
    return _trackId;
  }

  /**
   * Some playlist types support this notion, and it must be tracked up until the list is saved in another format if so.
   *
   * @param trackId the _trackId to set
   */
  public void setTrackId(String trackId)
  {
    this._trackId = trackId;
  }

  protected static int convertDurationToSeconds(long length)
  {
    return length <= 0 ? (int) length : (int) (length / 1000L);
  }
}
