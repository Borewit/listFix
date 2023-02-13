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

package listfix.comparators;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sorts directories ahead of files, and then sorts files alphabetically (ignoring case).
 */
public class DirectoryThenFileThenAlphabeticalPathComparator implements java.util.Comparator<Path>
{

  @Override
  public int compare(Path a, Path b)
  {
    if (!Files.isDirectory(a) && Files.isDirectory(b))
    {
      return 1;
    }
    else if (Files.isDirectory(a) && !Files.isDirectory(b))
    {
      return -1;
    }
    else
    {
      // both Files are files or both are directories
      return a.compareTo(b);
    }
  }
}
