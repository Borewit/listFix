/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2014 Jeremy Caron
 *
 * This file is part of listFix().
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.model.playlists;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import listfix.comparators.MatchedPlaylistEntryComparator;
import listfix.config.IMediaLibrary;
import listfix.io.FileUtils;
import listfix.io.IPlayListOptions;
import listfix.model.enums.PlaylistEntryStatus;
import listfix.util.ArrayFunctions;
import listfix.util.FileNameTokenizer;
import listfix.util.OperatingSystem;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;

/**
 * @author jcaron
 */
public class PlaylistEntry implements Cloneable
{
  // logger
  private final boolean fileSystemIsCaseSensitive = File.separatorChar == '/';

  /**
   * The list of folders we know don't exist.
   */
  public static List<String> NonExistentDirectories = new ArrayList<>();

  /**
   * A list of folders we know DO exist.
   */
  public static List<String> ExistingDirectories = new ArrayList<>();

  // This entry's _path.
  private String _path = ".";

  // This entry's extra info.
  private String _extInf = "";

  // This entry's file name.
  private String _fileName = "";

  // This entry's File object.
  private File _thisFile = null;

  // This entry's absolute file.
  private File _absoluteFile = null;

  // The entry's URI (for URLs).
  private URI _thisURI = null;

  // The _title of the entry
  private String _title = "";

  // The _length of the track
  private long _length = -1;

  // Status of this item.
  private PlaylistEntryStatus _status = PlaylistEntryStatus.Exists;

  // Has this item been fixed?
  private boolean _isFixed;

  // The file this entry belongs to.
  private Playlist _playlist;

  // WPL
  private String _cid = "";
  private String _tid = "";

  private static final Pattern APOS_PATTERN = Pattern.compile("\'");
  private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("(([a-z])([A-Z]))");
  private String _trackId;

  private final IPlayListOptions playListOptions;

  /**
   * @return the _status
   */
  public PlaylistEntryStatus getStatus()
  {
    return _status;
  }

  // Construct an M3U URL entry.

  private PlaylistEntry(IPlayListOptions playListOptions)
  {
    this.playListOptions = playListOptions;
  }

  private PlaylistEntry(IPlayListOptions playListOptions, URI uri)
  {
    this.playListOptions = playListOptions;
    _thisURI = uri;
  }

  public PlaylistEntry(IPlayListOptions playListOptions, URI uri, String extra)
  {
    this(playListOptions, uri);
    _extInf = extra;
    parseExtraInfo(extra);
  }

  // Construct a WPL URL entry

  public PlaylistEntry(IPlayListOptions playListOptions, URI uri, String extra, String cid, String tid)
  {
    this(playListOptions, uri, extra);
    _cid = cid;
    _tid = tid;
  }

  /**
   * Construct a PLS/XSPF URL entry.
   *
   * @param uri
   * @param title
   * @param length
   */
  public PlaylistEntry(IPlayListOptions playListOptions, URI uri, String title, long length)
  {
    this(playListOptions, uri);
    _title = title;
    _length = length;
    _extInf = "#EXTINF:" + convertDurationToSeconds(length) + "," + title;
  }

  /**
   * Copy constructor for a URI entry
   *
   * @param uri
   * @param title
   * @param length
   * @param list
   */
  public PlaylistEntry(IPlayListOptions playListOptions, URI uri, String title, long length, Playlist list)
  {
    this(playListOptions, uri);
    _length = length;
    _extInf = "#EXTINF:" + convertDurationToSeconds(length) + "," + title;
    _playlist = list;
  }

  /**
   * Construct an M3U path & file-name based entry.
   *
   * @param p
   * @param f
   * @param extra
   * @param list
   */
  public PlaylistEntry(IPlayListOptions playListOptions, String p, String f, String extra, File list)
  {
    this(playListOptions);
    _path = p;
    _fileName = f;
    _extInf = extra;
    parseExtraInfo(extra);
    _thisFile = new File(_path, _fileName);
    // should we skip the exists check?
    if (skipExistsCheck())
    {
      _status = PlaylistEntryStatus.Missing;
    }
    // if we should check, do so...
    else if (this.exists())
    {
      // file was found in its current location
      _status = PlaylistEntryStatus.Found;
      if (_thisFile.isAbsolute())
      {
        _absoluteFile = _thisFile;
      }
      else
      {
        _absoluteFile = new File(_thisFile.getAbsolutePath());
      }
    }
    else
    {
      // file was not found
      if (!_thisFile.isAbsolute())
      {
        // if the test file was relative, try making it absolute.
        _absoluteFile = Path.of(list.getParentFile().getPath(), p, f).toFile();
        if (_absoluteFile.exists())
        {
          _status = PlaylistEntryStatus.Found;
          // _path = parentPath.getPath();
        }
        else
        {
          if (OperatingSystem.isWindows())
          {
            // try one more thing, winamp creates some stupid lists (saves out psuedo-relative lists where the entries are assumed to be on the same drive as where the list is found)
            // only attempt this hack on windows...
            File root = list.getParentFile();
            while (root.getParentFile() != null)
            {
              root = root.getParentFile();
            }
            _absoluteFile = Path.of(root.getPath(), p, f).toFile();
            if (_absoluteFile.exists())
            {
              _status = PlaylistEntryStatus.Found;
              _thisFile = new File(FileUtils.getRelativePath(_absoluteFile, list));
              _path = _thisFile.getPath();
            }
            else
            {
              _status = PlaylistEntryStatus.Missing;
            }
          }
          else
          {
            _status = PlaylistEntryStatus.Missing;
          }
        }
      }
      else
      {
        _status = PlaylistEntryStatus.Missing;
      }
    }
  }

  /**
   * Another WPL constructor
   */
  public PlaylistEntry(IPlayListOptions playListOptions, String p, String f, String extra, File list, String cid, String tid)
  {
    this(playListOptions, p, f, extra, list);
    _cid = cid;
    _tid = tid;
  }

  // Construct a M3U file-based entry, used during save among other things

  /**
   * Construct a M3U file-based entry, used during save among other things
   */
  public PlaylistEntry(IPlayListOptions playListOptions, File input, String extra, File list)
  {
    this(playListOptions);
    _fileName = input.getName();
    _path = input.getPath().substring(0, input.getPath().indexOf(_fileName));
    _thisFile = input;

    // this parsing is insufficient when going from windows to linux... special handling is necessary.
    if (!OperatingSystem.isWindows() && _path.isEmpty() && !input.getName().contains("/"))
    {
      _fileName = input.getName().substring(input.getName().lastIndexOf("\\") + 1);
      _path = input.getPath().substring(0, input.getPath().indexOf(_fileName));
      _thisFile = new File(_path, _fileName);
    }

    _extInf = extra;
    parseExtraInfo(extra);
    if (skipExistsCheck())
    {
      _status = PlaylistEntryStatus.Missing;
    }
    else if (this.exists())
    {
      _status = PlaylistEntryStatus.Found;
      if (_thisFile.isAbsolute())
      {
        _absoluteFile = _thisFile;
      }
      else
      {
        _absoluteFile = new File(_thisFile.getAbsolutePath());
      }
    }
    else
    {
      if (!_thisFile.isAbsolute())
      {
        _absoluteFile = new File(list.getParentFile(), _path + _fileName);
        if (_absoluteFile.exists())
        {
          _status = PlaylistEntryStatus.Found;
          // _path = parentPath.getPath();
        }
        else
        {
          _status = PlaylistEntryStatus.Missing;
        }
      }
      else
      {
        _status = PlaylistEntryStatus.Missing;
      }
    }
  }

  // WPL constructor to hang on to random WPL-specific bits

  /**
   * WPL constructor to hang on to random WPL-specific bits
   */
  public PlaylistEntry(IPlayListOptions playListOptions, File input, String extra, File list, String cid, String tid)
  {
    this(playListOptions, input, extra, list);
    _cid = cid;
    _tid = tid;
  }

  /**
   * PLS constructor, which has file references, a title, and a length
   *
   * @param playListOptions
   * @param input
   * @param title
   * @param length
   * @param list
   */
  public PlaylistEntry(IPlayListOptions playListOptions, File input, String title, long length, File list)
  {
    this(playListOptions, input, "#EXTINF:" + convertDurationToSeconds(length) + "," + title, list);
    _title = title;
    _length = length;
  }

  /**
   * Copy constructor...
   *
   * @param entry
   */
  public PlaylistEntry(PlaylistEntry entry)
  {
    this.playListOptions = entry.playListOptions;
    _fileName = entry._fileName;
    _path = entry._path;
    _extInf = entry._extInf;
    _title = entry._title;
    _length = entry._length;
    _thisFile = entry._thisFile;
    _playlist = entry._playlist;
    _status = entry._status;
    _absoluteFile = entry._absoluteFile;
    _thisURI = entry._thisURI;
  }

  public void markFixedIfFound()
  {
    if (getStatus() == PlaylistEntryStatus.Found)
    {
      _isFixed = true;
    }
  }

  public String getPath()
  {
    return this.isURL() ? "" : _path;
  }

  public File getFile()
  {
    return _thisFile;
  }

  public String getFileName()
  {
    return this.isURL() ? _thisURI.toString() : _fileName;
  }

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
  private boolean exists()
  {
    return isFound() || _thisFile.exists();
  }

  public void recheckFoundStatus()
  {
    if (_absoluteFile.isFile() && _absoluteFile.exists())
    {
      _status = PlaylistEntryStatus.Found;
    }
    else
    {
      _status = PlaylistEntryStatus.Missing;
      _isFixed = false;
    }
  }

  public URI getURI()
  {
    return _thisURI;
  }

  public File getAbsoluteFile()
  {
    return _absoluteFile;
  }

  public void setPath(String input)
  {
    _path = input;
    _thisFile = new File(_path, _fileName);
    resetAbsoluteFile();
    recheckFoundStatus();
  }

  public void setFileName(String input)
  {
    _fileName = input;
    _thisFile = new File(_path, _fileName);
    resetAbsoluteFile();
    recheckFoundStatus();
  }

  public void setFile(File input)
  {
    _thisFile = input;
    _fileName = _thisFile.getName();
    _path = _thisFile.getPath().substring(0, _thisFile.getPath().indexOf(_fileName));
    resetAbsoluteFile();
    recheckFoundStatus();
  }

  private void resetAbsoluteFile()
  {
    if (_thisFile.isAbsolute())
    {
      _absoluteFile = _thisFile;
    }
    else
    {
      _absoluteFile = new File(_playlist.getFile().getAbsoluteFile().getParent(), _thisFile.toString());
    }
  }

  private boolean skipExistsCheck()
  {
    String[] emptyPaths = new String[NonExistentDirectories.size()];
    NonExistentDirectories.toArray(emptyPaths);
    return isFound() || this.isURL() || ArrayFunctions.containsStringPrefixingAnotherString(emptyPaths, _path, false);
  }

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

  public boolean isURL()
  {
    return _thisURI != null && _thisFile == null;
  }

  public boolean isRelative()
  {
    return !this.isURL() && _thisFile != null && !_thisFile.isAbsolute();
  }

  // Try to open the file with the "default" MP3 player (only works on some systems).

  public void play() throws Exception
  {
    if (this.isFound() || this.isURL())
    {
      List<PlaylistEntry> tempList = new ArrayList<>();
      tempList.add(this);
      (new Playlist(this.playListOptions, tempList)).play();
    }
  }

  public boolean findNewLocationFromFileList(Collection<String> fileList)
  {
    String fileSearchResult = null;
    String trimmedFileName = _fileName.trim();
    boolean caseSensitiveMatching = this.fileSystemIsCaseSensitive && !this.playListOptions.getCaseInsensitiveExactMatching();
    String candidateFileName;
    for (String file : fileList)
    {
      // Get the filename from the media library entry that we're looking at so we can compare it directly to the filename we're searching for.
      candidateFileName = Path.of(file).getFileName().toString();

      if (caseSensitiveMatching ? candidateFileName.equals(trimmedFileName) : candidateFileName.equalsIgnoreCase(trimmedFileName))
      {
        fileSearchResult = file;
        break;
      }
    }
    if (fileSearchResult != null)
    {
      this.setFile(new File(fileSearchResult));
      _status = this.getFile().exists() ? PlaylistEntryStatus.Found : PlaylistEntryStatus.Missing;
      _isFixed = _status == PlaylistEntryStatus.Found;
      return true;
    }
    return false;
  }

  public List<PotentialPlaylistEntryMatch> findClosestMatches(Collection<String> mediaFiles, IProgressObserver<Playlist> observer, IPlayListOptions filePathOptions)
  {
    List<PotentialPlaylistEntryMatch> matches = new ArrayList<>();
    ProgressAdapter<Playlist> progress = ProgressAdapter.wrap(observer);
    progress.setTotal(mediaFiles.size());

    matches.clear();

    // Remove apostrophes and addAt spaces between lowercase and capital letters so we can tokenize by camel case.
    String entryName = CAMEL_CASE_PATTERN.matcher(APOS_PATTERN.matcher(getFileName()).replaceAll("")).replaceAll("$2 $3").toLowerCase();
    File mediaFile;
    int score;
    for (String mediaFilePath : mediaFiles)
    {
      if (observer == null || !observer.getCancelled())
      {
        progress.stepCompleted();

        mediaFile = new File(mediaFilePath);

        // Remove apostrophes and addAt spaces between lowercase and capital letters so we can tokenize by camel case.
        score = (new FileNameTokenizer(filePathOptions)).score(entryName, CAMEL_CASE_PATTERN.matcher(APOS_PATTERN.matcher(mediaFile.getName()).replaceAll("")).replaceAll("$2 $3").toLowerCase());
        if (score > 0)
        {
          // Only keep the top X highest-rated matches (default is 20), anything more than that has a good chance of using too much memory
          // on systems w/ huge media libraries, too little RAM, or when fixing excessively large playlists (the things you have to worry
          // about when people run your software on ancient PCs in Africa =])
          if (matches.size() < this.playListOptions.getMaxClosestResults())
          {
            matches.add(new PotentialPlaylistEntryMatch(this.playListOptions, mediaFile, score, _playlist.getFile()));
          }
          else
          {
            if (matches.get(this.playListOptions.getMaxClosestResults() - 1).getScore() < score)
            {
              matches.set(this.playListOptions.getMaxClosestResults() - 1, new PotentialPlaylistEntryMatch(this.playListOptions, mediaFile, score, _playlist.getFile()));
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

  @Override
  public Object clone()
  {
    try
    {
      return this.isURL() ?
        new PlaylistEntry(this.playListOptions, new URI(this.getURI().toString()), this.getTitle(), this.getLength(), _playlist) :
        new PlaylistEntry(this);
    }
    catch (URISyntaxException e)
    {
      throw new RuntimeException(e);
    }
  }

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

  private void parseExtraInfo(String extra)
  {
    if (extra != null && extra.length() > 0)
    {
      extra = extra.replaceFirst("#EXTINF:", "");
      if (extra.contains(","))
      {
        String[] split = extra.split(",");
        if (split != null && split.length > 1)
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
        else if (split != null && split.length == 1)
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

  public boolean updatePathToMediaLibraryIfFoundOutside(IMediaLibrary dirLists)
  {
    if (_status == PlaylistEntryStatus.Found && !ArrayFunctions.containsStringPrefixingAnotherString(dirLists.getDirectories(), _path, !this.fileSystemIsCaseSensitive))
    {
      return findNewLocationFromFileList(dirLists.getNestedMediaFiles());
    }
    return false;
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

  private static int convertDurationToSeconds(long length)
  {
    return length <= 0 ? (int) length : (int) (length / 1000L);
  }
}
