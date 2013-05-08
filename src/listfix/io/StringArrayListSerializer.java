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

package listfix.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 *
 * @author jcaron
 */
public class StringArrayListSerializer
{
	public static String Serialize(ArrayList<String> input) throws IOException
	{
		String result;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream os = new ObjectOutputStream(bos))
		{
			os.writeObject(input);
			result = new String(Base64Coder.encode(bos.toByteArray()));
		}
		return result;
	}
	
	public static ArrayList<String> Deserialize(String input) throws IOException, ClassNotFoundException
	{
		ArrayList<String> result = new ArrayList<>();
		try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64Coder.decode(input)); ObjectInputStream oInputStream = new ObjectInputStream(bis))
		{
			result = (ArrayList<String>) oInputStream.readObject();
		}
		return result;
	}	
}
