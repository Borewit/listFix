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

public class RepairedPlaylistResult
{
	private Playlist playlist = null;
	private int startLostCount = 0;
	private int endLostCount = 0;
	private boolean writtenSuccessfully = false;

	public RepairedPlaylistResult(Playlist list, int originalLostCount, int newLostCount, boolean successfullyWritten)
	{
		playlist = list;
		startLostCount = originalLostCount;
		endLostCount = newLostCount;
		writtenSuccessfully = successfullyWritten;
	}

	/**
	 * @return the playlist
	 */
	public Playlist getPlaylist()
	{
		return playlist;
	}

	/**
	 * @return the startLostCount
	 */
	public int getStartLostCount()
	{
		return startLostCount;
	}

	/**
	 * @return the endLostCount
	 */
	public int getEndLostCount()
	{
		return endLostCount;
	}

	/**
	 * @return the writtenSuccessfully
	 */
	public boolean isWrittenSuccessfully()
	{
		return writtenSuccessfully;
	}

	public int getStartFoundCount()
	{
		return playlist.getEntryCount() - startLostCount;
	}

	public int getEndFoundCount()
	{
		return playlist.getEntryCount() - endLostCount;
	}
}
