/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2010 Jeremy Caron
 * 
 *  This file is part of listFix().
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.io.readers.playlists.itunes;

import java.util.List;

/**
 *
 * @author jcaron
 */
public class ITunesPlaylist
{
	private String _name;
	private List<ITunesTrack> _tracks;
	
	public ITunesPlaylist(String name, List<ITunesTrack> tracks)
	{
		_name = name;
		_tracks = tracks;
	}

	/**
	 * @return the _name
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * @param name the _name to set
	 */
	public void setName(String name)
	{
		this._name = name;
	}

	/**
	 * @return the _tracks
	 */
	public List<ITunesTrack> getTracks()
	{
		return _tracks;
	}

	/**
	 * @param tracks the _tracks to set
	 */
	public void setTracks(List<ITunesTrack> tracks)
	{
		this._tracks = tracks;
	}
	
}
