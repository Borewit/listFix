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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import listfix.comparators.MatchedPlaylistEntryComparator;
import listfix.controller.GUIDriver;
import listfix.io.Constants;
import listfix.io.FileUtils;
import listfix.io.writers.IFilePathOptions;
import listfix.json.JsonAppOptions;
import listfix.model.enums.PlaylistEntryStatus;
import listfix.util.ArrayFunctions;
import listfix.util.ExStack;
import listfix.util.FileNameTokenizer;
import listfix.util.OperatingSystem;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;

import org.apache.log4j.Logger;

/**
 * @author jcaron
 */
public class PlaylistEntry implements Cloneable
{
  // logger
  private static final Logger _logger = Logger.getLogger(PlaylistEntry.class);

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

  /**
   * @return the _status
   */
  public PlaylistEntryStatus getStatus()
  {
    return _status;
  }

  // Construct an M3U URL entry.

  /**
   * @param uri
   * @param extra
   */
  public PlaylistEntry(URI uri, String extra)
  {
    _thisURI = uri;
    _extInf = extra;
    parseExtraInfo(extra);
  }

  // Construct a WPL URL entry

  /**
   * @param uri
   * @param extra
   * @param cid
   * @param tid
   */
  public PlaylistEntry(URI uri, String extra, String cid, String tid)
  {
    this(uri, extra);
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
  public PlaylistEntry(URI uri, String title, long length)
  {
    _thisURI = uri;
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
  public PlaylistEntry(URI uri, String title, long length, Playlist list)
  {
    _thisURI = uri;
    _title = title;
    _length = length;
    _extInf = "#EXTINF:" + convertDurationToSeconds(length) + "," + title;
    _playlist = list;
  }

  // Construct an M3U path & file-name based entry.

  /**
   * @param p
   * @param f
   * @param extra
   * @param list
   */
  public PlaylistEntry(String p, String f, String extra, File list)
  {
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
        _absoluteFile = new File(list.getParentFile(), p + Constants.FS + f);
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
            _absoluteFile = new File(root, p + Constants.FS + f);
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

  // Another WPL constructor

  /**
   * @param p
   * @param f
   * @param extra
   * @param list
   * @param cid
   * @param tid
   */
  public PlaylistEntry(String p, String f, String extra, File list, String cid, String tid)
  {
    this(p, f, extra, list);
    _cid = cid;
    _tid = tid;
  }

  // Construct a M3U file-based entry, used during save among other things

  /**
   * @param input
   * @param extra
   * @param list
   */
  public PlaylistEntry(File input, String extra, File list)
  {
    _fileName = input.getName();
    _path = input.getPath().substring(0, input.getPath().indexOf(_fileName));
    _thisFile = input;

    // this parsing is insufficient when going from windows to linux... special handling is necessary.
    if (!OperatingSystem.isWindows() && _path.isEmpty() && !input.getName().contains(Constants.FS))
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
   * @param input
   * @param extra
   * @param list
   * @param cid
   * @param tid
   */
  public PlaylistEntry(File input, String extra, File list, String cid, String tid)
  {
    this(input, extra, list);
    _cid = cid;
    _tid = tid;
  }

  // PLS constructor, which has file references, a title, and a length

  /**
   * @param input
   * @param title
   * @param length
   * @param list
   */
  public PlaylistEntry(File input, String title, long length, File list)
  {
    _fileName = input.getName();
    _path = input.getPath().substring(0, input.getPath().indexOf(_fileName));
    _thisFile = input;

    // the parsing above is insufficient when going from windows to linux... special handling is necessary.
    if (!OperatingSystem.isWindows() && _path.isEmpty() && !input.getName().contains(Constants.FS))
    {
      _fileName = input.getName().substring(input.getName().lastIndexOf("\\") + 1);
      _path = input.getPath().substring(0, input.getPath().indexOf(_fileName));
      _thisFile = new File(_path, _fileName);
    }

    _extInf = "#EXTINF:" + convertDurationToSeconds(length) + "," + title;
    _title = title;
    _length = length;
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

  // Copy constructor...

  /**
   * @param entry
   */
  public PlaylistEntry(PlaylistEntry entry)
  {
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

  /**
   *
   */
  public void markFixedIfFound()
  {
    if (getStatus() == PlaylistEntryStatus.Found)
    {
      _isFixed = true;
    }
  }

  /**
   * @return
   */
  public String getPath()
  {
    return this.isURL() ? "" : _path;
  }

  /**
   * @return
   */
  public File getFile()
  {
    return _thisFile;
  }

  /**
   * @return
   */
  public String getFileName()
  {
    return this.isURL() ? _thisURI.toString() : _fileName;
  }

  /**
   * @return
   */
  public String getExtInf()
  {
    return _extInf;
  }

  /**
   * @return
   */
  public String getCID()
  {
    return _cid;
  }

  /**
   * @return
   */
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

  /**
   * @return
   */
  public URI getURI()
  {
    return _thisURI;
  }

  /**
   * @return
   */
  public File getAbsoluteFile()
  {
    return _absoluteFile;
  }

  /**
   * @param input
   */
  public void setPath(String input)
  {
    _path = input;
    _thisFile = new File(_path, _fileName);
    resetAbsoluteFile();
    recheckFoundStatus();
  }

  /**
   * @param input
   */
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

  /**
   * @return
   */
  public boolean isFound()
  {
    return getStatus() == PlaylistEntryStatus.Found;
  }

  /**
   * @return
   */
  public boolean isFixed()
  {
    return _isFixed;
  }

  /**
   * @param fixed
   */
  public void setFixed(boolean fixed)
  {
    _isFixed = fixed;
  }

  /**
   * @return
   */
  public boolean isURL()
  {
    return _thisURI != null && _thisFile == null;
  }

  /**
   * @return
   */
  public boolean isRelative()
  {
    return !this.isURL() && _thisFile != null && !_thisFile.isAbsolute();
  }

  // Try to open the file with the "default" MP3 player (only works on some systems).

  /**
   * @throws Exception
   */
  public void play() throws Exception
  {
    if (this.isFound() || this.isURL())
    {
      List<PlaylistEntry> tempList = new ArrayList<>();
      tempList.add(this);
      (new Playlist(tempList, this._playlist.getFilePathOptions())).play();
    }
  }

  /**
   * @param fileList
   * @return
   */
  public boolean findNewLocationFromFileList(Collection<String> fileList)
  {
    String fileSearchResult = null;
    String trimmedFileName = _fileName.trim();
    boolean caseSensitiveMatching = Constants.FILE_SYSTEM_IS_CASE_SENSITIVE && !GUIDriver.getInstance().getOptions().getCaseInsensitiveExactMatching();
    String candidateFileName;
    int lastFileSeparatorIndex;
    for (String file : fileList)
    {
      // JCaron - 2012.04.22
      // Get the filename from the media library entry that we're looking at so we can compare it directly to the filename we're searching for.
      // Need the last index of the file separator character on the current system to accomplish this.
      // A direct comparison is not only faster, but prevents considering "01 - test.mp3" an exact match for "1 - test.mp3" like the logic in the else block below did previously.
      lastFileSeparatorIndex = file.lastIndexOf(Constants.FS);
      if (lastFileSeparatorIndex >= 0)
      {
        candidateFileName = file.substring(lastFileSeparatorIndex + 1);
      }
      else
      {
        // JCaron - 2012.04.22
        // In theory, this code should be unreachable.  The list of files passed in should always be absolute,
        // which would mean including at least one OS-specific file separator character somewhere in the string.  But, the logic below
        // has served us well for years, so it's a reasonable fallback should this case somehow arise.
        candidateFileName = file;
      }

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

  private JsonAppOptions getApplicationConfig()
  {
    return GUIDriver.getInstance().getOptions();
  }

  /**
   * @param mediaFiles
   * @param observer
   * @return
   */
  public List<PotentialPlaylistEntryMatch> findClosestMatches(Collection<String> mediaFiles, IProgressObserver<Playlist> observer, IFilePathOptions filePathOptions)
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
          if (matches.size() < getApplicationConfig().getMaxClosestResults())
          {
            matches.add(new PotentialPlaylistEntryMatch(mediaFile, score, _playlist.getFile()));
          }
          else
          {
            if (matches.get(getApplicationConfig().getMaxClosestResults() - 1).getScore() < score)
            {
              matches.set(getApplicationConfig().getMaxClosestResults() - 1, new PotentialPlaylistEntryMatch(mediaFile, score, _playlist.getFile()));
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

  /**
   * @return
   */
  @Override
  public Object clone()
  {
    PlaylistEntry result = null;
    if (!this.isURL())
    {
      result = new PlaylistEntry(this);
    }
    else
    {
      try
      {
        result = new PlaylistEntry(new URI(this.getURI().toString()), this.getTitle(), this.getLength(), _playlist);
      }
      catch (Exception e)
      {
        //eat the error for now.
        _logger.warn(ExStack.toString(e));
      }
    }
    return result;
  }

  /**
   * @return the _title
   */
  public String getTitle()
  {
    return _title;
  }

  /**
   * @return the _length, in milliseconds
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

  /**
   * @return
   */
  public boolean updatePathToMediaLibraryIfFoundOutside()
  {
    if (_status == PlaylistEntryStatus.Found && !ArrayFunctions.containsStringPrefixingAnotherString(GUIDriver.getInstance().getMediaDirs(), _path, !GUIDriver.FILE_SYSTEM_IS_CASE_SENSITIVE))
    {
      return findNewLocationFromFileList(GUIDriver.getInstance().getMediaLibraryFileList());
    }
    return false;
  }

  /**
   * @param list
   */
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

  private int convertDurationToSeconds(long length)
  {
    if (length <= 0)
    {
      return (int) length;
    }
    else
    {
      return (int) (length / 1000L);
    }
  }
}
