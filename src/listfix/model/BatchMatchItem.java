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

/**
 * Serves to model the multiple matches that result for a single playlist entry during a closest matches search.  Should probably be renamed to make this more explicit.
 * @author jcaron
 */

public class BatchMatchItem
{
	private int _entryIx;
    private int _selectedIx;
	private PlaylistEntry _entry;
    private List<MatchedPlaylistEntry> _matches;
	
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

    public PlaylistEntry getEntry()
    {
        return _entry;
    }    

    public List<MatchedPlaylistEntry> getMatches()
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

    public MatchedPlaylistEntry getSelectedMatch()
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
