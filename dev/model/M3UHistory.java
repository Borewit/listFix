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

package listfix.model;

/**
 *
 * @author  jcaron
 */
import java.util.Vector;
import java.io.File;

public class M3UHistory
{
	private Vector<String> playlists = new Vector<String>();
	private int limit = 0;

	/** Creates a new instance of M3UHistory */
	public M3UHistory(int x)
	{
		limit = x;
	}

	public void setCapacity(int maxPlaylistHistoryEntries)
	{
		limit = maxPlaylistHistoryEntries;
		if (limit < playlists.size())
		{
			playlists.setSize(limit);
		}
	}

	protected int getLimit() // added to assist testing
	{
		return limit;
	}

	protected Vector<String> getPlaylists() // added to assist testing
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
				playlists.insertElementAt(temp, 0);
			}
			else
			{
				if (playlists.size() < limit)
				{
					playlists.insertElementAt(filename, 0);
				}
				else
				{
					playlists.removeElementAt(limit - 1);
					playlists.insertElementAt(filename, 0);
				}
			}
		}
	}

	public String[] getM3UFilenames()
	{
		String[] result = new String[playlists.size()];
		for (int i = 0; i < playlists.size(); i++)
		{
			result[i] = (String) playlists.elementAt(i);
		}
		return result;
	}

	public void clearHistory()
	{
		playlists.clear();
	}
}
