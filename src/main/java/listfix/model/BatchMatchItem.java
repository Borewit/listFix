package listfix.model;

import java.util.List;
import listfix.model.playlists.PlaylistEntry;
import listfix.model.playlists.PotentialPlaylistEntryMatch;

/**
 * Serves to model the multiple matches that result for a single playlist entry during a closest
 * matches search. Should probably be renamed to make this more explicit.
 */
public class BatchMatchItem {
  /** Playlist position */
  private final PlaylistEntry _entry;

  private final List<PotentialPlaylistEntryMatch> matches;
  private PotentialPlaylistEntryMatch selectedMatch;

  public BatchMatchItem(PlaylistEntry entry, List<PotentialPlaylistEntryMatch> matches) {
    this._entry = entry;
    this.matches = matches;
    this.selectedMatch = matches.size() > 0 ? matches.get(0) : null;
  }

  public PlaylistEntry getEntry() {
    return _entry;
  }

  public List<PotentialPlaylistEntryMatch> getMatches() {
    return this.matches;
  }

  public int getSelectedIx() {
    return this.matches.indexOf(this.selectedMatch);
  }

  @Deprecated // Use setSelectedMatch()
  public void setSelectedIx(int ix) {
    this.selectedMatch = this.matches.get(ix);
  }

  public void setSelectedMatch(PotentialPlaylistEntryMatch selectedMatch) {
    this.selectedMatch = selectedMatch;
  }

  public PotentialPlaylistEntryMatch getSelectedMatch() {
    return this.selectedMatch;
  }
}
