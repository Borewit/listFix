/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2010 Jeremy Caron
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

/**
 *
 * @author  jcaron
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaylistHistory
{
	private List<String> playlists = new ArrayList<String>();
	private int limit = 0;

	/** Creates a new instance of PlaylistHistory */
	public PlaylistHistory(int x)
	{
		limit = x;
	}

	public void setCapacity(int maxPlaylistHistoryEntries)
	{
		limit = maxPlaylistHistoryEntries;
		if (limit < playlists.size())
		{
            ((ArrayList)playlists).subList(limit, playlists.size()).clear();
		}
	}

	protected int getLimit() // added to assist testing
	{
		return limit;
	}

	protected List<String> getPlaylists() // added to assist testing
	{
		return playlists;
	}

	public void initHistory(String[] input)
	{
		int i = 0;
		while (i < input.length && i < limit)
		{
			File testFile = new File(input[i]);
			if (testFile.exists())
			{
				playlists.add(input[i]);
			}
			i++;
		}
	}

	public void add(String filename)
	{
		File testFile = new File(filename);
		if (testFile.exists())
		{
			int index = playlists.indexOf(filename);
			if (index > -1)
			{
				String temp = playlists.remove(index);
				playlists.add(0, temp);
			}
			else
			{
				if (playlists.size() < limit)
				{
					playlists.add(0, filename);
				}
				else
				{
					playlists.remove(limit - 1);
					playlists.add(0, filename);
				}
			}
		}
	}

	public String[] getFilenames()
	{
		String[] result = new String[playlists.size()];
		for (int i = 0; i < playlists.size(); i++)
		{
			result[i] = (String) playlists.get(i);
		}
		return result;
	}

	public void clearHistory()
	{
		playlists.clear();
	}
}
