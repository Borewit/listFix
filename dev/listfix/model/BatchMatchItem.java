/**
 * listFix() - Fix Broken Playlists!
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
