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
import java.util.List;

import listfix.model.enums.PlaylistType;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;

/**
 *
 * @author jcaron
 */
public class ITunesPlaylist extends Playlist
{
	private final ITunesMediaLibrary _library;
	
	public ITunesPlaylist(File listFile, List<PlaylistEntry> entries, ITunesMediaLibrary library)
	{
		super(listFile, PlaylistType.ITUNES, entries);
		_library = library;
	}

	/**
	 * @return the _library
	 */
	public ITunesMediaLibrary getLibrary()
	{
		return _library;
	}
}
