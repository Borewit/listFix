package listfix.model;

import listfix.model.playlists.PlaylistEntry;
import listfix.model.playlists.PotentialPlaylistEntryMatch;

import java.util.List;

/**
 * Serves to model the multiple matches that result for a single playlist entry during a closest matches search.
 * Should probably be renamed to make this more explicit.
 */
public class BatchMatchItem
{
  /**
   * Playlist position
   */
  private final int _entryIx;
  private final PlaylistEntry _entry;
  private final List<PotentialPlaylistEntryMatch> matches;
  private PotentialPlaylistEntryMatch selectedMatch;

  public BatchMatchItem(int ix, PlaylistEntry entry, List<PotentialPlaylistEntryMatch> matches)
  {
    this._entryIx = ix;
    this._entry = entry;
    this.matches = matches;
    this.selectedMatch = matches.size() > 0 ? matches.get(0) : null;
  }

  public int getEntryIx()
  {
    return this._entryIx;
  }

  public PlaylistEntry getEntry()
  {
    return _entry;
  }

  public List<PotentialPlaylistEntryMatch> getMatches()
  {
    return this.matches;
  }

  public int getSelectedIx()
  {
    return this.matches.indexOf(this.selectedMatch);
  }

  @Deprecated // Use setSelectedMatch()
  public void setSelectedIx(int ix)
  {
    this.selectedMatch = this.matches.get(ix);
  }

  public void setSelectedMatch(PotentialPlaylistEntryMatch selectedMatch)
  {
    this.selectedMatch = selectedMatch;
  }

  public PotentialPlaylistEntryMatch getSelectedMatch()
  {
    return this.selectedMatch;
  }
}
