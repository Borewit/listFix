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

package listfix.model.playlists;

import listfix.io.IPlaylistOptions;
import listfix.io.readers.playlists.IPlaylistReader;
import listfix.io.readers.playlists.ITunesXMLReader;
import listfix.io.readers.playlists.PlaylistReaderFactory;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.itunes.ITunesPlaylist;
import listfix.view.support.IProgressObserver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author jcaron
 */
public class PlaylistFactory
{
  public static Playlist getPlaylist(File playlistFile, IProgressObserver observer, IPlaylistOptions filePathOptions) throws IOException
  {
    return getPlaylist(playlistFile.toPath(), observer, filePathOptions);
  }

  public static Playlist getPlaylist(Path playlistPath, IProgressObserver observer, IPlaylistOptions filePathOptions) throws IOException
  {
    IPlaylistReader playlistProcessor = PlaylistReaderFactory.getPlaylistReader(playlistPath, filePathOptions);
    List<PlaylistEntry> entries = playlistProcessor.readPlaylist(observer);

    if (playlistProcessor.getPlaylistType() == PlaylistType.ITUNES)
    {
      return new ITunesPlaylist(playlistPath.toFile(), entries, ((ITunesXMLReader)playlistProcessor).getLibrary(), filePathOptions);
    }
    else
    {
      return new Playlist(filePathOptions, playlistPath.toFile(), playlistProcessor.getPlaylistType(), entries);
    }
  }
}
