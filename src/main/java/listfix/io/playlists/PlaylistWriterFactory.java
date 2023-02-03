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

package listfix.io.playlists;

import listfix.io.IPlaylistOptions;
import listfix.io.playlists.itunes.ITunesXMLWriter;
import listfix.io.playlists.m3u.M3UWriter;
import listfix.io.playlists.pls.PLSWriter;
import listfix.io.playlists.wpl.WPLWriter;
import listfix.io.playlists.xspf.XSPFWriter;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.Playlist;

import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @author jcaron
 */
public class PlaylistWriterFactory
{
  /**
   *
   * @param inputFile
   * @return
   * @throws FileNotFoundException
   */
  public static IPlaylistWriter getPlaylistWriter(File inputFile, IPlaylistOptions playListOptions) throws FileNotFoundException
  {
    PlaylistType type = Playlist.determinePlaylistTypeFromExtension(inputFile, playListOptions);
    if (type == PlaylistType.M3U)
    {
      return new M3UWriter(playListOptions);
    }
    else if (type == PlaylistType.PLS)
    {
      return new PLSWriter(playListOptions);
    }
    else if (type == PlaylistType.XSPF)
    {
      return new XSPFWriter(playListOptions);
    }
    else if (type == PlaylistType.WPL)
    {
      return new WPLWriter(playListOptions);
    }
    else if (type == PlaylistType.ITUNES)
    {
      return new ITunesXMLWriter(playListOptions);
    }
    else
    {
      return null;
    }
  }
}
