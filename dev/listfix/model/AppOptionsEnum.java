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
 * @author jcaron
 */
public class AppOptionsEnum
{
	public final static Integer SAVE_RELATIVE_REFERENCES = 0;
	public final static Integer AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD = 1;
	public final static Integer MAX_PLAYLIST_HISTORY_SIZE = 2;
	public final static Integer AUTO_REFRESH_MEDIA_LIBRARY_ON_LOAD = 3;
	public final static Integer LOOK_AND_FEEL = 4;
	public final static Integer ALWAYS_USE_UNC_PATHS = 5;
	public final static Integer PLAYLISTS_DIRECTORY = 6;
}
