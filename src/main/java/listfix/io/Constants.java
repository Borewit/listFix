/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2014 Jeremy Caron
 *
 *  This file is part of listFix().
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.io;

import java.io.File;

/**
 * Contains app-wide constants that are used throughout the listFix() codebase.
 * Some are listFix() specific notions while others are just shortcuts for System properties.
 *
 * @author jcaron
 */
public class Constants
{
  /**
   * The file system's separator on the current OS.
   */
  public static final String FS = System.getProperty("file.separator");

  /**
   * The user's home directory on the current OS's file system.
   */
  public static final String HOME_DIR = System.getProperty("user.home");

  /**
   * The directory on disk where the listFix() config files are stored.
   */
  public static final String DATA_DIR = HOME_DIR + FS + ".listFix()" + FS;

  /**
   * The line break string on the current OS.
   */
  public static final String BR = System.getProperty("line.separator");

  /**
   * Contains characters that are invalid for filenames under Windows.
   */
  public static final String INVALID_WINDOWS_FILENAME_CHARACTERS = "*|\\/:\"<>?";

  /**
   * Boolean specifying if the current OS's file system is case sensitive.
   */
  public static final boolean FILE_SYSTEM_IS_CASE_SENSITIVE = File.separatorChar == '/';
}
