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

package listfix.io.writers.playlists;

import listfix.io.Constants;
import listfix.io.IPlaylistOptions;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;
import listfix.util.OperatingSystem;
import listfix.util.UnicodeUtils;

import java.io.*;

/**
 * A playlist writer capable of saving to M3U or M3U8 format.
 *
 * @author jcaron
 */
public class M3UWriter extends PlaylistWriter<StringBuilder>
{
  public M3UWriter(IPlaylistOptions options)
  {
    super(options);
  }

  @Override
  protected StringBuilder initCollector()
  {
    return new StringBuilder();
  }

  @Override
  protected void writeHeader(StringBuilder buffer, Playlist playlist)
  {
    buffer.append("#EXTM3U").append(Constants.BR);
  }

  @Override
  protected void writeEntry(StringBuilder buffer, PlaylistEntry entry, int index)
  {
    buffer.append(serializeEntry(entry)).append(Constants.BR);
  }

  private String serializeEntry(PlaylistEntry entry)
  {
    StringBuilder result = new StringBuilder();
    if (!(entry.getExtInf() == null) && !(entry.getExtInf().equals("")))
    {
      result.append(entry.getExtInf());
      result.append(Constants.BR);
    }
    result.append(entry.trackPathToString());
    return result.toString();
  }

  @Override
  protected void finalize(StringBuilder buffer, Playlist playlist) throws IOException
  {
    final File playListFile = playlist.getFile();

    final File dirToSaveIn = playListFile.getParentFile().getAbsoluteFile();
    if (!dirToSaveIn.exists())
    {
      dirToSaveIn.mkdirs();
    }

    FileOutputStream outputStream = new FileOutputStream(playListFile);
    if (playlist.isUtfFormat() || playListFile.getName().toLowerCase().endsWith("m3u8"))
    {
      Writer osw = new OutputStreamWriter(outputStream, "UTF8");
      try (BufferedWriter output = new BufferedWriter(osw))
      {
        if (OperatingSystem.isWindows())
        {
          // For some reason, linux players seem to choke on this header when I addAt it... perhaps the stream classes do it automatically.
          output.write(UnicodeUtils.getBOM("UTF-8"));
        }
        output.write(buffer.toString());
      }
      outputStream.close();
      playlist.setUtfFormat(true);
    }
    else
    {
      try (BufferedOutputStream output = new BufferedOutputStream(outputStream))
      {
        output.write(buffer.toString().getBytes());
      }
      outputStream.close();
      playlist.setUtfFormat(false);
    }
  }
}
