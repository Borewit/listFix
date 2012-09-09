/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2012 Jeremy Caron
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

package listfix.io.writers.playlists;

import java.io.File;
import java.io.FileNotFoundException;
import listfix.model.Playlist;
import listfix.model.enums.PlaylistType;

/**
 *
 * @author jcaron
 */
public class PlaylistWriterFactory
{
	/**
	 *
	 * @param inputFile
	 * @return
	 * @throws FileNotFoundException
	 */
	public static IPlaylistWriter getPlaylistwriter(File inputFile) throws FileNotFoundException
	{
		PlaylistType type = Playlist.determinePlaylistType(inputFile);
		if (type == PlaylistType.M3U)
		{
			return new M3UWriter();
		}
		else if (type == PlaylistType.PLS)
		{
			return new PLSWriter();
		}
		else if (type == PlaylistType.XSPF)
		{
			return new XSPFWriter();
		}
		else if (type == PlaylistType.WPL)
		{
			return new WPLWriter();
		}
		else
		{
			return null;
		}
	}
}
