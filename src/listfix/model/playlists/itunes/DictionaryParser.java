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

import christophedelory.plist.Array;
import christophedelory.plist.Dict;

import java.util.Hashtable;

/**
 * The ghetto-tastic format used by iTunes (plist in lizzy-speak) has
 * an awkward in-memory structure; this class provides helper methods
 * for reading the data out of one of these objects based on the 
 * datatype stored under the given key.
 * @author jcaron
 */
public class DictionaryParser
{
	/**
	 * 
	 * @param dict
	 * @param key
	 * @return
	 * @throws ClassCastException Thrown when the datatype of the given key is not a christophedelory.plist.Dict.
	 */
	public static Dict getKeyValueAsDict(Dict dict, String key) throws ClassCastException
	{
		Object value = ((Hashtable) dict.getDictionary()).get(new christophedelory.plist.Key(key));
		if (value != null)
		{
			return (Dict) value;
		}
		return null;
	}
	
	public static void setKeyValue(Dict dict, String key, Object value) throws ClassCastException
	{
		((Hashtable) dict.getDictionary()).remove(new christophedelory.plist.Key(key));
		((Hashtable) dict.getDictionary()).put(new christophedelory.plist.Key(key), value);
	}

	/**
	 * 
	 * @param dict
	 * @param key
	 * @return
	 * @throws ClassCastException Thrown when the datatype of the given key is not a christophedelory.plist.Array.
	 */
	public static Array getKeyValueAsArray(Dict dict, String key) throws ClassCastException
	{
		Object value = ((Hashtable) dict.getDictionary()).get(new christophedelory.plist.Key(key));
		if (value != null)
		{
			return (Array) value;
		}
		return null;
	}

	/**
	 * 
	 * @param dict
	 * @param key
	 * @return
	 * @throws ClassCastException Thrown when the datatype of the given key is not a christophedelory.plist.Integer.
	 */
	public static christophedelory.plist.Integer getKeyValueAsInteger(Dict dict, String key) throws ClassCastException
	{
		Object value = ((Hashtable) dict.getDictionary()).get(new christophedelory.plist.Key(key));
		if (value != null)
		{
			return (christophedelory.plist.Integer) value;
		}
		return null;
	}

	/**
	 * 
	 * @param dict
	 * @param keyName
	 * @return
	 * @throws ClassCastException Thrown when the datatype of the given key is not a String.
	 */
	public static String getKeyValueAsString(Dict dict, String keyName) throws ClassCastException
	{
		Object value = ((Hashtable) dict.getDictionary()).get(new christophedelory.plist.Key(keyName));
		if (value != null)
		{
			return ((christophedelory.plist.String) value).getValue();
		}
		return null;
	}
}
