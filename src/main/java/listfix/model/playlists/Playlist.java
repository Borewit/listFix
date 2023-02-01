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

import listfix.config.IMediaLibrary;
import listfix.io.FileLauncher;
import listfix.io.IPlaylistOptions;
import listfix.io.UNCFile;
import listfix.io.readers.playlists.IPlaylistReader;
import listfix.io.readers.playlists.PlaylistReaderFactory;
import listfix.io.writers.playlists.IPlaylistWriter;
import listfix.io.writers.playlists.PlaylistWriterFactory;
import listfix.model.BatchMatchItem;
import listfix.model.enums.PlaylistType;
import listfix.util.FileNameTokenizer;
import listfix.view.support.IPlaylistModifiedListener;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;
import listfix.view.support.ProgressWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * @author jcaron
 */
public class Playlist
{
  private static final String FS = System.getProperty("file.separator");
  private static final String BR = System.getProperty("line.separator");
  private static final String HOME_DIR = System.getProperty("user.home");
  private static int NEW_LIST_COUNT = -1;

  private static final Marker markerPlaylist = MarkerManager.getMarker("Playlist");
  private static final Marker markerPlaylistRepair = MarkerManager.getMarker("Playlist-Repair").setParents(markerPlaylist);

  private File _file;
  private List<PlaylistEntry> _entries = new ArrayList<>();
  private List<PlaylistEntry> _originalEntries = new ArrayList<>();
  private boolean _utfFormat = false;
  private PlaylistType _type = PlaylistType.UNKNOWN;
  private int _fixedCount;
  private int _urlCount;
  private int _missingCount;
  private boolean _isModified;
  private boolean _isNew;
  private static final Logger _logger = LogManager.getLogger(Playlist.class);

  private final IPlaylistOptions playListOptions;

  /**
   * This constructor creates a temp-file backed playlist from a list of entries, only intended to be used for playback.
   *
   * @param sublist
   * @throws Exception
   */
  public Playlist(IPlaylistOptions playListOptions, List<PlaylistEntry> sublist) throws Exception
  {
    this.playListOptions = playListOptions;
    _utfFormat = true;
    _file = File.createTempFile("yay", ".m3u8");
    _file.deleteOnExit();

    setEntries(sublist);

    _type = PlaylistType.M3U;
    _isModified = false;
    refreshStatus();
    quickSave();
  }

  /**
   * Initializes a playlist with an externally created set of entries.
   * Currently used when reading playlists with external code.
   *
   * @param listFile
   * @param type
   * @param entries
   */
  public Playlist(IPlaylistOptions playListOptions, File listFile, PlaylistType type, List<PlaylistEntry> entries)
  {
    this.playListOptions = playListOptions;
    _utfFormat = true;
    _file = listFile;
    _type = type;
    _isModified = false;
    setEntries(entries);
    refreshStatus();
  }

  /**
   * Initializes a playlist using internal listFix() I/O model.
   *
   * @param playlist
   * @param observer
   * @throws IOException
   */
  public Playlist(IPlaylistOptions playListOptions, File playlist, IProgressObserver observer) throws IOException
  {
    this.playListOptions = playListOptions;
    init(playlist, observer);
  }

  /**
   * Creates an empty, untitled playlist.  The name of the list is auto-generated,
   * Untitled-#.m3u8 by default, where # is the number of lists you have created with this
   * method in the current session.
   *
   * @throws IOException
   */
  public Playlist(IPlaylistOptions playListOptions) throws IOException
  {
    this.playListOptions = playListOptions;
    NEW_LIST_COUNT++;
    _file = new File(HOME_DIR + FS + "Untitled-" + NEW_LIST_COUNT + ".m3u8");
    _file.deleteOnExit();
    _utfFormat = true;
    _type = PlaylistType.M3U;
    _isModified = false;
    _isNew = true;
    refreshStatus();
  }

  public final IPlaylistOptions getPlayListOptions()
  {
    return this.playListOptions;
  }

  private void init(File playlist, IProgressObserver<Void> observer) throws IOException
  {
    _file = playlist;
    IPlaylistReader playlistProcessor = PlaylistReaderFactory.getPlaylistReader(playlist.toPath(), playListOptions);
    if (observer != null)
    {
      List<PlaylistEntry> tempEntries = playlistProcessor.readPlaylist(observer);
      if (tempEntries != null)
      {
        this.setEntries(tempEntries);
      }
      else
      {
        return;
      }
    }
    else
    {
      this.setEntries(playlistProcessor.readPlaylist());
    }
    _utfFormat = playlistProcessor.getEncoding().equals("UTF-8");
    _type = playlistProcessor.getPlaylistType();
    if (_type == PlaylistType.PLS)
    {
      // let's override our previous determination in the PLS case so we don't end up saving it out incorrectly
      _utfFormat = false;
    }
    _isModified = false;
    refreshStatus();
  }

  /**
   * @return
   */
  public List<PlaylistEntry> getEntries()
  {
    return _entries;
  }

  /**
   * @param entryIndexList
   * @param destinationDirectory
   * @param observer
   */
  public void copySelectedEntries(List<Integer> entryIndexList, File destinationDirectory, IProgressObserver observer)
  {
    ProgressAdapter progress = ProgressAdapter.wrap(observer);
    progress.setTotal(entryIndexList.size());
    PlaylistEntry tempEntry;
    Path fileToCopy;
    Path dest;
    for (Integer entryIndexList1 : entryIndexList)
    {
      if (!observer.getCancelled())
      {
        tempEntry = this.get(entryIndexList1);
        if (tempEntry instanceof FilePlaylistEntry)
        {
          fileToCopy = ((FilePlaylistEntry) tempEntry).getAbsolutePath();
          if (tempEntry.isFound()) // && fileToCopy.exists())
          {
            dest = Path.of(destinationDirectory.getPath(), tempEntry.getTrackFileName());
            try
            {
              Files.copy(fileToCopy, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e)
            {
              // eat the error and continue
              throw new RuntimeException("Error copying file", e);
            }
          }
        }
        progress.stepCompleted();
      }
      else
      {
        return;
      }
    }
  }

  /**
   * @param observer
   */
  protected void resetInternalStateAfterSave(IProgressObserver observer)
  {
    // change original _entries
    replaceEntryListContents(_entries, _originalEntries);
    _isModified = false;
    _isNew = false;

    // set entries unfixed if we're being watched...
    // (otherwise writing out a temp file for playback, at least right now)
    if (observer != null)
    {
      for (PlaylistEntry entry : _entries)
      {
        entry.setFixed(false);
      }
    }

    refreshStatus();
  }

  /**
   *
   */
  public enum SortIx
  {
    /**
     *
     */
    None,
    /**
     *
     */
    Filename,
    /**
     *
     */
    Path,
    /**
     *
     */
    Status,
    /**
     *
     */
    Random,
    /**
     *
     */
    Reverse
  }

  /**
   * @param rows
   * @return
   * @throws Exception
   */
  public Playlist getSublist(int[] rows) throws Exception
  {
    List<PlaylistEntry> tempList = new ArrayList<>();
    for (int i : rows)
    {
      tempList.add(_entries.get(i));
    }
    return new Playlist(this.playListOptions, tempList);
  }

  /**
   * @param rows
   * @return
   * @throws IOException
   */
  public List<PlaylistEntry> getSelectedEntries(int[] rows) throws IOException
  {
    List<PlaylistEntry> tempList = new ArrayList<>();
    for (int i : rows)
    {
      tempList.add(_entries.get(i));
    }
    return tempList;
  }

  /**
   * @return the _type
   */
  public PlaylistType getType()
  {
    return _type;
  }

  /**
   * @param type
   */
  public void setType(PlaylistType type)
  {
    _type = type;
  }

  /**
   * @return
   */
  public File getFile()
  {
    return _file;
  }

  /**
   * @param file
   */
  public void setFile(File file)
  {
    UNCFile uncFile = new UNCFile(file);
    // if we're in "use UNC" mode, flip the file to a UNC representation
    String fileName = this.playListOptions.getAlwaysUseUNCPaths() ? uncFile.getUNCPath() : uncFile.getDrivePath();
    this._file = new File(fileName);
  }

  /**
   * @param listener
   */
  public void addModifiedListener(IPlaylistModifiedListener listener)
  {
    if (listener != null)
    {
      if (!_listeners.contains(listener))
      {
        _listeners.add(listener);
      }
    }
  }

  /**
   * @param listener
   */
  public void removeModifiedListener(IPlaylistModifiedListener listener)
  {
    _listeners.remove(listener);
  }

  /**
   * @return
   */
  public List<IPlaylistModifiedListener> getModifiedListeners()
  {
    return _listeners;
  }

  private void firePlaylistModified()
  {
    for (IPlaylistModifiedListener listener : _listeners)
    {
      if (listener != null)
      {
        listener.playlistModified(this);
      }
    }
  }

  private List<IPlaylistModifiedListener> _listeners = new ArrayList<>();

  private void setEntries(List<PlaylistEntry> aEntries)
  {
    for (PlaylistEntry entry : aEntries)
    {
      entry.setPlaylist(this);
    }

    replaceEntryListContents(aEntries, _originalEntries);
    replaceEntryListContents(aEntries, _entries);
  }

  private void replaceEntryListContents(List<PlaylistEntry> src, List<PlaylistEntry> dest)
  {
    dest.clear();
    for (PlaylistEntry src1 : src)
    {
      dest.add((PlaylistEntry) src1.clone());
    }
  }

  /**
   * @return
   */
  public int size()
  {
    return _entries.size();
  }

  /**
   *
   */
  public void updateModifiedStatus()
  {
    boolean result = false;

    // Run a full comparison against the original entry list we created when we were constructed
    if (_originalEntries.size() != _entries.size())
    {
      result = true;
    }
    else
    {
      for (int i = 0; i < _entries.size(); i++)
      {
        PlaylistEntry entryA = _entries.get(i);
        PlaylistEntry entryB = _originalEntries.get(i);

        if (entryA.isURL() == entryB.isURL() || entryA.equals(entryB))
        {
          // Comparable and (comparable & equal)
          result = true;
          break;
        }
      }
    }

    _isModified = result;

    // if this playlist refers to a file on disk, and aren't a new file, make sure that file still exists...
    if (_file != null && !isNew())
    {
      _isModified = _isModified || !_file.exists();
    }

    // notify the listeners if we have changed...
    if (_isModified)
    {
      firePlaylistModified();
    }
  }

  private void refreshStatus()
  {
    _urlCount = 0;
    _missingCount = 0;
    _fixedCount = 0;

    for (PlaylistEntry entry : _entries)
    {
      boolean entryIsUrl = entry.isURL();
      if (entryIsUrl)
      {
        _urlCount++;
      }
      else if (!entry.isFound())
      {
        _missingCount++;
      }
      if (entry.isFixed())
      {
        _fixedCount++;
      }
    }

    updateModifiedStatus();
  }

  /**
   * @return
   */
  public int getFixedCount()
  {
    return _fixedCount;
  }

  /**
   * @return
   */
  public int getUrlCount()
  {
    return _urlCount;
  }

  /**
   * @return
   */
  public int getMissingCount()
  {
    return _missingCount;
  }

  /**
   * @return
   */
  public boolean isModified()
  {
    return _isModified;
  }

  /**
   * @return
   */
  public String getFilename()
  {
    if (_file == null)
    {
      return "";
    }
    return _file.getName();
  }

  /**
   * @return
   */
  public boolean isEmpty()
  {
    return _entries.isEmpty();
  }

  /**
   *
   */
  public void play()
  {
    try
    {
      _logger.debug(String.format("Launching file: " + _file));
      FileLauncher.launch(_file);
    }
    catch (IOException | InterruptedException e)
    {
      _logger.warn(String.format("Launching file: " + _file), e);
    }
  }

  /**
   * @return
   */
  public boolean isUtfFormat()
  {
    return _utfFormat;
  }

  /**
   * @param utfFormat
   */
  public void setUtfFormat(boolean utfFormat)
  {
    this._utfFormat = utfFormat;
  }

  /**
   * @return
   */
  public boolean isNew()
  {
    return _isNew;
  }

  /**
   * @param index
   * @param newEntry
   */
  public void replace(int index, PlaylistEntry newEntry)
  {

    if (!_entries.get(index).isFound())
    {
      newEntry.markFixedIfFound();
    }
    _entries.set(index, newEntry);
    refreshStatus();
  }

  /**
   * @param indexes
   */
  public void moveUp(int[] indexes)
  {
    Arrays.sort(indexes);
    int ceiling = 0;
    for (int ix = 0; ix < indexes.length; ix++)
    {
      int rowIx = indexes[ix];
      if (rowIx != ceiling)
      {
        Collections.swap(_entries, rowIx, rowIx - 1);
        indexes[ix] = rowIx - 1;
      }
      else
      {
        ceiling++;
      }
    }
    refreshStatus();
  }

  /**
   * @param initialPos
   * @param finalPos
   */
  public void moveTo(int initialPos, int finalPos)
  {
    PlaylistEntry temp = _entries.get(initialPos);
    _entries.remove(initialPos);
    _entries.add(finalPos, temp);
    refreshStatus();
  }

  /**
   * @param indexes
   */
  public void moveDown(int[] indexes)
  {
    Arrays.sort(indexes);
    int floor = _entries.size() - 1;
    for (int ix = indexes.length - 1; ix >= 0; ix--)
    {
      int rowIx = indexes[ix];
      if (rowIx != floor)
      {
        Collections.swap(_entries, rowIx, rowIx + 1);
        indexes[ix] = rowIx + 1;
      }
      else
      {
        floor--;
      }
    }
    refreshStatus();
  }

  /**
   * @param i
   * @param entries
   * @return
   */
  public int addAllAt(int i, List<PlaylistEntry> entries)
  {
    _entries.addAll(i, entries);
    refreshStatus();
    return entries.size();
  }

  /**
   * @param files
   * @param observer
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  public int add(File[] files, IProgressObserver<String> observer) throws FileNotFoundException, IOException
  {
    List<PlaylistEntry> newEntries = getEntriesForFiles(files, observer);
    if (newEntries != null)
    {
      _entries.addAll(newEntries);
      refreshStatus();
      return newEntries.size();
    }
    else
    {
      return 0;
    }
  }

  /**
   * @param ix
   * @param files
   * @param observer
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  public int addAt(int ix, File[] files, IProgressObserver<String> observer) throws IOException
  {
    List<PlaylistEntry> newEntries = getEntriesForFiles(files, observer);
    if (newEntries != null)
    {
      _entries.addAll(ix, newEntries);
      refreshStatus();
      return newEntries.size();
    }
    else
    {
      return 0;
    }
  }

  private List<PlaylistEntry> getEntriesForFiles(File[] files, IProgressObserver<String> observer) throws IOException
  {
    ProgressAdapter<String> progress = ProgressAdapter.wrap(observer);
    progress.setTotal(files.length);

    List<PlaylistEntry> ents = new ArrayList<>();
    for (File file : files)
    {
      if (observer == null || !observer.getCancelled())
      {
        if (Playlist.isPlaylist(file, this.playListOptions))
        {
          // playlist file
          IPlaylistReader reader = PlaylistReaderFactory.getPlaylistReader(file.toPath(), this.playListOptions);
          ents.addAll(reader.readPlaylist(progress));
        }
        else
        {
          // regular file
          ents.add(new FilePlaylistEntry(file.toPath(), null, _file.toPath()));
        }
      }
      else
      {
        return null;
      }

      if (progress.getCompleted() != progress.getTotal())
      {
        progress.stepCompleted();
      }
    }

    return ents;
  }

  public void changeEntryFileName(int ix, String newName)
  {
    PlaylistEntry entry = _entries.get(ix);
    if (entry instanceof FilePlaylistEntry)
    {
      ((FilePlaylistEntry) entry).setFileName(newName);
    }
    refreshStatus();
  }

  /**
   * @param mediaLibrary Media library used to reference existing media files
   * @param observer     Progress observer
   * @return Positions of repaired rows
   */
  public List<Integer> repair(IMediaLibrary mediaLibrary, IProgressObserver observer)
  {
    ProgressAdapter progress = ProgressAdapter.wrap(observer);
    progress.setTotal(_entries.size());

    List<Integer> fixed = new ArrayList<>();
    for (int ix = 0; ix < _entries.size(); ix++)
    {
      if (observer.getCancelled())
      {
        _logger.info(markerPlaylistRepair, "Observer cancelled, quit repair");
        return null;
      }
      progress.stepCompleted();

      PlaylistEntry entry = _entries.get(ix);

      final boolean caseInsensitiveExactMatching = this.playListOptions.getCaseInsensitiveExactMatching();
      final boolean relativePaths = this.playListOptions.getSavePlaylistsWithRelativePaths();

      if (entry instanceof FilePlaylistEntry)
      {
        FilePlaylistEntry fileEntry = (FilePlaylistEntry) entry;
        FilePlaylistEntry filePlaylistEntry = (FilePlaylistEntry) entry;
        if (filePlaylistEntry.isFound())
        {
          _logger.debug(markerPlaylistRepair, "Found " + fileEntry.getTrackPath());
          if (filePlaylistEntry.updatePathToMediaLibraryIfFoundOutside(mediaLibrary, caseInsensitiveExactMatching, relativePaths))
          {
            fixed.add(ix);
            refreshStatus();
          }
        }
        else
        {
          _logger.debug(markerPlaylistRepair, "Search " + fileEntry.getStatus() + " file entry " + fileEntry.getTrackPath());
          filePlaylistEntry.findNewLocationFromFileList(mediaLibrary.getNestedMediaFiles(), caseInsensitiveExactMatching, relativePaths);
          if (entry.isFound())
          {
            _logger.debug(markerPlaylistRepair, "Found & repaired file entry " + fileEntry.getTrackPath());
            fixed.add(ix);
            refreshStatus();
          }
        }
      }
    }
    _logger.info(markerPlaylistRepair, "Completed.");
    return fixed;
  }

  /**
   * @param dirLists Media library used for repair
   * @param observer Progress observer
   */
  public void batchRepair(IMediaLibrary dirLists, IProgressObserver<String> observer)
  {
    this.batchRepair(dirLists.getNestedMediaFiles(), dirLists, observer);
  }

  /**
   * Similar to repair, but doesn't return repaired row information
   *
   * @param fileList Media library used for repair
   * @param observer Progress observer
   */
  public void batchRepair(Collection<String> fileList, IMediaLibrary dirLists, IProgressObserver<String> observer)
  {
    ProgressAdapter<String> progress = ProgressAdapter.wrap(observer);
    progress.setTotal(_entries.size());

    final boolean caseInsensitive = this.playListOptions.getCaseInsensitiveExactMatching();
    final boolean relativePaths = this.playListOptions.getSavePlaylistsWithRelativePaths();
    boolean isModified = false;
    for (PlaylistEntry entry : _entries)
    {
      progress.stepCompleted();

      if (entry instanceof FilePlaylistEntry)
      {
        FilePlaylistEntry filePlaylistEntry = (FilePlaylistEntry) entry;
        if (filePlaylistEntry.isFound())
        {
          if (filePlaylistEntry.updatePathToMediaLibraryIfFoundOutside(dirLists, caseInsensitive, relativePaths))
          {
            isModified = true;
          }
        }
        else
        {
          filePlaylistEntry.findNewLocationFromFileList(fileList, caseInsensitive, relativePaths);
          if (!isModified && entry.isFound())
          {
            isModified = true;
          }

        }
      }
    }

    if (isModified)
    {
      refreshStatus();
    }
  }

  /**
   * @param libraryFiles
   * @param observer
   * @return
   */
  public List<BatchMatchItem> findClosestMatches(Collection<String> libraryFiles, IProgressObserver<String> observer)
  {
    ProgressAdapter<String> progress = ProgressAdapter.wrap(observer);
    progress.setTotal(_entries.size());

    List<BatchMatchItem> fixed = new ArrayList<>();
    PlaylistEntry entry;
    List<PotentialPlaylistEntryMatch> matches;
    for (int ix = 0; ix < _entries.size(); ix++)
    {
      progress.stepCompleted();
      if (!observer.getCancelled())
      {
        entry = _entries.get(ix);
        if (!entry.isURL() && !entry.isFound())
        {
          matches = entry.findClosestMatches(libraryFiles, null, this.playListOptions);
          if (!matches.isEmpty())
          {
            fixed.add(new BatchMatchItem(ix, entry, matches));
          }
        }
      }
      else
      {
        return null;
      }
    }

    return fixed;
  }

  /**
   * @param rowList
   * @param libraryFiles
   * @param observer
   * @return
   */
  public List<BatchMatchItem> findClosestMatchesForSelectedEntries(List<Integer> rowList, Collection<String> libraryFiles, ProgressWorker<List<BatchMatchItem>, String> observer)
  {
    ProgressAdapter progress = ProgressAdapter.wrap(observer);
    progress.setTotal(_entries.size());

    List<BatchMatchItem> fixed = new ArrayList<>();
    PlaylistEntry entry;
    List<PotentialPlaylistEntryMatch> matches;
    for (Integer ix : rowList)
    {
      progress.stepCompleted();
      if (!observer.getCancelled())
      {
        entry = _entries.get(ix);
        if (!entry.isURL() && !entry.isFound())
        {
          matches = entry.findClosestMatches(libraryFiles, null, this.playListOptions);
          if (!matches.isEmpty())
          {
            fixed.add(new BatchMatchItem(ix, entry, matches));
          }
        }
      }
      else
      {
        return null;
      }
    }

    return fixed;
  }

  /**
   * @param items
   * @return
   */
  public List<Integer> applyClosestMatchSelections(List<BatchMatchItem> items)
  {
    List<Integer> fixed = new ArrayList<>();
    for (BatchMatchItem item : items)
    {
      if (item.getSelectedIx() >= 0)
      {
        int ix = item.getEntryIx();
        _entries.set(ix, item.getSelectedMatch().getPlaylistFile());
        PlaylistEntry tempEntry = _entries.get(ix);
        tempEntry.recheckFoundStatus();
        tempEntry.markFixedIfFound();
        if (tempEntry.isFixed())
        {
          fixed.add(ix);
        }
      }
    }

    if (!fixed.isEmpty())
    {
      refreshStatus();
    }
    return fixed;
  }

  /**
   * @param index
   * @return
   */
  public PlaylistEntry get(int index)
  {
    return _entries.get(index);
  }

  /**
   * @param indexes
   */
  public void remove(int[] indexes)
  {
    Arrays.sort(indexes);
    for (int ix = indexes.length - 1; ix >= 0; ix--)
    {
      int rowIx = indexes[ix];
      _entries.remove(rowIx);
    }
    refreshStatus();
  }

  /**
   * @param entry
   * @return
   */
  public int remove(PlaylistEntry entry)
  {
    int result = _entries.indexOf(entry);
    _entries.remove(entry);
    refreshStatus();
    return result;
  }

  /**
   * @param sortIx
   * @param isDescending
   */
  public void reorder(SortIx sortIx, boolean isDescending)
  {
    switch (sortIx)
    {
      case Filename:
      case Path:
      case Status:
        Collections.sort(_entries, new EntryComparator(sortIx, isDescending));
        break;

      case Random:
        Collections.shuffle(_entries);
        break;

      case Reverse:
        Collections.reverse(_entries);
        break;
    }
    refreshStatus();
  }

  private static class EntryComparator implements Comparator<PlaylistEntry>
  {
    EntryComparator(SortIx sortIx, boolean isDescending)
    {
      _sortIx = sortIx;
      _isDescending = isDescending;
    }

    private final SortIx _sortIx;
    private final boolean _isDescending;

    @Override
    public int compare(PlaylistEntry lhs, PlaylistEntry rhs)
    {
      int rc = 0;

      switch (_sortIx)
      {
        case Filename:
          rc = lhs.getTrackFileName().compareToIgnoreCase(rhs.getTrackFileName());
          break;
        case Path:
          rc = lhs.getTrackFolder().compareToIgnoreCase(rhs.getTrackFolder());
          break;
        case Status:
          // Randomly chosen order... Found > Fixed > Missing > URL (seemed reasonable)
          if (lhs.isURL())
          {
            if (rhs.isURL())
            {
              rc = 0;
              break;
            }
            rc = 1;
            break;
          }
          else if (!lhs.isFound())
          {
            if (rhs.isURL())
            {
              rc = -1;
              break;
            }
            else if (!rhs.isFound())
            {
              rc = 0;
              break;
            }
            rc = 1;
            break;
          }
          else if (lhs.isFixed())
          {
            if (!rhs.isFound() || rhs.isURL())
            {
              rc = -1;
              break;
            }
            else if (rhs.isFixed())
            {
              rc = 0;
              break;
            }
            rc = 1;
            break;
          }
          rc = -1;
          break;
      }

      return _isDescending ? -rc : rc;
    }
  }

  /**
   * @return
   */
  public int removeDuplicates()
  {
    int removed = 0;
    Set<String> found = new HashSet<>();
    for (int ix = 0; ix < _entries.size();)
    {
      PlaylistEntry entry = _entries.get(ix);
      String name = entry.getTrackFileName();
      if (found.contains(name))
      {
        // duplicate found, remove
        _entries.remove(ix);
        removed++;
      }
      else
      {
        found.add(name);
        ix++;
      }
    }
    if (removed > 0)
    {
      refreshStatus();
    }
    return removed;
  }

  /**
   * @return
   */
  public int removeMissing()
  {
    int removed = 0;
    for (int ix = _entries.size() - 1; ix >= 0; ix--)
    {
      PlaylistEntry entry = _entries.get(ix);
      if (!entry.isURL() && !entry.isFound())
      {
        _entries.remove(ix);
        removed++;
      }
    }
    if (removed > 0)
    {
      refreshStatus();
    }
    return removed;
  }

  /**
   * @param destination
   * @param observer
   * @throws Exception
   */
  public void saveAs(File destination, IProgressObserver<String> observer) throws Exception
  {
    // 2014.12.08 - JCaron - Need to make this assignment so we can determine relativity correctly when saving out entries.
    setFile(destination);
    _type = determinePlaylistTypeFromExtension(destination, this.playListOptions);
    if (_type == PlaylistType.PLS)
    {
      // apparently winamp shits itself if PLS files are saved in UTF-8 (who knew...)
      // but, since VLC supports UTF-8 formatted PLS files, and winamp can't read the filenames right
      // when we don't save as UTF-8 anyway, I'm removing this restriction
      // _utfFormat = false;
    }
    save(this.playListOptions.getSavePlaylistsWithRelativePaths(), observer);
  }

  /**
   * @param saveRelative
   * @param observer
   * @throws Exception
   */
  public final void save(boolean saveRelative, IProgressObserver<String> observer) throws Exception
  {
    // avoid resetting total if part of batch operation
    boolean hasTotal = observer instanceof ProgressAdapter;
    ProgressAdapter<String> progress = ProgressAdapter.wrap(observer);
    if (!hasTotal)
    {
      progress.setTotal(_entries.size());
    }

    IPlaylistWriter writer = PlaylistWriterFactory.getPlaylistWriter(_file, this.playListOptions);
    writer.save(this, saveRelative, progress);

    resetInternalStateAfterSave(observer);

    // need to fire this so that the tab showing this playlist updates its text & status
    firePlaylistModified();
  }

  private void quickSave() throws Exception
  {
    IPlaylistWriter writer = PlaylistWriterFactory.getPlaylistWriter(_file, this.playListOptions);
    _logger.debug(String.format("Writing playlist to %s", _file.getName()));
    writer.save(this, false, null);
  }

  /**
   * @param observer
   * @throws IOException
   */
  public void reload(IProgressObserver observer) throws IOException
  {
    if (_isNew)
    {
      _file = new File(HOME_DIR + FS + "Untitled-" + NEW_LIST_COUNT + ".m3u8");
      _file.deleteOnExit();
      _utfFormat = true;
      _type = PlaylistType.M3U;
      _isModified = false;
      _isNew = true;
      _entries.clear();
      _originalEntries.clear();
    }
    else if (_file.exists())
    {
      init(_file, observer);
    }
    else
    {
      replaceEntryListContents(_originalEntries, _entries);
    }
    refreshStatus();
  }

  /**
   * @param input
   * @return
   */
  public static boolean isPlaylist(File input, IPlaylistOptions filePathOptions)
  {
    return determinePlaylistTypeFromExtension(input, filePathOptions) != PlaylistType.UNKNOWN;
  }

  /**
   * @param input
   * @return
   */
  public static PlaylistType determinePlaylistTypeFromExtension(File input, IPlaylistOptions filePathOptions)
  {
    if (input != null)
    {
      String lowerCaseExtension = (new FileNameTokenizer(filePathOptions)).getExtensionFromFileName(input.getName()).toLowerCase();
      switch (lowerCaseExtension)
      {
        case "m3u":
        case "m3u8":
          return PlaylistType.M3U;
        case "pls":
          return PlaylistType.PLS;
        case "wpl":
          return PlaylistType.WPL;
        case "xspf":
          return PlaylistType.XSPF;
        case "xml":
          return PlaylistType.ITUNES;
      }
    }
    return PlaylistType.UNKNOWN;
  }
}
