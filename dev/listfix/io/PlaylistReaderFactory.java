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

package listfix.io;

import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @author jcaron
 */
public class PlaylistReaderFactory
{
	public static IPlaylistReader getPlaylistReader(File output) throws FileNotFoundException
	{
		if (output.getName().toLowerCase().contains(".m3u"))
		{
			return new M3UReader(output);
		}
		else
		{
			return new PLSReader(output);
		}
	}
}
