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
import java.util.ArrayList;
import java.util.List;

/*
============================================================================
= Author:   Jeremy Caron
= File:     PlaylistScanner.java
= Purpose:  To create a list of all of the playlists found in a given
=           directory and its subdirectories.
============================================================================
 */
public class PlaylistScanner
{
	public static List<File> getAllPlaylists(File directory)
	{
		List<File> result = new ArrayList<File>();
		if (directory.exists() && directory.isDirectory())
		{
			File[] inodes = directory.listFiles(new PlaylistFileFilter());
            if (inodes != null)
            {
                for (File inode : inodes)
                {
                    if (inode.isFile())
                    {
                        result.add(inode);
                    }
                    else
                    {
                        result.addAll(getAllPlaylists(inode));
                    }
                }
            }
		}
		return result;
	}
}
