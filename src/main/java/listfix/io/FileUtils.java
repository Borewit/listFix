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

package listfix.io;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import listfix.controller.GUIDriver;

/**
 *
 * @author jcaron
 */
public class FileUtils
{
  /**
   *
   * @param file
   * @return
   */
  public static File findDeepestPathToExist(File file)
  {
    if (file == null || file.exists())
    {
      return file;
    }
    return findDeepestPathToExist(file.getParentFile());
  }

  /**
   *
   * @param file
   * @return
   */
  public static boolean isMediaFile(File file)
  {
    String input = file.getName().toLowerCase();
    return (input.endsWith(".mp3") || input.endsWith(".wma")
      || input.endsWith(".flac") || input.endsWith(".ogg")
      || input.endsWith(".wav")  || input.endsWith(".midi")
      || input.endsWith(".cda")  || input.endsWith(".mpg")
      || input.endsWith(".mpeg") || input.endsWith(".m2v")
      || input.endsWith(".avi")  || input.endsWith(".m4v")
      || input.endsWith(".flv")  || input.endsWith(".mid")
      || input.endsWith(".mp2")  || input.endsWith(".mp1")
      || input.endsWith(".aac")  || input.endsWith(".asx")
      || input.endsWith(".m4a")  || input.endsWith(".mp4")
      || input.endsWith(".m4v")  || input.endsWith(".nsv")
      || input.endsWith(".aiff") || input.endsWith(".au")
      || input.endsWith(".wmv")  || input.endsWith(".asf")
      || input.endsWith(".mpc"));
  }

  public static boolean isURL(String trackText)
  {
    try
    {
      if (trackText.startsWith("file:"))
      {
        return false;
      }
      else
      {
        (new URL(trackText)).toURI();
        return true;
      }
    }
    catch (MalformedURLException | URISyntaxException e)
    {
      return false;
    }
  }

  /**
   *
   * @param file
   * @param relativeTo
   * @return
   */
  public static String getRelativePath(File file, File relativeTo)
  {
    try
    {
      UNCFile unc1 = new UNCFile(file);
      UNCFile unc2 = new UNCFile(relativeTo);
      StringTokenizer fileTizer;
      StringTokenizer relativeToTizer;
      if (unc1.onNetworkDrive())
      {
        fileTizer = new StringTokenizer(unc1.getUNCPath(), Constants.FS);
      }
      else
      {
        fileTizer = new StringTokenizer(file.getAbsolutePath(), Constants.FS);
      }
      if (unc2.onNetworkDrive())
      {
        relativeToTizer = new StringTokenizer(unc2.getUNCPath(), Constants.FS);
      }
      else
      {
        relativeToTizer = new StringTokenizer(relativeTo.getAbsolutePath(), Constants.FS);
      }
      List<String> fileTokens = new ArrayList<>();
      List<String> relativeToTokens = new ArrayList<>();
      while (fileTizer.hasMoreTokens())
      {
        fileTokens.add(fileTizer.nextToken());
      }
      while (relativeToTizer.hasMoreTokens())
      {
        relativeToTokens.add(relativeToTizer.nextToken());
      }

      // throw away last token from each, don't need the file names for path calculation.
      String fileName = "";
      if (file.isFile())
      {
        fileName = fileTokens.remove(fileTokens.size() - 1);
      }

      // relativeTo is the playlist we'll be writing to, we need to remove the last token regardless...
      relativeToTokens.remove(relativeToTokens.size() - 1);

      int maxSize = Math.min(fileTokens.size(), relativeToTokens.size());
      boolean tokenMatch = false;
      int i = 0;
      while (i < maxSize)
      {
        if (GUIDriver.FILE_SYSTEM_IS_CASE_SENSITIVE ? fileTokens.get(i).equals(relativeToTokens.get(i)) : fileTokens.get(i).equalsIgnoreCase(relativeToTokens.get(i)))
        {
          tokenMatch = true;
          fileTokens.remove(i);
          relativeToTokens.remove(i);
          i--;
          maxSize--;
        }
        else if (!tokenMatch)
        {
          // files can not be made relative to one another.
          return file.getAbsolutePath();
        }
        else
        {
          break;
        }
        i++;
      }

      StringBuilder resultBuffer = new StringBuilder();
      for (String relativeToToken : relativeToTokens)
      {
        resultBuffer.append("..").append(Constants.FS);
      }

      for (String fileToken : fileTokens)
      {
        resultBuffer.append(fileToken).append(Constants.FS);
      }

      resultBuffer.append(fileName);

      return resultBuffer.toString();
    }
    catch (Exception e)
    {
      // not logging anything here as this seems to be a common fallback...
      return file.getAbsolutePath();
    }
  }

  /**
   *
   * @param input
   * @param replacement
   * @return
   */
  public static String replaceInvalidWindowsFileSystemCharsWithChar(String input, char replacement)
  {
    StringBuilder result = new StringBuilder();
    for (char x : input.toCharArray())
    {
      if (Constants.INVALID_WINDOWS_FILENAME_CHARACTERS.indexOf(x) > -1)
      {
        result.append(replacement);
      }
      else
      {
        result.append(x);
      }
    }
    return result.toString();
  }

  /**
   *
   * @param dir
   */
  public static void deleteDirectory(File dir)
  {
    if (dir.isDirectory())
    {
      File[] files = dir.listFiles();
      for (File file : files)
      {
        if (file.isDirectory())
        {
          deleteDirectory(file);
          file.delete();
        }
        else
        {
          file.delete();
        }
      }
      dir.delete();
    }
    if (dir.exists())
    {
      deleteDirectory(dir);
    }
  }

  /**
   *
   * @param nodeFile
   * @return
   */
  public static String GetExtension(TreeNodeFile nodeFile)
  {
    return nodeFile.getName().substring(nodeFile.getName().lastIndexOf("."));
  }
}
