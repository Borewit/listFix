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
import listfix.io.playlists.itunes.ITunesXMLReader;
import listfix.io.playlists.m3u.M3UReader;
import listfix.io.playlists.pls.PLSReader;
import listfix.io.playlists.wpl.WPLReader;
import listfix.io.playlists.xspf.XSPFReader;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.Playlist;

import java.io.IOException;
import java.nio.file.Path;

public class PlaylistReaderFactory
{

  public static IPlaylistReader getPlaylistReader(Path inputFile, IPlaylistOptions playListOptions) throws IOException
  {
    PlaylistType type = Playlist.determinePlaylistTypeFromExtension(inputFile.toFile(), playListOptions);
    return switch (type)
      {
        case M3U -> new M3UReader(playListOptions, inputFile);
        case PLS -> new PLSReader(playListOptions, inputFile);
        case XSPF -> new XSPFReader(playListOptions, inputFile);
        case WPL -> new WPLReader(playListOptions, inputFile);
        case ITUNES -> new ITunesXMLReader(playListOptions, inputFile);
        default -> throw new IOException("Unsupported playlist type");
      };
  }
}
