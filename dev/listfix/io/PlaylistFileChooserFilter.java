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

/*
============================================================================
= Author:   Jeremy Caron
= File:     M3UFileChooserFilter.java
= Purpose:  Simple instance of FilenameFilter that displays only
=           M3U files or directories.
============================================================================
 */

public class PlaylistFileChooserFilter extends javax.swing.filechooser.FileFilter
{
	public PlaylistFileChooserFilter()
	{

	}

	@Override
	public boolean accept(java.io.File file)
	{
		return (file.getName().toLowerCase().endsWith(".m3u")
			|| file.getName().toLowerCase().endsWith(".m3u8") 
			|| file.getName().toLowerCase().endsWith(".pls")
			|| file.isDirectory());
	}

	@Override
	public String getDescription()
	{
		return "Playlist Files (*.m3u, *.m3u8, *.pls)";
	}
	
	@Override
	public String toString()
	{
		return "Playlist Files (*.m3u, *.m3u8, *.pls)";
	}
}
