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
  private final int _entryIx;
  private final PlaylistEntry _entry;
  private final List<PotentialPlaylistEntryMatch> _matches;

  private int _selectedIx;

  public BatchMatchItem(int ix, PlaylistEntry entry, List<PotentialPlaylistEntryMatch> matches)
  {
    _entryIx = ix;
    _entry = entry;
    _matches = matches;
  }

  public int getEntryIx()
  {
    return _entryIx;
  }

  public PlaylistEntry getEntry()
  {
    return _entry;
  }

  public List<PotentialPlaylistEntryMatch> getMatches()
  {
    return _matches;
  }

  public int getSelectedIx()
  {
    return _selectedIx;
  }

  public void setSelectedIx(int ix)
  {
    _selectedIx = ix;
  }

  public PotentialPlaylistEntryMatch getSelectedMatch()
  {
    if (_selectedIx >= 0)
    {
      return _matches.get(_selectedIx);
    }
    else
    {
      return null;
    }
  }
}
