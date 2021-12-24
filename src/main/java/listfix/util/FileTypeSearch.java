/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2014 Jeremy Caron
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

import listfix.comparators.DirectoryThenFileThenAlphabeticalFileComparator;

/**
 *
 * @author jcaron
 */
public class FileTypeSearch
{
	/**
	 *
	 * @param directoryToSearch
	 * @param filter
	 * @return
	 */
	public List<File> findFiles(File directoryToSearch, FileFilter filter)
	{
		if (directoryToSearch.exists())
		{
			List<File> ol = new ArrayList<>();
			File[] inodes = directoryToSearch.listFiles(filter);

			if (inodes != null && inodes.length > 0)
			{
                ol.addAll(Arrays.asList(inodes));
				Collections.sort(ol, new DirectoryThenFileThenAlphabeticalFileComparator());
				File f;
				List<File> files = new ArrayList<>();
				for (File ol1 : ol)
				{
					f = ol1;
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
