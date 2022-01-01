/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2014 Jeremy Caron
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
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import listfix.controller.GUIDriver;

/**
 *
 * @author jcaron
 */
public class FileNameTokenizer
{
  private static final String separators = " .-_[]{},/\\`'~!@#$%^\"&*()+=|:;";
  public List<String> ignoreList = new ArrayList<>();

  public FileNameTokenizer()
  {
    String ignoredWords = GUIDriver.getInstance().getAppOptions().getIgnoredSmallWords();
    StringTokenizer tokenMaker = new StringTokenizer(ignoredWords, " ,;|");
    while (tokenMaker.hasMoreTokens())
    {
      ignoreList.add(tokenMaker.nextToken());
    }
  }

  /**
   *
   * @param filename1
   * @param filename2
   * @return
   */
  public int score(String filename1, String filename2)
  {
    return scoreMatchingTokens(splitFileName(filename1), splitFileName(filename2));
  }

  /**
   *
   * @param name
   * @return
   */
  public String removeExtensionFromFileName(String name)
  {
    String result = name;
    int index = name.lastIndexOf(".");
    if (index >= 0)
    {
      result = name.substring(0, index);
    }
    return result;
  }

    /**
   *
   * @param name
   * @return
   */
  public String getExtensionFromFileName(String name)
  {
      int ix = name.lastIndexOf('.');
      if (ix >= 0 && ix < name.length() - 1)
          return name.substring(ix + 1);
      else
          return "";
  }

  private List<String> splitFileName(String fileName)
  {
    fileName = removeExtensionFromFileName(fileName);
    StringTokenizer tokenMaker = new StringTokenizer(fileName, separators);
    int tokenCount = tokenMaker.countTokens();
    List<String> result = new ArrayList<>();
    String token = "";
    for (int i = 0; i < tokenCount; i++)
    {
      token = tokenMaker.nextToken();
      if (token.length() > 1 && !ignoreList.contains(token))
      {
        result.add(token);
      }
    }
    return result;
  }

  private int scoreMatchingTokens(List<String> array1, List<String> array2)
  {
    HashMap<String, Integer> array1Counts = new HashMap<>();
    HashMap<String, Integer> array2Counts = new HashMap<>();

    for (String t : array1)
    {
      if (array1Counts.containsKey(t))
      {
        array1Counts.put(t, array1Counts.get(t) + 1);
      }
      else
      {
        array1Counts.put(t, 1);
      }
    }

    for (String t : array2)
    {
      if (array1Counts.containsKey(t))
      {
        if (array2Counts.containsKey(t))
        {
          array2Counts.put(t, array2Counts.get(t) + 1);
        }
        else
        {
          array2Counts.put(t, 1);
        }
      }
    }

    int result = 0;
    String token;
    if (array2Counts.keySet().size() > 0)
    {
      result = array2Counts.keySet().size() == 1 ? 1 : (int) Math.pow(3.0, array2Counts.keySet().size());
      for (String s : array2Counts.keySet()) {
        token = s;
        int array1Count = array1Counts.get(token);
        int array2Count = array2Counts.get(token);
        if (array1Count < array2Count) {
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
