/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2010 Jeremy Caron
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

/**
 *
 * @author jcaron
 */
public class Constants
{
	public static final String FS = System.getProperty("file.separator");
	public static final String HOME_DIR = System.getProperty("user.home");
	public static final String DATA_DIR = HOME_DIR + FS + ".listFix()" + FS;
	public static final String BR = System.getProperty("line.separator");
	public static final String MEDIA_LIBRARY_INI = DATA_DIR + "dirLists.ini";
	public static final String HISTORY_INI = DATA_DIR + "history.ini";
	public static final String OPTIONS_INI = DATA_DIR + "options.ini";
}
