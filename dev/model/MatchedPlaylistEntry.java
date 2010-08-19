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

import java.io.*;

public class MatchedPlaylistEntry
{
	private PlaylistEntry thisEntry = null;
	private int score = 0;

	public MatchedPlaylistEntry(String path, String fileName, int c)
	{
		thisEntry = new PlaylistEntry(path, fileName, "");
		score = c;
	}

	public MatchedPlaylistEntry(File f, int c)
	{
		thisEntry = new PlaylistEntry(f, "");
		score = c;
	}

	public int getScore()
	{
		return score;
	}

	public PlaylistEntry getPlaylistFile()
	{
		return thisEntry;
	}
}