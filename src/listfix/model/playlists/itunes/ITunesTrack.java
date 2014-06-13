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

package listfix.model.playlists.itunes;

import christophedelory.plist.Dict;

import java.util.Hashtable;

/**
 * Wrapper around a christophedelory.plist.Dict to provide easy access to the information about a track
 * in an iTunes library/playlist file for the purposes of conversion to a listFix() PlaylistEntry object.
 * @author jcaron
 */
public class ITunesTrack
{
	private Dict _trackDict;
	
	/**
	 * Constructor that takes a christophedelory.plist.Dict object.
	 * @param trackDict The christophedelory.plist.Dict object containing information about the track.
	 */
	public ITunesTrack(Dict trackDict)
	{
		_trackDict = trackDict;
	}

	/**
	 * @return the _location
	 */
	public String getLocation()
	{
		return DictionaryParser.getKeyValueAsString(_trackDict, "Location");
	}
	
	public void setLocation(String location)
	{
		DictionaryParser.setKeyValue(_trackDict, "Location", new christophedelory.plist.String(location));
	}

	/**
	 * @return the _artist
	 */
	public String getArtist()
	{
		return DictionaryParser.getKeyValueAsString(_trackDict, "Artist");
	}

	/**
	 * @return the _name
	 */
	public String getName()
	{
		return DictionaryParser.getKeyValueAsString(_trackDict, "Name");
	}

	/**
	 * @return the _album
	 */
	public String getAlbum()
	{
		return DictionaryParser.getKeyValueAsString(_trackDict, "Album");
	}

	/**
	 * @return the _albumArtist
	 */
	public String getAlbumArtist()
	{
		return DictionaryParser.getKeyValueAsString(_trackDict, "Album Artist");
	}

	/**
	 * @return the _duration
	 */
	public long getDuration()
	{
		long result = -1;
		christophedelory.plist.Integer timeInt = ((christophedelory.plist.Integer)((Hashtable)getTrackDict().getDictionary()).get(new christophedelory.plist.Key("Total Time")));
		if (timeInt != null)
		{
			String timeText = timeInt.getValue();
			result = Long.parseLong(timeText);
		}
		return result;
	}

	/**
	 * @return the _trackId
	 */
	public String getTrackId()
	{
		return DictionaryParser.getKeyValueAsInteger(_trackDict, "Track ID").getValue();
	}

	/**
	 * @return the _trackDict
	 */
	public Dict getTrackDict()
	{
		return _trackDict;
	}
}
