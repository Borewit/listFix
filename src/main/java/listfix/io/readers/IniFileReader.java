/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2012 Jeremy Caron
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

package listfix.io.readers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import listfix.io.Constants;
import listfix.io.UNCFile;
import listfix.model.AppOptions;

/**
 * Read in the media library and history files, providing methods for retrieving the data.
 * @author jcaron
 */
public class IniFileReader
{
  private final String fname1;
  private final String fname2;
  private final AppOptions options;

  private String[] mediaDirs = new String[0];
  private String[] history = new String[0];
  private String[] mediaLibrary = new String[0];
  private String[] mediaLibraryFiles = new String[0];

  /**
   * Constructor.
   * @param opts An AppOptions configuration, needed to decide if the library should use UNC paths or not.
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  public IniFileReader(AppOptions opts) throws FileNotFoundException, UnsupportedEncodingException
  {
    options = opts;
    fname1 = Constants.MEDIA_LIBRARY_INI;
    fname2 = Constants.HISTORY_INI;

    File in_data1 = new File(fname1);
    if (!in_data1.exists())
    {
      throw new FileNotFoundException(in_data1.getPath());
    }
    else if (in_data1.length() == 0)
    {
      throw new FileNotFoundException("File found, but was of zero size.");
    }

    File in_data2 = new File(fname2);
    if (!in_data2.exists())
    {
      throw new FileNotFoundException(in_data2.getPath());
    }
    else if (in_data2.length() == 0)
    {
      throw new FileNotFoundException("File found, but was of zero size.");
    }
  }

  private static int readIniEntries(int lineNr, List<String> tempList, List<String> lines) {
    while (lineNr < lines.size())
    {
      String line = lines.get(lineNr++);
      if (line.startsWith("[")) {
        break;
      }
      tempList.add(line);
    }
    return lineNr;
  }

  public void readIni() throws IOException
  {
    List<String> tempList = new ArrayList<>();
    List<String> lines = Files.readAllLines(Paths.get(fname1), StandardCharsets.UTF_8);

    // Read in base media directories
    // skip first line, contains header
    int lineNr = readIniEntries(1, tempList, lines);
    mediaDirs = new String[tempList.size()];
    tempList.toArray(mediaDirs);

    tempList.clear();

    // Read in media library directories
    // skip first line, contains header
    lineNr = readIniEntries(lineNr, tempList, lines);
    mediaLibrary = new String[tempList.size()];
    tempList.toArray(mediaLibrary);
    tempList.clear();

    // Read in media library files
    // skip first line, contains header
    readIniEntries(lineNr, tempList, lines);

    mediaLibraryFiles = new String[tempList.size()];
    tempList.toArray(mediaLibraryFiles);
    tempList.clear();

    // Read in history...
    lines = Files.readAllLines(Paths.get(fname2), StandardCharsets.UTF_8);
    history = new String[lines.size()];
    lines.toArray(history);
  }

  /**
   * Get a list of paths to playlists that have been opened recently.
   * @return
   */
  public String[] getHistory()
  {
    return history;
  }

  /**
   * Get the root media directories the user has specified.
   * @return
   */
  public String[] getMediaDirs()
  {
    if (options.getAlwaysUseUNCPaths())
    {
      String[] result = new String[mediaDirs.length];
      for (int i = 0; i < result.length; i++)
      {
        UNCFile file = new UNCFile(mediaDirs[i]);
        if (file.onNetworkDrive())
        {
          result[i] = file.getUNCPath();
        }
        else
        {
          result[i] = mediaDirs[i];
        }
      }
      return result;
    }
    else
    {
      return mediaDirs;
    }
  }

  /**
   * Get the cached list of all directories in the media library.
   * @return
   */
  public String[] getMediaLibraryDirectories()
  {
    if (options.getAlwaysUseUNCPaths())
    {
      String[] result = new String[mediaLibrary.length];
      for (int i = 0; i < result.length; i++)
      {
        UNCFile file = new UNCFile(mediaLibrary[i]);
        if (file.onNetworkDrive())
        {
          result[i] = file.getUNCPath();
        }
        else
        {
          result[i] = mediaLibrary[i];
        }
      }
      return result;
    }
    else
    {
      return mediaLibrary;
    }
  }

  /**
   * Get the cached list of all files in the media library.
   * @return
   */
  public String[] getMediaLibraryFiles()
  {
    if (options.getAlwaysUseUNCPaths())
    {
      String[] result = new String[mediaLibraryFiles.length];
      for (int i = 0; i < result.length; i++)
      {
        UNCFile file = new UNCFile(mediaLibraryFiles[i]);
        if (file.onNetworkDrive())
        {
          result[i] = file.getUNCPath();
        }
        else
        {
          result[i] = mediaLibraryFiles[i];
        }
      }
      return result;
    }
    else
    {
      return mediaLibraryFiles;
    }
  }
}
