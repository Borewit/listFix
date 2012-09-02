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

package listfix.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import listfix.comparators.FileComparator;

/**
 *
 * @author jcaron
 */
public class FileTypeSearch
{
	public List<File> findFiles(File directoryToSearch, FileFilter filter)
	{
		if (directoryToSearch.exists())
		{
			String curPath = directoryToSearch.getPath();
			File file = new File(curPath);

			List<File> ol = new ArrayList<File>();
			File[] inodes = directoryToSearch.listFiles(filter);

			if (inodes != null && inodes.length > 0)
			{
                ol.addAll(Arrays.asList(inodes));
				Collections.sort(ol, new FileComparator());
				File f;
				List<File> files = new ArrayList<File>();
				for (int i = 0; i < ol.size(); i++)
				{
					f = ol.get(i);
					if (f.isDirectory())
					{
						files.addAll(findFiles(f, filter));
					}
					else
					{
						files.add(f);
					}
				}

				return files;
			}
			return null;
		}
		return null;
	}
}
