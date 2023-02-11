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

package listfix.io.playlists.m3u;

import listfix.io.IPlaylistOptions;
import listfix.io.UnicodeInputStream;
import listfix.io.playlists.PlaylistReader;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.PlaylistEntry;
import listfix.util.UnicodeUtils;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads in a M3U/M3U8 file and returns a List containing PlaylistEntries that represent the files & URIs in the playlist.
 *
 * @author jcaron
 */
public class M3UReader extends PlaylistReader
{
  private final BufferedReader buffer;
  private final List<PlaylistEntry> results = new ArrayList<>();
  private final long fileLength;
  private static final PlaylistType type = PlaylistType.M3U;
  private static final Logger _logger = LogManager.getLogger(M3UReader.class);

  private StringBuilder _cache;

  public M3UReader(IPlaylistOptions playListOptions, Path m3uPath) throws FileNotFoundException
  {
    super(playListOptions, m3uPath);

    File m3uFile = m3uPath.toFile();

    encoding = UnicodeUtils.getEncoding(m3uFile);
    final Charset defaultEncoding = StandardCharsets.UTF_8;
    if (encoding.equals(defaultEncoding) || m3uFile.getName().toLowerCase().endsWith(".m3u8"))
    {
      buffer = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(m3uFile), defaultEncoding), defaultEncoding));
      encoding = StandardCharsets.UTF_8;
    }
    else
    {
      buffer = new BufferedReader(new FileReader(m3uFile));
    }
    fileLength = m3uFile.length();
  }

  @Override
  public Charset getEncoding()
  {
    return encoding;
  }

  @Override
  public PlaylistType getPlaylistType()
  {
    return type;
  }

  @Override
  public List<PlaylistEntry> readPlaylist(IProgressObserver<String> observer) throws IOException
  {
    // See http://gonze.com/playlists/playlist-format-survey.html#M3U for the format of an M3U file.
    // Line1 holds the metadata about the file that we just hang on to, line2 represents the file reference.

    //Initialize the progress adapter if we're given an observer.
    ProgressAdapter<String> progress = ProgressAdapter.wrap(observer);

    _cache = new StringBuilder();
    String line1 = readLine();
    String line2;
    if (line1 != null)
    {
      // Ignore the standard M3U header and random mediamonkey crap.
      while (line1.contains("#EXTM3U") || line1.startsWith("#EXTINFUTF8") || line1.isEmpty())
      {
        line1 = readLine();
        if (line1 == null)
        {
          // needed to handle empty playlists
          return results;
        }
      }

      // If after skipping that line the line doesn't start w/ a #, then we already have the file reference.  Stuff that into line2.
      if (!line1.startsWith("#"))
      {
        line2 = line1;
        line1 = "";
      }
      else
      {
        // Otherwise, read in the next line which should be our file reference.
        line2 = readLine();
        while (line2.startsWith("#"))
        {
          // throw away non-standard metadata added by mediamonkey...
          line2 = readLine();
        }
      }

      // Declare this variable outside the loop so we don't do it over and over.
      int cacheSize;

      while (line1 != null)
      {
        // If we have an observer and the user cancelled, bail out.
        if (observer != null)
        {
          if (observer.getCancelled())
          {
            return null;
          }
        }

        // Process the two strings we have into a playlist entry
        processEntry(line2, line1);

        // We just processed an entry, update the progress bar w/ the % of the file we've read if we have an observer.
        cacheSize = _cache.toString().getBytes().length;
        if (cacheSize < fileLength)
        {
          progress.setCompleted(cacheSize);
        }

        // Start processing the next entry.
        line1 = readLine();
        if (line1 != null)
        {
          // WMP produces M3Us with spaces between entries... have to read in an extra line to avoid this if line1 is empty.
          // Let's also handle an arbitrary number of spaces between the entries while we're at it.
          while (line1.isEmpty())
          {
            line1 = readLine();

            // And of course WMP ends the file w/ several blank lines, so if we find a null here return what we have...
            if (line1 == null)
            {
              // Fill the progress bar
              progress.setCompleted((int) fileLength);

              return results;
            }
          }

          if (!line1.startsWith("#"))
          {
            line2 = line1;
            line1 = "";
          }
          else
          {
            line2 = readLine();
            while (line2.startsWith("#"))
            {
              // throw away non-standard metadata added by mediamonkey...
              line2 = readLine();
            }
          }
        }
      }
    }

    // Close the reader
    buffer.close();

    // Fill the progress bar
    progress.setCompleted((int) fileLength);

    return results;
  }

  @Override
  public List<PlaylistEntry> readPlaylist() throws IOException
  {
    readPlaylist(null);
    return results;
  }

  // Custom readLine implementation that appends to the internal cache so we know how much of the file we've read.
  private String readLine() throws IOException
  {
    String line = buffer.readLine();
    if (_cache != null)
    {
      _cache.append(line);
    }
    return line;
  }

  private void processEntry(String L2, String extInf)
  {
    super.processEntry(this.results, L2, extInf, null);
  }
}
