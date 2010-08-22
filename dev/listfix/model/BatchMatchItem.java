
package listfix.model;

import java.util.List;

public class BatchMatchItem
{
    public BatchMatchItem(int ix, PlaylistEntry entry, List<MatchedPlaylistEntry> matches)
    {
        _entryIx = ix;
        _entry = entry;
        _matches = matches;
    }

    public int getEntryIx()
    {
        return _entryIx;
    }
    private int _entryIx;

    public PlaylistEntry getEntry()
    {
        return _entry;
    }
    private PlaylistEntry _entry;

    public List<MatchedPlaylistEntry> getMatches()
    {
        return _matches;
    }
    private List<MatchedPlaylistEntry> _matches;

    public int getSelectedIx()
    {
        return _selectedIx;
    }
    public void setSelectedIx(int ix)
    {
        _selectedIx = ix;
    }
    private int _selectedIx;

    public MatchedPlaylistEntry getSelectedMatch()
    {
        if (_selectedIx >= 0)
            return _matches.get(_selectedIx);
        else
            return null;
    }

}
