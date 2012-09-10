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
	private final Dict _trackDict;
	
	/**
	 * 
	 * @param trackDict
	 */
	public ITunesTrack(Dict trackDict)
	{
		_trackDict = trackDict;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLocation()
	{
		return getStringValueForKey("Location");
	}
	
	/**
	 * 
	 * @return
	 */
	public String getArtist()
	{
		return getStringValueForKey("Artist");
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName()
	{
		return getStringValueForKey("Name");
	}
	
	/**
	 * 
	 * @return
	 */
	public String getAlbum()
	{
		return getStringValueForKey("Album");
	}
	
	/**
	 * 
	 * @return
	 */
	public String getAlbumArtist()
	{
		return getStringValueForKey("Album Artist");
	}
	
	/**
	 * 
	 * @return
	 */
	public long getDuration()
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
		return ((christophedelory.plist.String)((Hashtable)_trackDict.getDictionary()).get(new christophedelory.plist.Key(keyName))).getValue();
	}
}
