/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2009 Jeremy Caron
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

package listfix.comparators;

import listfix.model.MatchedPlaylistEntry;

public class MatchedPlaylistEntryColumnSorterComparator implements java.util.Comparator
{
	protected boolean isSortAsc;
	protected int sortCol;

	public MatchedPlaylistEntryColumnSorterComparator(boolean sortAsc, int sortByColumn)
	{
		sortCol = sortByColumn;
		isSortAsc = sortAsc;
	}

	@Override
	public int compare(Object o1, Object o2)
	{
		if (!(o1 instanceof MatchedPlaylistEntry) || !(o2 instanceof MatchedPlaylistEntry))
		{
			return 0;
		}

		if (sortCol == 0)
		{
			if (isSortAsc)
			{
				return ((MatchedPlaylistEntry) o1).getPlaylistFile().getFileName().compareTo(((MatchedPlaylistEntry) o2).getPlaylistFile().getFileName());
			}
			else
			{
				return ((MatchedPlaylistEntry) o2).getPlaylistFile().getFileName().compareTo(((MatchedPlaylistEntry) o1).getPlaylistFile().getFileName());
			}
		}
		else
		{
			int o1Count = ((MatchedPlaylistEntry) o1).getScore();
			int o2Count = ((MatchedPlaylistEntry) o2).getScore();
			if (o1Count == o2Count)
			{
				return 0;
			}
			else if (o1Count > o2Count)
			{
				return (isSortAsc ? 1 : -1);
			}
			else
			{
				return (isSortAsc ? -1 : 1);
			}
		}
	}
}
