/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2014 Jeremy Caron, 2012 John Peterson
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

package listfix.io.playlists.wpl;

import listfix.io.IPlaylistOptions;
import listfix.io.UnicodeInputStream;
import listfix.io.playlists.PlaylistReader;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.PlaylistEntry;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads in a WPL file and returns a List containing PlaylistEntries that represent the files & URIs in the playlist.
 * @author jcaron, jpeterson
 */
public class WPLReader extends PlaylistReader
{
  private final BufferedReader _buffer;
  private final List<PlaylistEntry> results = new ArrayList<>();
  private final long fileLength;
  private static final PlaylistType type = PlaylistType.WPL;
  private StringBuilder _cache;

  public WPLReader(IPlaylistOptions playListOptions, Path wplFile) throws FileNotFoundException
  {
    super(playListOptions, wplFile);
    final Charset defaultEncoding = StandardCharsets.UTF_8;
    _buffer = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(wplFile.toFile()), defaultEncoding), defaultEncoding));
    encoding = StandardCharsets.UTF_8;
    fileLength = wplFile.toFile().length();
  }

  private String XMLDecode(String s)
  {
    s = s.replaceAll("&apos;", "'");
    s = s.replaceAll("&lt;", "<");
    s = s.replaceAll("&gt;", ">");
    s = s.replaceAll("&amp;", "&");
    return s;
  }

  @Override
  public PlaylistType getPlaylistType()
  {
    return type;
  }

  @Override
  public List<PlaylistEntry> readPlaylist(IProgressObserver<String> observer) throws IOException
  {
    ProgressAdapter<String> progress = ProgressAdapter.wrap(observer);
    progress.setTotal((int) fileLength);

    _cache = new StringBuilder();
    String line = readLine();
    String path = "";
    String cid = "";
    String tid = "";

    int cacheSize = _cache.toString().getBytes().length;
    if (cacheSize < fileLength)
    {
      progress.setCompleted(cacheSize);
    }

    while (line != null)
    {
      if (observer != null)
      {
        if (observer.getCancelled())
        {
          return null;
        }
      }
      cacheSize = _cache.toString().getBytes().length;
      if (cacheSize < fileLength)
      {
        progress.setCompleted(cacheSize);
      }
      line = readLine();
      if (line == null)
      {
        break;
      }
      line = XMLDecode(line);

      if (line.trim().startsWith("<media"))
      {
        path = line.substring(line.indexOf("\"") + 1, line.indexOf("\"", line.indexOf("\"") + 1));
        if (line.contains("cid=\""))
        {
          cid = line.substring(line.indexOf("cid=\"") + 5, line.indexOf("\"", line.indexOf("cid=\"") + 5));
        }
        if (line.contains("tid=\""))
        {
          tid = line.substring(line.indexOf("tid=\"") + 5, line.indexOf("\"", line.indexOf("tid=\"") + 5));
        }
      }
      if (!path.isEmpty())
      {
        processEntry(path, cid, tid);
      }
      path = "";
      cid = "";
      tid = "";

      cacheSize = _cache.toString().getBytes().length;
      if (cacheSize < fileLength)
      {
        progress.setCompleted(cacheSize);
      }
    }
    _buffer.close();
    return results;
  }

  @Override
  public List<PlaylistEntry> readPlaylist() throws IOException
  {
    readPlaylist(null);
    return results;
  }

  private String readLine() throws IOException
  {
    String line = _buffer.readLine();
    if (_cache != null)
    {
      _cache.append(line);
    }
    return line;
  }

  private void processEntry(String L2, String cid, String tid)
  {
    super.processEntry(this.results, L2, cid, tid);
  }
}
