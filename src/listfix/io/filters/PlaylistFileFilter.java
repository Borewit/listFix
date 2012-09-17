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

package listfix.io.filters;

import java.io.FileFilter;
import java.util.Set;

/*
============================================================================
= Author:   Jeremy Caron
= File:     PlaylistFileFilter.java
= Purpose:  A FileFilter that accepts M3Us, M3U8s, and directories.
============================================================================
 */
public class PlaylistFileFilter extends FileExtensionFilterBase implements FileFilter
{
    public PlaylistFileFilter()
    {
        super(_extensions);
    }

    @Override
    public String getDescription()
    {
        return "Playlists (*.m3u, *.m3u8, *.pls, *.wpl, *.xspf, *.xml)";
    }

    private static final Set<String> _extensions;

    static
    {
        _extensions = createExtensionSet("m3u", "m3u8", "pls", "wpl", "xspf", "xml");
    }

	@Override
	// Fixes display in linux
	public String toString()
	{
		return getDescription();
	}
}

