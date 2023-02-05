/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2010 Jeremy Caron
 *
 *  This file is part of listFix().
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.io.playlists.itunes;

import listfix.io.IPlaylistOptions;
import listfix.io.playlists.PlaylistWriter;
import listfix.model.playlists.FilePlaylistEntry;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;
import listfix.model.playlists.UriPlaylistEntry;
import listfix.model.playlists.itunes.IITunesPlaylistEntry;
import listfix.model.playlists.itunes.ITunesPlaylist;
import listfix.model.playlists.itunes.ITunesTrack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author jcaron
 */
public class ITunesXMLWriter extends PlaylistWriter<Map<String, ITunesTrack>>
{
  private static final Logger _logger = LogManager.getLogger(ITunesXMLWriter.class);

  private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">";

  public ITunesXMLWriter(IPlaylistOptions playListOptions)
  {
    super(playListOptions);
  }

  @Override
  protected Map<String, ITunesTrack> initCollector()
  {
    return new TreeMap<String, ITunesTrack>();
  }

  @Override
  protected void writeEntry(Map<String, ITunesTrack> collector, PlaylistEntry entry, int index) throws Exception
  {
    final ITunesTrack iTunesTrack = ((IITunesPlaylistEntry) entry).getTrack();

    URI mediaURI;
    if (entry.isURL())
    {
      mediaURI = ((UriPlaylistEntry) entry).getURI();
    }
    else
    {
      FilePlaylistEntry fileEntry = (FilePlaylistEntry) entry;
      mediaURI = fileEntry.getAbsolutePath().toUri();
    }

    mediaURI = normalizeFileUri(mediaURI);

    iTunesTrack.setLocation(mediaURI.toString());
    collector.put(iTunesTrack.getTrackId(), iTunesTrack);
  }

  @Override
  protected void finalize(Map<String, ITunesTrack> trackMap, Playlist playlist) throws Exception
  {
    ITunesPlaylist iList = (ITunesPlaylist) playlist;
    iList.getLibrary().setTracks(trackMap);

    try
    {
      try (FileOutputStream stream = new FileOutputStream(playlist.getFile()))
      {
        iList.getLibrary().getPlist().writeTo(stream, "UTF-8");
      }

      //FileWriter second argument is for append if its true than FileWriter will
      //write bytes at the end of File (append) rather than beginning of file
      insertStringInFile(playlist.getFile(), 1, HEADER);
    }
    catch (Exception ex)
    {
      _logger.error("Error writing library", ex);
      throw ex;
    }
  }

  private void insertStringInFile(File inFile, int lineno, String lineToBeInserted) throws Exception
  {
    // temp file
    File outFile = File.createTempFile("temp", ".txt");

    // input
    FileInputStream fis = new FileInputStream(inFile);
    try (BufferedReader in = new BufferedReader(new InputStreamReader(fis)))
    {
      FileOutputStream fos = new FileOutputStream(outFile);
      try (PrintWriter out = new PrintWriter(fos))
      {
        String thisLine;
        int i = 1;
        while ((thisLine = in.readLine()) != null)
        {
          if (i == lineno)
          {
            out.println(lineToBeInserted);
          }
          out.println(thisLine);
          i++;
        }
        out.flush();
      }
    }

    inFile.delete();
    outFile.renameTo(inFile);
  }

  // Files not on UNC drives need to have file:// as the prefix.   // UNCs need file:///.
  private URI normalizeFileUri(URI mediaURI) throws URISyntaxException
  {
    // What do we do w/ UNC?  Does iTunes even support this?
    if (!mediaURI.toString().startsWith("file://") && mediaURI.toString().startsWith("file:/"))
    {
      try
      {
        mediaURI = new URI(mediaURI.toString().replace("file:/", "file://localhost/"));
      }
      catch (URISyntaxException ex)
      {
       throw new RuntimeException(ex);
      }
    }
    else if (mediaURI.toString().startsWith("file:////"))
    {
      try
      {
        mediaURI = new URI(mediaURI.toString().replace("file:////", "file://localhost//"));
      }
      catch (URISyntaxException ex)
      {
        throw new RuntimeException(ex);
      }
    }
    return mediaURI;
  }
}
