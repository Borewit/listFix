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

package listfix.testing;

import christophedelory.playlist.SpecificPlaylist;
import christophedelory.playlist.SpecificPlaylistFactory;
import christophedelory.playlist.plist.PlistPlaylist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jcaron
 */
public class ITunesWriteTest
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
    }
    catch (IOException ex)
    {
      Logger.getLogger(PlaylistOpener.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
