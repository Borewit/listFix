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

package listfix.io.playlists.wpl;

import listfix.io.Constants;
import listfix.io.IPlaylistOptions;
import listfix.io.UnicodeInputStream;
import listfix.io.playlists.PlaylistWriter;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;

import java.io.*;

/**
 * A playlist writer capable of saving to WPL format.
 *
 * @author jcaron & jpeterson
 */
public class WPLWriter extends PlaylistWriter<StringBuilder>
{
  public WPLWriter(IPlaylistOptions options)
  {
    super(options);
  }

  @Override
  protected StringBuilder initCollector() throws Exception
  {
    return new StringBuilder();
  }

  protected void writeHeader(StringBuilder buffer, Playlist playlist) throws Exception
  {
    final File playlistFile = playlist.getFile();
    buffer.append(getWPLHead(playlistFile));
  }

  @Override
  protected void writeEntry(StringBuilder buffer, PlaylistEntry entry, int index) throws Exception
  {
    String media = "\t\t\t<media src=\"" + XMLEncode(serializeEntry(entry)) + "\"";
    if (!entry.getCID().isEmpty())
    {
      media += " cid=\"" + entry.getCID() + "\"";
    }
    if (!entry.getTID().isEmpty())
    {
      media += " tid=\"" + entry.getTID() + "\"";
    }
    media += "/>" + Constants.BR;
    buffer.append(media);
  }

  @Override
  protected void finalize(StringBuilder buffer, Playlist playlist) throws Exception
  {
    buffer.append(getWPLFoot());

    final File playlistFile = playlist.getFile();

    File dirToSaveIn = playlistFile.getParentFile().getAbsoluteFile();
    if (!dirToSaveIn.exists())
    {
      dirToSaveIn.mkdirs();
    }
    try (FileOutputStream outputStream = new FileOutputStream(playlistFile))
    {
      Writer osw = new OutputStreamWriter(outputStream, "UTF8");
      try (BufferedWriter output = new BufferedWriter(osw))
      {
        output.write(buffer.toString());
      }
    }
    playlist.setUtfFormat(true);

  }

  // WPL Helper Method
  private String XMLEncode(String s)
  {
    s = s.replaceAll("&", "&amp;");
    s = s.replaceAll("'", "&apos;");
    s = s.replaceAll("<", "&lt;");
    s = s.replaceAll(">", "&gt;");
    return s;
  }

  // WPL Helper Method
  private String getWPLHead(File listFile) throws IOException
  {
    String head = "";
    boolean newHead = false;
    try
    {
      try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(listFile), "UTF-8"), "UTF8")))
      {
        String line = buffer.readLine();
        while (line != null)
        {
          if (line.trim().startsWith("<media"))
          {
            break;
          }
          head += line + Constants.BR;
          line = buffer.readLine();
        }
      }
      // determine if a head was read
      if (!head.contains("<?wpl"))
      {
        newHead = true;
      }
    }
    catch (Exception ex)
    {
      // Don't bother logging here, it's expected when saving out a new file
      // _logger.error(ex);
      newHead = true;
    }
    if (newHead)
    {
      head = "<?wpl version=\"1.0\"?>\r\n<smil>\r\n\t<body>\r\n\t\t<sec>\r\n";
    }
    return head;
  }

  // WPL Helper Method
  private String getWPLFoot() throws IOException
  {
    return "\t\t</sec>\r\n\t</body>\r\n</smil>";
  }

  private String serializeEntry(PlaylistEntry entry)
  {
    StringBuilder result = new StringBuilder();
    result.append(entry.trackPathToString());
    return result.toString();
  }
}
