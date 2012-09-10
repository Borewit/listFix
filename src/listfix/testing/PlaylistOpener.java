/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2012 Jeremy Caron
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

import christophedelory.playlist.Playlist;
import christophedelory.playlist.SpecificPlaylist;
import christophedelory.playlist.SpecificPlaylistFactory;
import christophedelory.playlist.plist.PlistPlaylist;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import listfix.io.readers.playlists.itunes.ITunesMediaLibrary;

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
			SpecificPlaylist playlist = SpecificPlaylistFactory.getInstance().readFrom(new File("C:\\Users\\jcaron\\Music\\iTunes\\iTunes Music Library.xml"));
			PlistPlaylist plistList = (PlistPlaylist)playlist;
			ITunesMediaLibrary list = new ITunesMediaLibrary(plistList);
			list.getPlaylists();
		}
		catch (IOException ex)
		{
			Logger.getLogger(PlaylistOpener.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static Playlist convertToListFixPlaylist(PlistPlaylist xspfList)
	{
//		List<PlaylistEntry> newList = new ArrayList<>();
//		for (Track track : xspfList.getTracks())
//		{
//			try
//			{
//				newList.add(new PlaylistEntry(new URI(track.getStringContainers().get(0).getText()), track.getTitle(), track.getDuration()));
//			}
//			catch (URISyntaxException ex)
//			{
//				Logger.getLogger(PlaylistOpener.class.getName()).log(Level.SEVERE, null, ex);
//			}
//		}
//		try
//		{
//			return new Playlist(newList);
//		}
//		catch (Exception ex)
//		{
//			Logger.getLogger(PlaylistOpener.class.getName()).log(Level.SEVERE, null, ex);
//		}
		return null;
	}
}
