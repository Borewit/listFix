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

package listfix.io;

import java.io.IOException;
import java.util.Vector;
import listfix.controller.Task;
import listfix.model.PlaylistEntry;
import listfix.model.PlaylistType;

/**
 *
 * @author jcaron
 */
public interface IPlaylistReader
{
	public static final PlaylistType ListType = PlaylistType.UNKNOWN;

	public String getEncoding();
	public void setEncoding(String encoding);

	Vector<PlaylistEntry> readPlaylist(Task input) throws IOException;
	Vector<PlaylistEntry> readPlaylist() throws IOException;
}
