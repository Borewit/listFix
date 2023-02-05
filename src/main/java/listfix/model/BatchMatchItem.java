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

import listfix.model.playlists.PlaylistEntry;
import listfix.model.playlists.PotentialPlaylistEntryMatch;

import java.util.List;

/**
 * Serves to model the multiple matches that result for a single playlist entry during a closest matches search.  Should probably be renamed to make this more explicit.
 * @author jcaron
 */

public class BatchMatchItem
{
  private final int _entryIx;
  private final PlaylistEntry _entry;
    private final List<PotentialPlaylistEntryMatch> _matches;

    private int _selectedIx;

    /**
   *
   * @param ix
   * @param entry
   * @param matches
   */
  public BatchMatchItem(int ix, PlaylistEntry entry, List<PotentialPlaylistEntryMatch> matches)
    {
        _entryIx = ix;
        _entry = entry;
        _matches = matches;
    }

    /**
   *
   * @return
   */
  public int getEntryIx()
    {
        return _entryIx;
    }

    /**
   *
   * @return
   */
  public PlaylistEntry getEntry()
    {
        return _entry;
    }

    /**
   *
   * @return
   */
  public List<PotentialPlaylistEntryMatch> getMatches()
    {
        return _matches;
    }

    /**
   *
   * @return
   */
  public int getSelectedIx()
    {
        return _selectedIx;
    }

    /**
   *
   * @param ix
   */
  public void setSelectedIx(int ix)
    {
        _selectedIx = ix;
    }

    /**
   *
   * @return
   */
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
