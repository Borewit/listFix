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
import java.util.Set;

/*
============================================================================
= Author:   Jeremy Caron
= File:     PlaylistFileChooserFilter.java
= Purpose:  Simple instance of FilenameFilter that displays only
=           Playlist files or directories.
============================================================================
 */

// allows selection of playlists (m3u, m3u8) and directories (M3UFileFilter does not allow directories)
public class PlaylistFileChooserFilter extends FileExtensionFilterBase
{
    public PlaylistFileChooserFilter()
    {
        super(_extensions);
    }

    @Override
    public String getDescription()
    {
        return "Playlists (*.m3u, *.m3u8, *.pls)";
    }

    private static final Set<String> _extensions;

    static
    {
        _extensions = createExtensionSet("m3u", "m3u8", "pls");
    }
}
