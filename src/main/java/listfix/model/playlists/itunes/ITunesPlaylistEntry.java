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

package listfix.model.playlists.itunes;

import java.io.File;
import java.net.URI;

import listfix.model.playlists.PlaylistEntry;

import org.apache.log4j.Logger;

/**
 *
 * @author jcaron
 */
public class ITunesPlaylistEntry extends PlaylistEntry
{
	private final ITunesTrack _track;
	
	// logger
	private static final Logger _logger = Logger.getLogger(PlaylistEntry.class);
	
	public ITunesPlaylistEntry(File input, String title, long length, File list, ITunesTrack track)
	{
		super(input, title, length, list);
		_track = track;
	}
	
	public ITunesPlaylistEntry(URI input, ITunesTrack track)
	{
		super(input, "");
		_track = track;
	}

	private ITunesPlaylistEntry(ITunesPlaylistEntry toClone)
	{
		super(toClone);
		_track = toClone.getTrack();
	}

	/**
	 * @return the _track
	 */
	public ITunesTrack getTrack()
	{
		return _track;
	}
	
	/**
	 *
	 * @return
	 */
	@Override
	public Object clone()
	{
		return new ITunesPlaylistEntry(this);
	}
}
