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
import java.util.List;
import listfix.model.PlaylistEntry;
import listfix.model.enums.PlaylistType;
import listfix.view.support.IProgressObserver;

/**
 *
 * @author jcaron
 */
public interface IPlaylistReader
{
	public String getEncoding();
	public void setEncoding(String encoding);

	public PlaylistType getPlaylistType();

	List<PlaylistEntry> readPlaylist(IProgressObserver input) throws IOException;
	List<PlaylistEntry> readPlaylist() throws IOException;
}
