package listfix.model.playlists;

import io.github.borewit.lizzy.content.Content;
import io.github.borewit.lizzy.playlist.Media;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import listfix.comparators.MatchedPlaylistEntryComparator;
import listfix.io.IPlaylistOptions;
import listfix.model.enums.PlaylistEntryStatus;
import listfix.util.FileNameTokenizer;
import listfix.util.UnicodeUtils;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;

public abstract class PlaylistEntry implements Cloneable {
  // logger
  /** The list of folders we know don't exist. */
  public static List<String> NonExistentDirectories = new ArrayList<>();

  // Status of this item.
  protected PlaylistEntryStatus _status = PlaylistEntryStatus.Exists;

  // Has this item been fixed?
  protected boolean _isFixed;

  // The file this entry belongs to.
  protected final Playlist playlist;
  protected final Media media;

  private static final Pattern APOS_PATTERN = Pattern.compile("'");
  private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("(([a-z])([A-Z]))");

  /** Returns the _status. */
  public PlaylistEntryStatus getStatus() {
    return _status;
  }

  // Construct an M3U URL entry.
  protected PlaylistEntry(Playlist playlist, Media media) {
    this.playlist = playlist;
    this.media = media;
  }

  protected void copyTo(PlaylistEntry target) {
    target._status = this._status;
  }

  public void markFixedIfFound() {
    if (getStatus() == PlaylistEntryStatus.Found) {
      _isFixed = true;
    }
  }

  public Media getMedia() {
    return this.media;
  }

  public abstract String getTrackFolder();

  public abstract String getTrackFileName();

  // check the file system for existence if we don't already know the file exists.
  protected abstract boolean exists();

  public abstract void recheckFoundStatus();

  public boolean isFound() {
    return getStatus() == PlaylistEntryStatus.Found;
  }

  public boolean isFixed() {
    return _isFixed;
  }

  public void setFixed(boolean fixed) {
    _isFixed = fixed;
  }

  public abstract boolean isURL();

  public abstract boolean isRelative();

  private final MatchedPlaylistEntryComparator matchedPlaylistEntryComparator =
      new MatchedPlaylistEntryComparator();

  public List<PotentialPlaylistEntryMatch> findClosestMatches(
      Collection<String> mediaFiles,
      IProgressObserver<String> observer,
      IPlaylistOptions playListOptions) {
    List<PotentialPlaylistEntryMatch> matches = new ArrayList<>();
    ProgressAdapter<String> progress = new ProgressAdapter<>(observer);
    progress.setTotal(mediaFiles.size());

    // For internal matching, normalize the track file name.
    String normalizedTrackName = UnicodeUtils.normalizeNfc(getTrackFileName());
    // Remove apostrophes and insert spaces between camel case letters for
    // tokenization.
    String entryName =
        CAMEL_CASE_PATTERN
            .matcher(APOS_PATTERN.matcher(normalizedTrackName).replaceAll(""))
            .replaceAll("$2 $3")
            .toLowerCase();
    File mediaFile;
    int score;
    for (String mediaFilePath : mediaFiles) {
      if (observer == null || !observer.getCancelled()) {
        progress.stepCompleted();

        mediaFile = new File(mediaFilePath);

        // Normalize the media file name for token matching.
        String mediaFileName = UnicodeUtils.normalizeNfc(mediaFile.getName());
        score =
            new FileNameTokenizer(playListOptions)
                .score(
                    entryName,
                    CAMEL_CASE_PATTERN
                        .matcher(APOS_PATTERN.matcher(mediaFileName).replaceAll(""))
                        .replaceAll("$2 $3")
                        .toLowerCase());
        if (score > 0) {
          // Only keep the top X highest-rated matches (default is 20)
          if (matches.size() < playListOptions.getMaxClosestResults()) {
            matches.add(new PotentialPlaylistEntryMatch(mediaFile.toPath(), score));
          } else {
            if (matches.get(playListOptions.getMaxClosestResults() - 1).getScore() < score) {
              matches.set(
                  playListOptions.getMaxClosestResults() - 1,
                  new PotentialPlaylistEntryMatch(mediaFile.toPath(), score));
            }
          }
          matches.sort(matchedPlaylistEntryComparator);
        }
      } else {
        return null;
      }
    }
    return matches;
  }

  @Override
  public abstract Object clone();

  public static PlaylistEntry makePlaylistEntry(Playlist playlist, Media media) {
    Content content = media.getSource();
    try {
      URI uri = content.getURI();
      if (!Objects.equals(uri.getScheme(), "file")) {
        return new UriPlaylistEntry(playlist, media);
      }
    } catch (URISyntaxException e) {
      // ignore
    }
    return new FilePlaylistEntry(playlist, media);
  }
}
