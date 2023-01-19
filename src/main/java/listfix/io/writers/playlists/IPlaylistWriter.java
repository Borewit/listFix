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

package listfix.io.writers.playlists;

import listfix.model.playlists.Playlist;
import listfix.view.support.ProgressAdapter;

/**
 * Generic contract for a class that can save a playlist to disk.
 * @author jcaron
 */
public interface IPlaylistWriter
{
  /**
   * The primary method a playlist writer needs, save.
   * @param list The list to persist to disk.
   * @param saveRelative Specifies if the playlist should be written out relatively or not.
   * @param adapter An optionally null progress adapter which lets other code monitor the progress of this operation.
   * @throws Exception
   */
  void save(Playlist list, boolean saveRelative, ProgressAdapter<String> adapter) throws Exception;
}
