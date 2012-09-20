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
	
	private String _album;
	private String _albumArtist;
	private String _artist;
	private String _location;
	private String _name;
	private String _trackId;
	private long _duration;
	
	/**
	 * Constructor that takes a christophedelory.plist.Dict object.
	 * @param trackDict The christophedelory.plist.Dict object containing information about the track.
	 */
	public ITunesTrack(Dict trackDict)
	{
		_trackDict = trackDict;
		_location = DictionaryParser.getKeyValueAsString(_trackDict, "Location");
		_artist = DictionaryParser.getKeyValueAsString(_trackDict, "Artist");
		_name = DictionaryParser.getKeyValueAsString(_trackDict, "Name");
		_album = DictionaryParser.getKeyValueAsString(_trackDict, "Album");
		_albumArtist = DictionaryParser.getKeyValueAsString(_trackDict, "Album Artist");
		_trackId = DictionaryParser.getKeyValueAsString(_trackDict, "Track ID");
		_duration = parseDuration();
	}
	
	private long parseDuration()
	{
		long result = -1;
		christophedelory.plist.Integer timeInt = ((christophedelory.plist.Integer)((Hashtable)_trackDict.getDictionary()).get(new christophedelory.plist.Key("Total Time")));
		if (timeInt != null)
		{
			String timeText = timeInt.getValue();
			result = Long.parseLong(timeText);
		}
		return result;
	}

	/**
	 * @return the _location
	 */
	public String getLocation()
	{
		return _location;
	}

	/**
	 * @return the _artist
	 */
	public String getArtist()
	{
		return _artist;
	}

	/**
	 * @return the _name
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * @return the _album
	 */
	public String getAlbum()
	{
		return _album;
	}

	/**
	 * @return the _albumArtist
	 */
	public String getAlbumArtist()
	{
		return _albumArtist;
	}

	/**
	 * @return the _duration
	 */
	public long getDuration()
	{
		return _duration;
	}

	/**
	 * @return the _trackId
	 */
	public String getTrackId()
	{
		return _trackId;
	}
}
