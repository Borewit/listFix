/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2010 Jeremy Caron
 *
 * This file is part of listFix().
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.util;

import java.util.ArrayList;
import java.util.List;

/*
============================================================================
= Author:   Jeremy Caron
= File:     StringExtensions.java
= Purpose:  It is what it is...
============================================================================
 */

public class StringExtensions
{
	public static List<String> splitCamelCase(String inputString)
	{
		List<String> result = new ArrayList<String>();
		char[] chars = inputString.toCharArray();
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < chars.length; i++)
		{
			if (i == 0)
			{
				buffer.append(chars[i]);
			}
			else
			{
				if (Character.isUpperCase(chars[i]))
				{
					// start a new token in the buffer, saving the current buffer off to the result list
					result.add(buffer.toString());
					buffer.setLength(0);
					buffer.append(chars[i]);
				}
				else
				{
					buffer.append(chars[i]);
				}
			}
		}

		// buffer contains the last token, need to keep it.
		result.add(buffer.toString());
		buffer.setLength(0);
		return result;
	}
}
