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

package listfix.io.filters;

import listfix.model.playlists.Playlist;

import java.io.FileFilter;

/**
 * A FileFilter that accepts our currently supported playlist types, and directories.
 * @author jcaron
 */
public class PlaylistFileFilter extends FileExtensionFilterBase implements FileFilter
{
  public PlaylistFileFilter()
  {
      super(Playlist.playlistExtensions);
  }

  @Override
  public String getDescription()
  {
      return "Playlists (*.m3u, *.m3u8, *.pls, *.wpl, *.xspf, *.xml)";
  }

  @Override
  public String toString()
  {
    return getDescription();
  }
}

