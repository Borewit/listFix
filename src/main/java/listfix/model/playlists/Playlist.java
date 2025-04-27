package listfix.model.playlists;

import io.github.borewit.lizzy.content.Content;
import io.github.borewit.lizzy.playlist.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.filechooser.FileSystemView;
import listfix.config.IMediaLibrary;
import listfix.io.FileUtils;
import listfix.io.IPlaylistOptions;
import listfix.io.playlists.LizzyPlaylistUtil;
import listfix.io.progress.ObservableInputStream;
import listfix.io.progress.ObservableOutputStream;
import listfix.model.BatchMatchItem;
import listfix.view.support.IPlaylistModifiedListener;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;
import listfix.view.support.ProgressWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class Playlist {
  private static final String DEFAULT_SAVE_FOLDER =
      FileSystemView.getFileSystemView().getDefaultDirectory().getPath();

  private static int NEW_LIST_COUNT = -1;

  private static final Marker markerPlaylist = MarkerManager.getMarker("Playlist");
  private static final Marker markerPlaylistRepair =
      MarkerManager.getMarker("Playlist-Repair").setParents(markerPlaylist);

  private Path playlistPath;
  private SpecificPlaylist specificPlaylist;
  private final List<PlaylistEntry> _entries = new ArrayList<>();
  private final List<PlaylistEntry> _originalEntries = new ArrayList<>();
  private int _fixedCount;
  private int _urlCount;
  private int _missingCount;
  private boolean isModified;
  private boolean isUnsaved;
  private static final Logger _logger = LogManager.getLogger(Playlist.class);

  private final IPlaylistOptions playListOptions;

  private final List<IPlaylistModifiedListener> _listeners = new ArrayList<>();

  private static final PlaylistFormat defaultPlaylistFormat = PlaylistFormat.m3u;
  private static final String defaultPlaylistExtension = "m3u8";

  public static Playlist load(
      Path playlistPath, IProgressObserver<String> observer, IPlaylistOptions playListOptions)
      throws IOException {
    SpecificPlaylist specificPlaylist = LizzyPlaylistUtil.readPlaylist(playlistPath);
    Playlist playlist = new Playlist(playlistPath, playListOptions, specificPlaylist);
    playlist.load(observer);
    playlist.isUnsaved = false;
    return playlist;
  }

  private void load(IProgressObserver<String> observer) throws IOException {
    final List<PlaylistEntry> playlistEntries = new ArrayList<>();
    this.loadPlaylistEntries(playlistEntries, playlistPath, observer);
    this.setEntries(playlistEntries);
    this.isModified = false;
    this.refreshStatus();
  }

  public static Playlist makeTemporaryPlaylist(
      IPlaylistOptions playListOptions, io.github.borewit.lizzy.playlist.Playlist playlist)
      throws IOException {
    String extension = LizzyPlaylistUtil.getPreferredExtensionFor(defaultPlaylistFormat);

    // Create temporary file
    Path tempFile = Files.createTempFile("yay", extension);

    // Generate empty playlist file, which will be deleted upon exiting application
    SpecificPlaylist specificPlaylist =
        LizzyPlaylistUtil.writeNewPlaylist(
            playlist, tempFile, defaultPlaylistFormat, StandardOpenOption.DELETE_ON_CLOSE);

    Playlist newPlaylist = new Playlist(tempFile, playListOptions, specificPlaylist);
    newPlaylist.isModified = false;
    newPlaylist.refreshStatus();
    newPlaylist.quickSave();
    return newPlaylist;
  }

  public static Playlist makeNewPersistentPlaylist(
      String extension, IPlaylistOptions playListOptions)
      throws IOException, PlaylistProviderNotFoundException {
    final Path path = getNewPlaylistFilename(extension);
    SpecificPlaylistFactory specificPlaylistFactory = SpecificPlaylistFactory.getInstance();
    List<SpecificPlaylistProvider> providers =
        specificPlaylistFactory.findProvidersByExtension(extension);
    if (!providers.isEmpty()) {
      final io.github.borewit.lizzy.playlist.Playlist playlist =
          new io.github.borewit.lizzy.playlist.Playlist();
      SpecificPlaylist specificPlaylist = providers.get(0).toSpecificPlaylist(playlist);
      return new Playlist(path, playListOptions, specificPlaylist);
    }
    throw new PlaylistProviderNotFoundException(
        "Could not find a playlist provided for " + extension);
  }

  /**
   * Initializes a playlist with an externally created set of entries. Currently used when reading
   * playlists with external code.
   */
  public Playlist(Path playlistPath, IPlaylistOptions playListOptions, SpecificPlaylist playlist) {
    assert playlistPath != null;
    this.playlistPath = playlistPath;
    this.playListOptions = playListOptions;
    this.specificPlaylist = playlist;
    this.isUnsaved = true;
    this.isModified = false;
    toPlaylistEntries(this._entries, playlist.toPlaylist().getRootSequence());
    refreshStatus();
  }

  private List<PlaylistEntry> getEntriesForFiles(
      Collection<Path> files, IProgressObserver<String> observer) throws IOException {
    ProgressAdapter<String> progress = ProgressAdapter.make(observer);
    progress.setTotal(files.size());

    final List<PlaylistEntry> ents = new ArrayList<>();
    for (Path trackPath : files) {
      if (observer == null || !observer.getCancelled()) {
        if (LizzyPlaylistUtil.isPlaylist(trackPath)) {
          // playlist file
          loadPlaylistEntries(ents, trackPath, null); // ToDo: nest observable
        } else {
          ents.add(makeEntry(trackPath));
        }
      } else {
        return null;
      }

      if (progress.getCompleted() != progress.getTotal()) {
        progress.stepCompleted();
      }
    }

    return ents;
  }

  private FilePlaylistEntry makeEntry(Path trackPath) {
    return new FilePlaylistEntry(
        this, new Media(new Content(normalizeTrackPath(trackPath).toString())));
  }

  private void loadPlaylistEntries(
      List<PlaylistEntry> playlistEntries, Path playlistPath, IProgressObserver<String> observer)
      throws IOException {
    List<SpecificPlaylistProvider> specificPlaylistProviders =
        SpecificPlaylistFactory.getInstance().findProvidersByExtension(playlistPath.toString());

    for (SpecificPlaylistProvider specificPlaylistProvider : specificPlaylistProviders) {
      try (InputStream is = Files.newInputStream(playlistPath)) {
        final InputStream observableInputStream =
            observer == null
                ? is
                : new ObservableInputStream(is, Files.size(playlistPath), observer);
        try {
          this.specificPlaylist = specificPlaylistProvider.readFrom(observableInputStream);
          if (this.specificPlaylist != null) {
            toPlaylistEntries(
                playlistEntries, this.specificPlaylist.toPlaylist().getRootSequence());
            break;
          }
        } catch (Exception e) {
          throw new IOException(
              String.format("Failed to read from %s", playlistPath.getFileName()), e);
        }
      }
    }
  }

  private void toPlaylistEntries(List<PlaylistEntry> playlistEntries, Sequence sequence) {
    sequence
        .getComponents()
        .forEach(
            component -> {
              if (component instanceof Media) {
                Media media = (Media) component;
                PlaylistEntry playlistEntry = PlaylistEntry.makePlaylistEntry(this, media);
                playlistEntries.add(playlistEntry);
              } else if (component instanceof Sequence) {
                toPlaylistEntries(playlistEntries, (Sequence) component);
              } else {
                _logger.warn(
                    String.format(
                        "Unsupport playlist entry type %s", component.getClass().getName()));
              }
            });
  }

  public List<PlaylistEntry> getEntries() {
    return _entries;
  }

  public void copySelectedEntries(
      List<Integer> entryIndexList, File destinationDirectory, IProgressObserver<String> observer) {
    ProgressAdapter<String> progress = ProgressAdapter.make(observer);
    progress.setTotal(entryIndexList.size());
    PlaylistEntry tempEntry;
    Path fileToCopy;
    Path dest;
    for (Integer entryIndexList1 : entryIndexList) {
      if (!observer.getCancelled()) {
        tempEntry = this.get(entryIndexList1);
        if (tempEntry instanceof FilePlaylistEntry) {
          fileToCopy = ((FilePlaylistEntry) tempEntry).getAbsolutePath();
          if (tempEntry.isFound()) // && fileToCopy.exists())
          {
            dest = Path.of(destinationDirectory.getPath(), tempEntry.getTrackFileName());
            try {
              Files.copy(fileToCopy, dest, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
              // eat the error and continue
              throw new RuntimeException("Error copying file", e);
            }
          }
        }
        progress.stepCompleted();
      } else {
        return;
      }
    }
  }

  protected void resetInternalStateAfterSave(IProgressObserver<String> observer) {
    // change original _entries
    replaceEntryListContents(_entries, _originalEntries);
    this.isModified = false;
    this.isUnsaved = false;

    // set entries unfixed if we're being watched...
    // (otherwise writing out a temp file for playback, at least right now)
    if (observer != null) {
      for (PlaylistEntry entry : _entries) {
        entry.setFixed(false);
      }
    }

    refreshStatus();
  }

  public enum SortIx {
    None,
    Filename,
    Path,
    Status,
    Random,
    Reverse
  }

  /**
   * Used to generate a temporary playlist to play selected entries
   *
   * @param rows Row index numbers of selected entries
   * @return Playlist which contains selected entries
   */
  public Playlist getSublist(int[] rows) throws IOException {
    return getSublist(Arrays.stream(rows).mapToObj(_entries::get).collect(Collectors.toList()));
  }

  public Playlist getSublist(Collection<PlaylistEntry> entries) throws IOException {
    final io.github.borewit.lizzy.playlist.Playlist newPlaylist =
        new io.github.borewit.lizzy.playlist.Playlist();
    final Sequence rootSequence = newPlaylist.getRootSequence();
    for (PlaylistEntry entry : entries) {
      String srcUri;
      if (entry instanceof FilePlaylistEntry) {
        // Normalize to absolute path
        Path trackPath = ((FilePlaylistEntry) entry).trackPath;
        srcUri =
            trackPath.isAbsolute()
                ? trackPath.toString()
                : this.playlistPath.getParent().resolve(trackPath).toString();
      } else {
        srcUri = entry.getMedia().getSource().toString();
      }
      Media media = new Media(new Content(srcUri));
      rootSequence.addComponent(media);
    }

    return makeTemporaryPlaylist(this.playListOptions, newPlaylist);
  }

  public List<PlaylistEntry> getSelectedEntries(int[] rows) throws IOException {
    List<PlaylistEntry> tempList = new ArrayList<>();
    for (int i : rows) {
      tempList.add(_entries.get(i));
    }
    return tempList;
  }

  public PlaylistFormat getType() {
    return PlaylistFormat.valueOf(this.specificPlaylist.getProvider().getId());
  }

  public File getFile() {
    return this.playlistPath.toFile();
  }

  public Path getPath() {
    return this.playlistPath;
  }

  public void setPath(Path playlistPath) {
    assert playlistPath != null;
    this.playlistPath = playlistPath;
  }

  public void addModifiedListener(IPlaylistModifiedListener listener) {
    if (listener != null) {
      if (!_listeners.contains(listener)) {
        _listeners.add(listener);
      }
    }
  }

  public void removeModifiedListener(IPlaylistModifiedListener listener) {
    _listeners.remove(listener);
  }

  private void firePlaylistModified() {
    for (IPlaylistModifiedListener listener : _listeners) {
      if (listener != null) {
        listener.playlistModified(this);
      }
    }
  }

  private void setEntries(List<PlaylistEntry> aEntries) {
    replaceEntryListContents(aEntries, _originalEntries);
    replaceEntryListContents(aEntries, _entries);
  }

  private void replaceEntryListContents(List<PlaylistEntry> src, List<PlaylistEntry> dest) {
    dest.clear();
    for (PlaylistEntry src1 : src) {
      dest.add((PlaylistEntry) src1.clone());
    }
  }

  public int size() {
    return _entries.size();
  }

  public void updateModifiedStatus() {
    // Run a full comparison against the original entry list we created when we were constructed
    this.isModified = !_entries.equals(_originalEntries);

    // if this playlist refers to a file on disk, and aren't a new file, make sure that file still
    // exists...
    if (this.playlistPath != null && !isNew()) {
      this.isModified = this.isModified || !Files.exists(this.playlistPath);
    }

    // notify the listeners if we have changed...
    this.firePlaylistModified();
  }

  private void refreshStatus() {
    _urlCount = 0;
    _missingCount = 0;
    _fixedCount = 0;

    for (PlaylistEntry entry : _entries) {
      boolean entryIsUrl = entry.isURL();
      if (entryIsUrl) {
        _urlCount++;
      } else if (!entry.isFound()) {
        _missingCount++;
      }
      if (entry.isFixed()) {
        _fixedCount++;
      }
    }

    updateModifiedStatus();
  }

  public int getFixedCount() {
    return this._fixedCount;
  }

  public int getUrlCount() {
    return this._urlCount;
  }

  public int getMissingCount() {
    return this._missingCount;
  }

  public boolean isModified() {
    return this.isModified;
  }

  public String getFilename() {
    return this.playlistPath == null ? "" : this.playlistPath.getFileName().toString();
  }

  public boolean isEmpty() {
    return this._entries.isEmpty();
  }

  public void play() {
    try {
      _logger.debug(String.format("Launching file: %s", this.playlistPath));
      Desktop.getDesktop().open(this.playlistPath.toFile());
    } catch (IOException e) {
      _logger.warn(String.format("Failed to launching file: %s", this.playlistPath), e);
    }
  }

  public boolean isNew() {
    return this.isUnsaved;
  }

  public void replace(int index, PlaylistEntry newEntry) {

    if (!this._entries.get(index).isFound()) {
      newEntry.markFixedIfFound();
    }
    this._entries.set(index, newEntry);
    refreshStatus();
  }

  public void moveUp(int[] indexes) {
    Arrays.sort(indexes);
    int ceiling = 0;
    for (int ix = 0; ix < indexes.length; ix++) {
      int rowIx = indexes[ix];
      if (rowIx != ceiling) {
        Collections.swap(_entries, rowIx, rowIx - 1);
        indexes[ix] = rowIx - 1;
      } else {
        ceiling++;
      }
    }
    refreshStatus();
  }

  public void moveDown(int[] indexes) {
    Arrays.sort(indexes);
    int floor = _entries.size() - 1;
    for (int ix = indexes.length - 1; ix >= 0; ix--) {
      int rowIx = indexes[ix];
      if (rowIx != floor) {
        Collections.swap(this._entries, rowIx, rowIx + 1);
        indexes[ix] = rowIx + 1;
      } else {
        floor--;
      }
    }
    refreshStatus();
  }

  public int addAllAt(int i, List<PlaylistEntry> entries) {
    this._entries.addAll(i, entries);
    refreshStatus();
    return entries.size();
  }

  public int add(Collection<Path> files, IProgressObserver<String> observer) throws IOException {
    List<PlaylistEntry> newEntries = getEntriesForFiles(files, observer);
    if (newEntries != null) {
      this._entries.addAll(newEntries);
      refreshStatus();
      return newEntries.size();
    } else {
      return 0;
    }
  }

  public int addAt(int ix, Collection<Path> files, IProgressObserver<String> observer)
      throws IOException {
    List<PlaylistEntry> newEntries = getEntriesForFiles(files, observer);
    if (newEntries != null) {
      this._entries.addAll(ix, newEntries);
      refreshStatus();
      return newEntries.size();
    } else {
      return 0;
    }
  }

  public void changeEntryFileName(int ix, String newName) {
    PlaylistEntry entry = this._entries.get(ix);
    if (entry instanceof FilePlaylistEntry) {
      ((FilePlaylistEntry) entry).setFileName(newName);
    }
    refreshStatus();
  }

  /**
   * Returns positions of repaired rows.
   *
   * @param mediaLibrary Media library used to reference existing media files
   * @param observer Progress observer
   */
  public List<PlaylistEntry> repair(
      IMediaLibrary mediaLibrary, IProgressObserver<String> observer) {
    ProgressAdapter<String> progress = ProgressAdapter.make(observer);
    progress.setTotal(this._entries.size());

    final long start = System.currentTimeMillis();

    List<PlaylistEntry> fixed =
        _entries.parallelStream()
            .filter(
                entry -> {
                  if (observer.getCancelled()) {
                    _logger.info(markerPlaylistRepair, "Observer cancelled, quit repair");
                    return false;
                  }
                  progress.stepCompleted();

                  final boolean caseInsensitiveExactMatching =
                      this.playListOptions.getCaseInsensitiveExactMatching();
                  final boolean relativePaths =
                      this.playListOptions.getSavePlaylistsWithRelativePaths();

                  if (entry instanceof FilePlaylistEntry) {
                    FilePlaylistEntry fileEntry = (FilePlaylistEntry) entry;
                    FilePlaylistEntry filePlaylistEntry = (FilePlaylistEntry) entry;
                    if (filePlaylistEntry.isFound()) {
                      _logger.debug(markerPlaylistRepair, "Found " + fileEntry.getTrackPath());
                      if (filePlaylistEntry.updatePathToMediaLibraryIfFoundOutside(
                          mediaLibrary, caseInsensitiveExactMatching, relativePaths)) {
                        this.refreshStatus();
                        return true;
                      }
                    } else {
                      _logger.debug(
                          markerPlaylistRepair,
                          "Search "
                              + fileEntry.getStatus()
                              + " file entry "
                              + fileEntry.getTrackPath());
                      filePlaylistEntry.findNewLocationFromFileList(
                          mediaLibrary.getNestedMediaFiles(),
                          caseInsensitiveExactMatching,
                          relativePaths);
                      if (entry.isFound()) {
                        _logger.debug(
                            markerPlaylistRepair,
                            "Found & repaired file entry " + fileEntry.getTrackPath());
                        this.refreshStatus();
                        return true;
                      }
                    }
                  }
                  return false;
                })
            .collect(Collectors.toList());

    long timeElapsed = System.currentTimeMillis() - start;
    _logger.info("Repaired playlist in " + timeElapsed + " ms.");
    return fixed;
  }

  /**
   * @param dirLists Media library used for repair
   * @param observer Progress observer
   */
  public void batchRepair(IMediaLibrary dirLists, IProgressObserver<String> observer) {
    this.batchRepair(dirLists.getNestedMediaFiles(), dirLists, observer);
  }

  /**
   * Similar to repair, but doesn't return repaired row information
   *
   * @param fileList Media library used for repair
   * @param observer Progress observer
   */
  public void batchRepair(
      Collection<String> fileList, IMediaLibrary dirLists, IProgressObserver<String> observer) {
    ProgressAdapter<String> progress = ProgressAdapter.make(observer);
    progress.setTotal(_entries.size());

    final boolean caseInsensitive = this.playListOptions.getCaseInsensitiveExactMatching();
    final boolean relativePaths = this.playListOptions.getSavePlaylistsWithRelativePaths();
    boolean isModified = false;
    for (PlaylistEntry entry : _entries) {
      progress.stepCompleted();

      if (entry instanceof FilePlaylistEntry) {
        FilePlaylistEntry filePlaylistEntry = (FilePlaylistEntry) entry;
        if (filePlaylistEntry.isFound()) {
          if (filePlaylistEntry.updatePathToMediaLibraryIfFoundOutside(
              dirLists, caseInsensitive, relativePaths)) {
            isModified = true;
          }
        } else {
          filePlaylistEntry.findNewLocationFromFileList(fileList, caseInsensitive, relativePaths);
          if (!isModified && entry.isFound()) {
            isModified = true;
          }
        }
      }
    }

    if (isModified) {
      refreshStatus();
    }
  }

  public List<BatchMatchItem> findClosestMatches(
      Collection<String> libraryFiles, IProgressObserver<String> observer) {
    return findClosestMatches(this._entries, libraryFiles, observer);
  }

  public List<BatchMatchItem> findClosestMatches(
      List<PlaylistEntry> entries,
      Collection<String> libraryFiles,
      IProgressObserver<String> observer) {
    final long start = System.currentTimeMillis();

    final ProgressAdapter<String> progress = ProgressAdapter.make(observer);
    progress.setTotal(entries.size());

    final List<BatchMatchItem> needToBeFixed = new ArrayList<>();

    entries.parallelStream()
        .forEach(
            entry -> {
              if (observer.getCancelled()) return;
              if (!entry.isURL() && !entry.isFound()) {
                List<PotentialPlaylistEntryMatch> matches =
                    entry.findClosestMatches(libraryFiles, null, this.playListOptions);
                if (!matches.isEmpty()) {
                  needToBeFixed.add(new BatchMatchItem(entry, matches));
                }
              }
              progress.stepCompleted();
            });

    long timeElapsed = System.currentTimeMillis() - start;
    _logger.info("Resolved closest matches in " + timeElapsed + " ms.");

    return needToBeFixed;
  }

  public List<BatchMatchItem> findClosestMatchesForSelectedEntries(
      List<Integer> rowList,
      Collection<String> libraryFiles,
      ProgressWorker<List<BatchMatchItem>, String> observer) {
    List<PlaylistEntry> entrySelection =
        rowList.stream().map(this._entries::get).collect(Collectors.toUnmodifiableList());
    return findClosestMatches(entrySelection, libraryFiles, observer);
  }

  public List<PlaylistEntry> applyClosestMatchSelections(List<BatchMatchItem> items) {
    List<PlaylistEntry> fixed = new ArrayList<>();
    for (BatchMatchItem item : items) {
      if (item.getSelectedIx() >= 0) {
        final PlaylistEntry playlistEntry = item.getEntry();
        if (playlistEntry instanceof FilePlaylistEntry) {
          ((FilePlaylistEntry) playlistEntry).update(item.getSelectedMatch().getTrack());
        } else {
          throw new UnsupportedOperationException("ToDo");
        }
        playlistEntry.recheckFoundStatus();
        playlistEntry.markFixedIfFound();
        if (playlistEntry.isFixed()) {
          fixed.add(playlistEntry);
        }
      }
    }

    if (!fixed.isEmpty()) {
      refreshStatus();
    }
    return fixed;
  }

  public PlaylistEntry get(int index) {
    return _entries.get(index);
  }

  public void remove(int[] indexes) {
    Arrays.sort(indexes);
    for (int ix = indexes.length - 1; ix >= 0; ix--) {
      int rowIx = indexes[ix];
      _entries.remove(rowIx);
    }
    refreshStatus();
  }

  public int remove(PlaylistEntry entry) {
    int result = _entries.indexOf(entry);
    _entries.remove(entry);
    refreshStatus();
    return result;
  }

  public void reorder(SortIx sortIx, boolean isDescending) {
    switch (sortIx) {
      case Filename, Path, Status -> _entries.sort(new EntryComparator(sortIx, isDescending));
      case Random -> Collections.shuffle(_entries);
      case Reverse -> Collections.reverse(_entries);
    }
    refreshStatus();
  }

  private static class EntryComparator implements Comparator<PlaylistEntry> {
    EntryComparator(SortIx sortIx, boolean isDescending) {
      _sortIx = sortIx;
      _isDescending = isDescending;
    }

    private final SortIx _sortIx;
    private final boolean _isDescending;

    @Override
    public int compare(PlaylistEntry lhs, PlaylistEntry rhs) {
      int rc = 0;

      // Randomly chosen order... Found > Fixed > Missing > URL (seemed reasonable)
      switch (_sortIx) {
        case Filename:
          rc = lhs.getTrackFileName().compareToIgnoreCase(rhs.getTrackFileName());
          break;
        case Path:
          rc = lhs.getTrackFolder().compareToIgnoreCase(rhs.getTrackFolder());
          break;
        case Status:
          if (lhs.isURL()) {
            if (rhs.isURL()) {
              rc = 0;
              break;
            }
            rc = 1;
            break;
          } else if (!lhs.isFound()) {
            if (rhs.isURL()) {
              rc = -1;
              break;
            } else if (!rhs.isFound()) {
              rc = 0;
              break;
            }
            rc = 1;
            break;
          } else if (lhs.isFixed()) {
            if (!rhs.isFound() || rhs.isURL()) {
              rc = -1;
              break;
            } else if (rhs.isFixed()) {
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

  public int removeDuplicates() {
    int removed = 0;
    Set<String> found = new HashSet<>();
    for (int ix = 0; ix < _entries.size(); ) {
      PlaylistEntry entry = _entries.get(ix);
      String name = entry.getTrackFileName();
      if (found.contains(name)) {
        // duplicate found, remove
        _entries.remove(ix);
        removed++;
      } else {
        found.add(name);
        ix++;
      }
    }
    if (removed > 0) {
      refreshStatus();
    }
    return removed;
  }

  public int removeMissing() {
    int removed = 0;
    for (int ix = _entries.size() - 1; ix >= 0; ix--) {
      PlaylistEntry entry = _entries.get(ix);
      if (!entry.isURL() && !entry.isFound()) {
        _entries.remove(ix);
        removed++;
      }
    }
    if (removed > 0) {
      refreshStatus();
    }
    return removed;
  }

  public void saveAs(Path destination, PlaylistFormat format, IProgressObserver<String> observer)
      throws Exception {
    // 2014.12.08 - JCaron - Need to make this assignment,
    // so we can determine relativity correctly when saving out entries.
    setPath(destination);
    this.save(format, observer);
  }

  /** Sync all changes tot his.specificPlaylist */
  protected void syncEntriesToSpecificPlaylist(PlaylistFormat format) throws IOException {
    io.github.borewit.lizzy.playlist.Playlist playlist =
        new io.github.borewit.lizzy.playlist.Playlist();
    Sequence sequence = playlist.getRootSequence();
    this._entries.forEach(
        entry -> {
          Media media = entry.getMedia();
          if (entry.isURL()) {
            media.getSource().setURI(((UriPlaylistEntry) entry).getURI());
          } else {
            FilePlaylistEntry fileEntry = (FilePlaylistEntry) entry;
            fileEntry.trackPath = normalizeTrackPath(fileEntry.trackPath);
            media.getSource().setURL(fileEntry.trackPath.toString());
          }
          sequence.addComponent(media);
        });
    this.specificPlaylist = LizzyPlaylistUtil.getProvider(format).toSpecificPlaylist(playlist);
  }

  private Path normalizeTrackPath(Path trackPath) {
    if (this.playListOptions.getSavePlaylistsWithRelativePaths()) {
      if (trackPath.isAbsolute() && this.playlistPath.getParent() != null) {
        try {
          return this.playlistPath.getParent().relativize(trackPath);
        } catch (IllegalArgumentException ignore) {
          // Maybe thrown if the track and playlist have no common root
        }
      }
      return trackPath;
    } else {
      if (!trackPath.isAbsolute() && this.playlistPath.getParent() != null) {
        return this.playlistPath.getParent().resolve(trackPath);
      }
      return trackPath;
    }
  }

  public final void save(PlaylistFormat format, IProgressObserver<String> observer)
      throws IOException {
    this.syncEntriesToSpecificPlaylist(format);
    _logger.info(String.format("Writing playlist to %s", this.playlistPath));
    // avoid resetting total if part of batch operation

    // Guess the future file length, to have progress indication
    long currentFileSize =
        Files.isRegularFile(this.playlistPath)
            ? Files.size(this.playlistPath)
            : this._entries.size() * 65L;
    try (OutputStream os = Files.newOutputStream(this.playlistPath)) {
      OutputStream observableOutputStream =
          observer == null ? os : new ObservableOutputStream(os, currentFileSize, observer);
      try {
        this.specificPlaylist.writeTo(observableOutputStream);
      } catch (Exception e) {
        throw new IOException(String.format("Failed to save \"%s\"", this.playlistPath), e);
      }
    }

    resetInternalStateAfterSave(observer);

    // need to fire this so that the tab showing this playlist updates its text & status
    firePlaylistModified();
  }

  private void quickSave() throws IOException {
    this.save(this.getType(), null);
  }

  /** Reload playlist and reset unsaved user changes */
  public void reload(IProgressObserver<String> observer) throws IOException {
    if (isUnsaved) {
      this.playlistPath = getNewPlaylistFilename(defaultPlaylistExtension);
      this.playlistPath.toFile().deleteOnExit();
      this.isModified = false;
      isUnsaved = true;
      _entries.clear();
      _originalEntries.clear();
    } else if (Files.exists(this.playlistPath)) {
      this.load(observer);
    } else {
      replaceEntryListContents(_originalEntries, _entries);
    }
    refreshStatus();
  }

  public static synchronized Path getNewPlaylistFilename(String pathOrExtension) {
    final String extension =
        pathOrExtension.contains(".")
            ? FileUtils.getFileExtension(pathOrExtension)
            : pathOrExtension;
    Path path;
    do {
      ++NEW_LIST_COUNT;
      path = Path.of(DEFAULT_SAVE_FOLDER, "Untitled-" + NEW_LIST_COUNT + "." + extension);
    } while (Files.exists(path));
    return path;
  }

  public int indexOf(PlaylistEntry playlistEntry) {
    return this._entries.indexOf(playlistEntry);
  }
}
