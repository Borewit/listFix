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
import listfix.util.UnicodeUtils;

import java.io.*;

/**
 * A playlist writer capable of saving to PLS format.
 * @author jcaron
 */
public class PLSWriter extends PlaylistWriter<StringBuilder>
{

  public PLSWriter(IPlaylistOptions options) {
    super(options);
  }

  @Override
  protected StringBuilder initCollector() throws Exception
  {
    return new StringBuilder();
  }

  @Override
  protected void writeHeader(StringBuilder buffer, Playlist playlist)
  {
    buffer.append("[playlist]").append(Constants.BR);
  }

  @Override
  protected void writeEntry(StringBuilder buffer, PlaylistEntry entry, int index)
  {
    buffer.append(serializeEntry(entry, index + 1));
  }

  private String serializeEntry(PlaylistEntry entry, int index)
  {
    StringBuilder result = new StringBuilder();

    // set the file
    if (!entry.isURL())
    {
      result.append("File").append(index).append("=");
      result.append(entry.trackPathToString());
    }
    else
    {
      result.append("File").append(index).append("=").append(result.append(entry.trackPathToString()));
    }
    result.append(Constants.BR);

    // set the _title
    result.append("Title").append(index).append("=").append(entry.getTitle()).append(Constants.BR);

    // set the _length
    long lengthToSeconds = entry.getLength() == -1 ? entry.getLength() : entry.getLength() / 1000L;
    result.append("Length").append(index).append("=").append(lengthToSeconds).append(Constants.BR);

    return result.toString();
  }

  @Override
  protected void finalize(StringBuilder buffer, Playlist playlist) throws IOException
  {
    buffer.append("NumberOfEntries=").append(playlist.getEntries().size()).append(Constants.BR);
    buffer.append("Version=2");

    final File playlistFile = playlist.getFile();

    File dirToSaveIn = playlistFile.getParentFile().getAbsoluteFile();
    if (!dirToSaveIn.exists())
    {
      dirToSaveIn.mkdirs();
    }

    if (playlist.isUtfFormat())
    {
      try (FileOutputStream outputStream = new FileOutputStream(playlistFile))
      {
        Writer osw = new OutputStreamWriter(outputStream, "UTF8");
        try (BufferedWriter output = new BufferedWriter(osw))
        {
          output.write(UnicodeUtils.getBOM("UTF-8") + buffer.toString());
        }
      }
      playlist.setUtfFormat(true);
    }
    else
    {
      try (FileOutputStream outputStream = new FileOutputStream(playlistFile); BufferedOutputStream output = new BufferedOutputStream(outputStream))
      {
        output.write(buffer.toString().getBytes());
      }
      playlist.setUtfFormat(false);
    }
  }
}
