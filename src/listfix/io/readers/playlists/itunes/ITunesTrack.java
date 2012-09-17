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

import christophedelory.plist.Dict;
import java.util.Hashtable;

/**
 *
 * @author jcaron
 */
public class ITunesTrack
{
	private Dict _trackDict;
	private String _location;
	private String _artist;
	private String _name;
	private String _album;
	private String _albumArtist;
	private long _duration;
	
	/**
	 * 
	 * @param trackDict
	 */
	public ITunesTrack(Dict trackDict)
	{
		_trackDict = trackDict;
		_location = getStringValueForKey("Location");
		_artist = getStringValueForKey("Artist");
		_name = getStringValueForKey("Name");
		_album = getStringValueForKey("Album");
		_albumArtist = getStringValueForKey("Album Artist");
		_duration = parseDuration();
	}
	
	/**
	 * 
	 * @return
	 */
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
	
	private String getStringValueForKey(String keyName)
	{
		Object value = ((Hashtable)_trackDict.getDictionary()).get(new christophedelory.plist.Key(keyName));
		if (value != null)
		{
			return ((christophedelory.plist.String)value).getValue();
		}
		return null;
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
}
