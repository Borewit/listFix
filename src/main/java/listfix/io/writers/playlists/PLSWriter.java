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

import listfix.io.Constants;
import listfix.io.FileUtils;
import listfix.io.UNCFile;
import listfix.io.writers.IFilePathOptions;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;
import listfix.util.OperatingSystem;
import listfix.util.UnicodeUtils;
import listfix.view.support.ProgressAdapter;

/**
 * A playlist writer capable of saving to PLS format.
 * @author jcaron
 */
public class PLSWriter implements IPlaylistWriter
{

  private IFilePathOptions options;

  public PLSWriter(IFilePathOptions options) {
    this.options = options;
  }

  /**
   * Saves the list to disk.
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
    PlaylistEntry entry;
    StringBuilder buffer = new StringBuilder();
    buffer.append("[playlist]").append(Constants.BR);
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
          if (!saveRelative && entry.getAbsoluteFile() != null)
          {
            // replace existing relative entry with a new absolute one
            File absolute = entry.getAbsoluteFile().getCanonicalFile();

            // Switch to UNC representation if selected in the options
            if (this.options.getAlwaysUseUNCPaths())
            {
              UNCFile temp = new UNCFile(absolute);
              absolute = new File(temp.getUNCPath());
            }

            entry = new PlaylistEntry(absolute, entry.getTitle(), entry.getLength(), listFile);
            entries.set(i, entry);
          }
          else if (saveRelative && entry.isFound())
          {
            // replace existing entry with a new relative one
            String relativePath = FileUtils.getRelativePath(entry.getAbsoluteFile().getCanonicalFile(), listFile);
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
              if (this.options.getAlwaysUseUNCPaths())
              {
                UNCFile uncd = new UNCFile(temp);
                temp = new File(uncd.getUNCPath());
              }
            }

            // make the entry and addAt it
            entry = new PlaylistEntry(temp, entry.getTitle(), entry.getLength(), listFile);
            entries.set(i, entry);
          }
        }
        buffer.append(serializeEntry(entry, i + 1));
      }
      else
      {
        return;
      }
    }

    buffer.append("NumberOfEntries=").append(entries.size()).append(Constants.BR);
    buffer.append("Version=2");

    if (!track || !adapter.getCancelled())
    {
      File dirToSaveIn = listFile.getParentFile().getAbsoluteFile();
      if (!dirToSaveIn.exists())
      {
        dirToSaveIn.mkdirs();
      }

      if (list.isUtfFormat())
      {
        try (FileOutputStream outputStream = new FileOutputStream(listFile))
        {
          Writer osw = new OutputStreamWriter(outputStream, "UTF8");
          try (BufferedWriter output = new BufferedWriter(osw))
          {
            output.write(UnicodeUtils.getBOM("UTF-8") + buffer.toString());
          }
        }
        list.setUtfFormat(true);
      }
      else
      {
        try (FileOutputStream outputStream = new FileOutputStream(listFile); BufferedOutputStream output = new BufferedOutputStream(outputStream))
        {
          output.write(buffer.toString().getBytes());
        }
        list.setUtfFormat(false);
      }
    }
  }

  private String serializeEntry(PlaylistEntry entry, int index)
  {
    StringBuilder result = new StringBuilder();

    // set the file
    if (!entry.isURL())
    {
      result.append("File").append(index).append("=");
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
      result.append("File").append(index).append("=").append(entry.getURI().toString());
    }
    result.append(Constants.BR);

    // set the _title
    result.append("Title").append(index).append("=").append(entry.getTitle()).append(Constants.BR);

    // set the _length
    long lengthToSeconds = entry.getLength() == -1 ? entry.getLength() : entry.getLength() / 1000L;
    result.append("Length").append(index).append("=").append(lengthToSeconds).append(Constants.BR);

    return result.toString();
  }
}
