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

/*
============================================================================
= Author:   Jeremy Caron
= File:     ArrayFunctions.java
= Purpose:  A compilation of commonly used array functions, including
=           several which mimic Vector functionality.
============================================================================
 */
import java.util.List;

public class ArrayFunctions
{
	public static float[] multByConstant(int[] array1, float constant)
	{
		float[] temp = new float[array1.length];
		for (int i = 0; i < array1.length; i++)
		{
			temp[i] = (float) array1[i] * constant;
		}
		return temp;
	}

	public static float[] multByConstant(float[] array1, int constant)
	{
		float[] temp = new float[array1.length];
		for (int i = 0; i < array1.length; i++)
		{
			temp[i] = array1[i] * (float) constant;
		}
		return temp;
	}

	public static float[] addTwoArrays(float[] array1, float[] array2)
	{
		if (array1.length == array2.length)
		{
			float[] temp = new float[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				temp[i] = array1[i] + array2[i];
			}
			return temp;
		}
		else
		{
			System.out.println("You are a monkey... add two arrays that have the same size!!!");
			return null;
		}
	}

	public static int[] addTwoArrays(int[] array1, int[] array2)
	{
		if (array1.length == array2.length)
		{
			int[] temp = new int[array1.length];
			for (int i = 0; i < array1.length; i++)
			{
				temp[i] = array1[i] + array2[i];
			}
			return temp;
		}
		else
		{
			System.out.println("You are a monkey... add two arrays that have the same size!!!");
			return null;
		}
	}

	public static int[] copyArrayAddOneValue(int[] array1, int value)
	{
		int[] copied = new int[array1.length + 1];
		for (int i = 0; i < array1.length; i++)
		{
			copied[i] = array1[i];
		}
		copied[copied.length - 1] = value;
		return copied;
	}

	public static String[] copyArrayAddOneValue(String[] array1, String value)
	{
		String[] copied = new String[array1.length + 1];
		for (int i = 0; i < array1.length; i++)
		{
			copied[i] = array1[i];
		}
		copied[copied.length - 1] = value;
		return copied;
	}

	public static String[] removeItem(String[] array1, int location)
	{
		String[] result = new String[array1.length - 1];
		for (int i = 0; i < location; i++)
		{
			result[i] = array1[i];
		}
		for (int i = location + 1; i < array1.length; i++)
		{
			result[i - 1] = array1[i];
		}
		return result;
	}

	public static String[] mergeArray(String[] a, String[] b)
	{
		int c = a.length + b.length;
		String[] cArray = new String[c];
		for (int i = 0; i < a.length; i++)
		{
			cArray[i] = a[i];
		}
		for (int i = a.length; i < c; i++)
		{
			cArray[i] = b[i - a.length];
		}
		return cArray;
	}

	public static boolean ContainsStringPrefixingAnotherString(String[] a, String b, boolean ignoreCase)
	{
		boolean result = false;
		if (a != null && a.length > 0)
		{
			for (int i = 0; i < a.length; i++)
			{
				if (ignoreCase ? b.toLowerCase().startsWith(a[i].toLowerCase()) : b.startsWith(a[i]))
				{
					result = true;
					break;
				}

			}
		}
		return result;
	}

	public static boolean ContainsStringPrefixingAnotherString(List<String> a, String b, boolean ignoreCase)
	{
		boolean result = false;
		if (a != null && a.size() > 0)
		{
			for (int i = 0; i < a.size(); i++)
			{
				if (ignoreCase ? b.toLowerCase().startsWith(a.get(i).toLowerCase()) : b.startsWith(a.get(i)))
				{
					result = true;
					break;
				}

			}
		}
		return result;
	}

	public static String[] convertVectorToStringArray(List<Object> v)
	{
		int size = v.size();
		String[] result = new String[size];
		for (int i = 0; i < size; i++)
		{
			result[i] = v.get(i).toString();
		}
		return result;
	}

	public static int[] integerListToArray(List<Integer> list)
	{
		int[] rows = new int[list.size()];
		for (int i = 0; i < list.size(); i++)
		{
			rows[i] = list.get(i);
		}
		return rows;
	}
}
