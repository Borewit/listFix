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
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jcaron
 */
public class FileUtils
{
  private static final Set<String> mediaExtension = Stream.of("mp3", "wma", "flac", "ogg", "wav", "midi", "cda", "mpg", "mpeg", "m2v", "avi", "m4v", "flv", "mid", "mp2", "mp1", "aac", "asx", "m4a", "mp4", "m4v", "nsv", "aiff", "au", "wmv", "asf", "mpc")
    .collect(Collectors.toCollection(HashSet::new));

  public static File findDeepestPathToExist(File file)
  {
    if (file == null || file.exists())
    {
      return file;
    }
    return findDeepestPathToExist(file.getParentFile());
  }

  public static boolean isMediaFile(File file)
  {
    return isMediaFile(file.getName());
  }

  public static boolean isMediaFile(String filename)
  {
    String extension = getFileExtension(filename);
    if (extension != null)
    {
      return mediaExtension.contains(extension.toLowerCase());
    }
    return false;
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

  public static String getRelativePath(File file, File relativeTo)
  {
    try
    {
      return Path.of(UNCFile.from(relativeTo).getUNCPath()).relativize(file.toPath()).toString();
    }
    catch (IllegalArgumentException exception)
    {
      throw exception;
    }
  }

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
   * Extract the extension from the provided filename
   *
   * @param filename Filename
   * @return extension without leading dot, e.g.: "mp3"
   */
  public static String getFileExtension(String filename)
  {
    int index = filename.lastIndexOf(".");
    return index == -1 ? null : filename.substring(filename.lastIndexOf(".") + 1);
  }
}
