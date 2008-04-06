/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2008 Jeremy Caron
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

import java.util.StringTokenizer;
import java.util.*;

public class FileNameTokenizer
{
    public static final Vector ignoreList = new Vector();
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
    
    public static String[] splitFileName(String fileName)
    {
        String[] result = new String[0];
        fileName = removeExtensionFromFileName(fileName);
        StringTokenizer tokenMaker = new StringTokenizer(fileName, " .-_[]{},/\\`'~!@#$%^\"&*()+=|:;");
        int tokenCount = tokenMaker.countTokens();
        Vector tempResult = new Vector();
        for (int i = 0; i < tokenCount; i++)
        {
            String token = tokenMaker.nextToken();
            if (token.length() > 1 && !ignoreList.contains(token.toLowerCase()))
            {
                tempResult.add(token);
            }
        }
        result = new String[tempResult.size()];
        tempResult.copyInto(result);
        return result;
    }
    
    public static int countMatchingTokens(String[] array1, String[] array2)
    {
        int result = 0; int array1Size = array1.length; int array2Size = array2.length;
        for (int i = 0; i < array1Size; i++)
        {
            for (int j = 0; j < array2Size; j++)
            {
                if (array1[i].equalsIgnoreCase(array2[j]))
                {
                    result++;
                }
            }
        }
        return result;
    }  
    
}