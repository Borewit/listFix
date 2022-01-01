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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import listfix.controller.GUIDriver;
import listfix.io.Constants;
import listfix.io.FileUtils;
import listfix.io.UNCFile;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;
import listfix.util.OperatingSystem;
import listfix.util.UnicodeUtils;
import listfix.view.support.ProgressAdapter;

/**
 * A playlist writer capable of saving to M3U or M3U8 format.
 * @author jcaron
 */
public class M3UWriter implements IPlaylistWriter
{
  /**
   * Saves the list to disk.  If the list's filename ends in m3u8, will save in M3U8 format.  Otherwise saves in M3U format.
   * @param list The list to persist to disk.
   * @param saveRelative Specifies if the playlist should be written out relatively or not.
   * @param adapter An optionally null progress adapter which lets other code monitor the progress of this operation.
   * @throws IOException
   */
  @Override
  public void save(Playlist list, boolean saveRelative, ProgressAdapter adapter) throws IOException
  {
    boolean track = adapter != null;
    List<PlaylistEntry> entries = list.getEntries();
    File listFile = list.getFile();
    StringBuilder buffer = new StringBuilder();
    buffer.append("#EXTM3U").append(Constants.BR);
    PlaylistEntry entry;
    String relativePath;
    for (int i = 0; i < entries.size(); i++)
    {
      if (!track || !adapter.getCancelled())
      {
        if (track)
        {
          adapter.stepCompleted();
        }
        entry = entries.get(i);
        if (!entry.isURL())
        {
          if (!saveRelative && entry.isRelative() && entry.getAbsoluteFile() != null)
          {
            // replace existing relative entry with a new absolute one
            File absolute = entry.getAbsoluteFile().getCanonicalFile();

            // Switch to UNC representation if selected in the options
            if (GUIDriver.getInstance().getAppOptions().getAlwaysUseUNCPaths())
            {
              UNCFile temp = new UNCFile(absolute);
              absolute = new File(temp.getUNCPath());
            }

            // make the entry and addAt it
            entry = new PlaylistEntry(absolute, entry.getExtInf(), listFile);
            entries.set(i, entry);
          }
          else if (saveRelative && entry.isFound())
          {
            // replace existing entry with a new relative one
            relativePath = FileUtils.getRelativePath(entry.getAbsoluteFile().getCanonicalFile(), listFile);
            if (!OperatingSystem.isWindows() && !relativePath.contains(Constants.FS))
            {
              relativePath = "." + Constants.FS + relativePath;
            }

            // make a new file out of this relative path, and see if it's really relative...
            // if it's absolute, we have to perform the UNC check and convert if necessary.
            File temp = new File(relativePath);
            if (temp.isAbsolute())
            {
              // Switch to UNC representation if selected in the options
              if (GUIDriver.getInstance().getAppOptions().getAlwaysUseUNCPaths())
              {
                UNCFile uncd = new UNCFile(temp);
                temp = new File(uncd.getUNCPath());
              }
            }

            // make the entry and addAt it
            entry = new PlaylistEntry(temp, entry.getExtInf(), listFile);
            entries.set(i, entry);
          }
        }

        buffer.append(serializeEntry(entry)).append(Constants.BR);
      }
      else
      {
        return;
      }
    }

    if (!track || !adapter.getCancelled())
    {
      File dirToSaveIn = listFile.getParentFile().getAbsoluteFile();
      if (!dirToSaveIn.exists())
      {
        dirToSaveIn.mkdirs();
      }

      FileOutputStream outputStream = new FileOutputStream(listFile);
      if (list.isUtfFormat() || listFile.getName().toLowerCase().endsWith("m3u8"))
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
        list.setUtfFormat(true);
      }
      else
      {
        try (BufferedOutputStream output = new BufferedOutputStream(outputStream))
        {
          output.write(buffer.toString().getBytes());
        }
        outputStream.close();
        list.setUtfFormat(false);
      }
    }
  }

  private String serializeEntry(PlaylistEntry entry)
  {
    StringBuilder result = new StringBuilder();
    if (!(entry.getExtInf() == null) && !(entry.getExtInf().equals("")))
    {
      result.append(entry.getExtInf());
      result.append(Constants.BR);
    }
    if (!entry.isURL())
    {
      if (!entry.isRelative())
      {
        if (entry.getPath().endsWith(Constants.FS))
        {
          result.append(entry.getPath());
          result.append(entry.getFileName());
        }
        else
        {
          result.append(entry.getPath());
          result.append(Constants.FS);
          result.append(entry.getFileName());
        }
      }
      else
      {
        String tempPath = entry.getFile().getPath();
        if (tempPath.substring(0, tempPath.indexOf(entry.getFileName())).equals(Constants.FS))
        {
          result.append(entry.getFileName());
        }
        else
        {
          result.append(entry.getFile().getPath());
        }
      }
    }
    else
    {
      result.append(entry.getURI().toString());
    }
    return result.toString();
  }
}
