/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2009 Jeremy Caron
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

import java.util.*;

public class FileNameTokenizer
{
	public static final Vector<String> ignoreList = new Vector<String>();

	static
	{
		ignoreList.add("an");
		ignoreList.add("of");
		ignoreList.add("the");
		ignoreList.add("in");
		ignoreList.add("dsp");
		ignoreList.add("my");
		ignoreList.add("and");
		ignoreList.add("to");
	}

	public static int score(String filename1, String filename2)
	{
		return scoreMatchingTokens(splitFileName(filename1), splitFileName(filename2));
	}

	private static String removeExtensionFromFileName(String name)
	{
		String result = name;
		int index = name.lastIndexOf(".");
		if (index >= 0)
		{
			result = name.substring(0, index);
		}
		return result;
	}

	private static Vector<String> splitFileName(String fileName)
	{
		fileName = removeExtensionFromFileName(fileName);
		StringTokenizer tokenMaker = new StringTokenizer(fileName, " .-_[]{},/\\`'~!@#$%^\"&*()+=|:;");
		int tokenCount = tokenMaker.countTokens();
		Vector<String> result = new Vector<String>();
		for (int i = 0; i < tokenCount; i++)
		{
			String token = tokenMaker.nextToken();
			if (token.length() > 1 && !ignoreList.contains(token.toLowerCase()))
			{
				result.add(token);
			}
		}
		return result;
	}

	private static int scoreMatchingTokens(Vector<String> array1, Vector<String> array2)
	{
		HashMap<String, Integer> array1Counts = new HashMap<String, Integer>();
		HashMap<String, Integer> array2Counts = new HashMap<String, Integer>();

		for (String t : array1)
		{
			String token = t.toLowerCase();
			if (array1Counts.containsKey(token))
			{
				array1Counts.put(token, new Integer(array1Counts.get(token).intValue() + 1));
			}
			else
			{
				array1Counts.put(token, new Integer(1));
			}
		}

		for (String t : array2)
		{
			String token = t.toLowerCase();
			if (array1Counts.containsKey(token))
			{
				if (array2Counts.containsKey(token))
				{
					array2Counts.put(token, new Integer(array2Counts.get(token).intValue() + 1));
				}
				else
				{
					array2Counts.put(token, new Integer(1));
				}
			}
		}

		int result = 0;
		if (array2Counts.keySet().size() > 0)
		{
			result = array2Counts.keySet().size() == 1 ? 1 : (int) Math.pow(3.0, (double) array2Counts.keySet().size());
			Iterator<String> array2CountsIterator = array2Counts.keySet().iterator();
			while (array2CountsIterator.hasNext())
			{
				String token = array2CountsIterator.next();
				int array1Count = array1Counts.get(token).intValue();
				int array2Count = array2Counts.get(token).intValue();
				if (array1Count < array2Count)
				{
					result = result + ((token.length() - 1) * array1Count);
				}
				else
				{
					result = result + ((token.length() - 1) * array2Count);
				}
			}
		}

		return result;
	}
}
