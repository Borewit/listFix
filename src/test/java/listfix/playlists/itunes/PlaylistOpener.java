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

package listfix.playlists.itunes;

import christophedelory.playlist.SpecificPlaylist;
import christophedelory.playlist.SpecificPlaylistFactory;
import christophedelory.playlist.plist.PlistPlaylist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import listfix.io.IPlayListOptions;
import listfix.model.playlists.itunes.ITunesMediaLibrary;
import listfix.model.playlists.itunes.ITunesTrack;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;
import listfix.model.playlists.itunes.ITunesPlaylist;
import listfix.model.playlists.itunes.ITunesPlaylistEntry;

/**
 *
 * @author jcaron
 */
public class PlaylistOpener
{
  public static void main(String[] args)
  {
    try
    {
      File toOpen = new File("C:\\Users\\jcaron\\Desktop\\svnListfix\\testing\\iTunes Music Library.xml");
      // File toOpen = new File("C:\\Users\\jcaron\\Desktop\\svnListfix\\testing\\iTunesTest.xml");
      SpecificPlaylist playlist = SpecificPlaylistFactory.getInstance().readFrom(toOpen);
      PlistPlaylist plistList = (PlistPlaylist)playlist;
      try (FileOutputStream stream = new FileOutputStream(toOpen))
      {
        plistList.writeTo(stream, "UTF-8");
      }
      catch (Exception ex)
      {
        Logger.getLogger(PlaylistOpener.class.getName()).log(Level.SEVERE, null, ex);
      }
      ITunesMediaLibrary list = new ITunesMediaLibrary(plistList);
      Playlist myList = convertToListFixPlaylist(list, toOpen, new IPlayListOptions()
      {
        @Override
        public boolean getAlwaysUseUNCPaths()
        {
          return false;
        }

        @Override
        public boolean getSavePlaylistsWithRelativePaths()
        {
          return false;
        }

        @Override
        public String getIgnoredSmallWords()
        {
          return "";
        }

        @Override
        public int getMaxClosestResults()
        {
          return 0;
        }

        @Override
        public boolean getCaseInsensitiveExactMatching()
        {
          return true;
        }
      });
      myList.getEntries();
    }
    catch (IOException ex)
    {
      Logger.getLogger(PlaylistOpener.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static Playlist convertToListFixPlaylist(ITunesMediaLibrary list, File listFile, IPlayListOptions playListOptions)
  {
    List<PlaylistEntry> newList = new ArrayList<>();
    Map<String, ITunesTrack> tracks = list.getTracks();

    for (String id : tracks.keySet())
    {
      ITunesTrack track = tracks.get(id);
      try
      {
        newList.add(new ITunesPlaylistEntry(playListOptions, new File((new URI(track.getLocation())).getPath()), track.getArtist() + " - " + track.getName(), track.getDuration(), listFile, track));
      }
      catch (URISyntaxException ex)
      {
        Logger.getLogger(PlaylistOpener.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    try
    {
      return new ITunesPlaylist(listFile, newList, list, playListOptions);
    }
    catch (Exception ex)
    {
      Logger.getLogger(PlaylistOpener.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }
}
