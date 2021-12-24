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

package listfix.model.playlists.itunes;

import christophedelory.playlist.plist.PlistPlaylist;
import christophedelory.plist.Array;
import christophedelory.plist.Dict;
import christophedelory.plist.Integer;
import christophedelory.plist.Key;
import christophedelory.plist.PlistObject;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps a lizzy PlistPlaylist to provide easy access to the important/relevant sections of an iTunes XML (plist) file.
 * Unlike other playlist formats supported by listFix(), iTunes XML files can contain one or more playlists.  This means that when opening
 * an iTunes XML file, we may be handling a single exported playlist, or the entire media library for iTunes; there's really no way to know
 * since the structure is identical in both cases (some metadata followed by a tracks section, and then a playlists section).
 * 
 * It is possible for a track in the Tracks section of the library to be an orphaned, as in no playlist in the library references it.
 * Playlists themselves are just a name (string) and list of track IDs from the tracks section.
 * 
 * @author jcaron
 * @see ITunesTrackList
 * @see ITunesTrack
 */
public class ITunesMediaLibrary 
{
	private final PlistPlaylist _plist;
	
	/**
	 * Constructor that accepts a PlistPlaylist to be wrapped.
	 * @param plist The PlistPlaylist to be wrapped.
	 * @see PlistPlaylist
	 */
	public ITunesMediaLibrary(PlistPlaylist plist)
	{
		_plist = plist;
	}
	
	/**
	 * Gets the "Tracks" section of an iTunes library/playlist file as a map of Track IDs to ITunesTracks.
	 * @return The "Tracks" section of an iTunes library/playlist file as a map of Track IDs to ITunesTracks
	 * @see ITunesTrack
	 */
	public Map<String, ITunesTrack> getTracks()
	{
		Map<String, ITunesTrack> result = new HashMap<>();
		Dict rootDict = ((Dict)_plist.getPlist().getPlistObject());
		Dict tracksDict = DictionaryParser.getKeyValueAsDict(rootDict, "Tracks");
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
	
	public void setTracks(Map<String, ITunesTrack> trackMap)
	{
		Dict rootDict = ((Dict)_plist.getPlist().getPlistObject());
		Dict tracksDict = DictionaryParser.getKeyValueAsDict(rootDict, "Tracks");
		Dictionary tracksDictionary = tracksDict.getDictionary();
		Key plistKey;
		for (String key : trackMap.keySet())
		{
			plistKey = new Key(key);
			tracksDictionary.remove(plistKey);
			tracksDictionary.put(plistKey, trackMap.get(key).getTrackDict());
		}
	}
	
	/**
	 * Gets the Playlists section of an iTunes library/playlist file as a list of ITunesTrackList objects.
	 * @return The Playlists section of an iTunes library/playlist file as a list of ITunesTrackList objects.
	 * @see ITunesTrackList
	 */
	public List<ITunesTrackList> getPlaylists()
	{
		List<ITunesTrackList> result = new ArrayList<>();
		Dict rootDict = ((Dict)_plist.getPlist().getPlistObject());
		Array playlistsArray = DictionaryParser.getKeyValueAsArray(rootDict, "Playlists");
		Map<String, ITunesTrack> tracks = getTracks();
		Dict playlistDict;
		Array playlistItems;
		Dict innerDict;
		Integer trackId;
		for (PlistObject object : playlistsArray.getPlistObjects())
		{			
			List<ITunesTrack> contents = new ArrayList<>();
			playlistDict = (Dict)object;
			playlistItems = DictionaryParser.getKeyValueAsArray(playlistDict, "Playlist Items");
			if (playlistItems != null)
			{
				for (PlistObject innerObj : playlistItems.getPlistObjects())
				{
					// now each thing is a dict of "Track ID" to Integer.
					innerDict = (Dict)innerObj;
					trackId = DictionaryParser.getKeyValueAsInteger(innerDict, "Track ID");
					contents.add(tracks.get(trackId.getValue()));
				}
			}
			result.add(new ITunesTrackList(DictionaryParser.getKeyValueAsString(playlistDict, "Name"), contents));
		}		
		return result;
	}

	/**
	 * @return the _plist
	 */
	public PlistPlaylist getPlist()
	{
		return _plist;
	}
}
