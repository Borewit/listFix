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
= File:     ValidM3UFileRefFileChooserFilter.java
= Purpose:  Simple instance of FilenameFilter that displays only
=           audio files or directories.
============================================================================
 */
public class ValidM3UFileRefFileChooserFilter extends javax.swing.filechooser.FileFilter
{
	private boolean endsWithValidExtension(String fileName)
	{
		// disallow nested playlists
		return !fileName.toLowerCase().endsWith(".m3u");
	}

	public ValidM3UFileRefFileChooserFilter()
	{
	}

	@Override
	public boolean accept(java.io.File file)
	{
		return (endsWithValidExtension(file.getName().toLowerCase()) || file.isDirectory());
	}

	@Override
	public String getDescription()
	{
		return "All Files (except M3Us)";
	}
}
