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

package listfix.io.readers.playlists.itunes;

import christophedelory.playlist.plist.PlistPlaylist;
import christophedelory.plist.Array;
import christophedelory.plist.Dict;
import christophedelory.plist.Integer;
import christophedelory.plist.PlistObject;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jcaron
 */
public class ITunesMediaLibrary 
{
	private final PlistPlaylist _plist;
	
	/**
	 * 
	 * @param plist
	 */
	public ITunesMediaLibrary(PlistPlaylist plist)
	{
		_plist = plist;
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String, ITunesTrack> getTracks()
	{
		Map<String, ITunesTrack> result = new HashMap<>();
		Dict rootDict = ((Dict)_plist.getPlist().getPlistObject());
		Dict tracksDict = getDictValueForKey(rootDict, "Tracks");
		Dictionary tracksDictionary = tracksDict.getDictionary();
		Enumeration keys = tracksDictionary.keys();
		christophedelory.plist.Key key;
		while(keys.hasMoreElements())
		{
			key = (christophedelory.plist.Key) keys.nextElement();
			ITunesTrack track = new ITunesTrack((Dict)tracksDictionary.get(key));
			result.put(key.getValue(), track);
		}
		return result;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<ITunesPlaylist> getPlaylists()
	{
		List<ITunesPlaylist> result = new ArrayList<>();
		Dict rootDict = ((Dict)_plist.getPlist().getPlistObject());
		Array playlistsArray = getArrayValueForKey(rootDict, "Playlists");
		Map<String, ITunesTrack> tracks = getTracks();
		Dict playlistDict;
		Array playlistItems;
		Dict innerDict;
		Integer trackId;
		for (PlistObject object : playlistsArray.getPlistObjects())
		{			
			List<ITunesTrack> contents = new ArrayList<>();
			playlistDict = (Dict)object;
			playlistItems = getArrayValueForKey(playlistDict, "Playlist Items");
			if (playlistItems != null)
			{
				for (PlistObject innerObj : playlistItems.getPlistObjects())
				{
					// now each thing is a dict of "Track ID" to Integer.
					innerDict = (Dict)innerObj;
					trackId = getIntegerValueForKey(innerDict, "Track ID");
					contents.add(tracks.get(trackId.getValue()));
				}
			}
			result.add(new ITunesPlaylist(getStringValueForKey(playlistDict, "Name"), contents));
		}		
		return result;
	}
	
	private Dict getDictValueForKey(Dict dict, String key)
	{
		Object value = ((Hashtable)dict.getDictionary()).get(new christophedelory.plist.Key(key));
		if (value != null)
		{
			return (Dict)value;
		}
		return null;
	}
	
	private Array getArrayValueForKey(Dict dict, String key)
	{
		Object value = ((Hashtable)dict.getDictionary()).get(new christophedelory.plist.Key(key));
		if (value != null)
		{
			return (Array)value;
		}
		return null;
	}
	
	private christophedelory.plist.Integer getIntegerValueForKey(Dict dict, String key)
	{
		Object value = ((Hashtable)dict.getDictionary()).get(new christophedelory.plist.Key(key));
		if (value != null)
		{
			return (christophedelory.plist.Integer)value;
		}
		return null;
	}
	
	private String getStringValueForKey(Dict dict, String keyName)
	{
		Object value = ((Hashtable)dict.getDictionary()).get(new christophedelory.plist.Key(keyName));
		if (value != null)
		{
			return ((christophedelory.plist.String)value).getValue();
		}
		return null;
	}	
}
