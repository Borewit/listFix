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

package listfix.io.playlists.xspf;

import christophedelory.content.Content;
import christophedelory.playlist.*;
import christophedelory.playlist.xspf.Track;
import listfix.io.IPlaylistOptions;
import listfix.io.UNCFile;
import listfix.io.playlists.PlaylistWriter;
import listfix.model.playlists.FilePlaylistEntry;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;
import listfix.model.playlists.UriPlaylistEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A playlist writer capable of saving to XSPF ("spiff") format.
 *
 * @author jcaron
 */
public class XSPFWriter extends PlaylistWriter<christophedelory.playlist.Playlist>
{
  private static final Logger _logger = LogManager.getLogger(XSPFWriter.class);
  private Media _trackToAdd;

  public XSPFWriter(IPlaylistOptions playListOptions)
  {
    super(playListOptions);
  }

  @Override
  protected christophedelory.playlist.Playlist initCollector()
  {
    return new christophedelory.playlist.Playlist();
  }

  @Override
  protected void writeEntry(christophedelory.playlist.Playlist lizzyList, PlaylistEntry entry, int index) throws IOException
  {
    lizzyList.getRootSequence().addComponent(getMedia(entry));
  }

  @Override
  protected void finalize(christophedelory.playlist.Playlist lizzyList, Playlist playlist) throws Exception
  {
    SpecificPlaylistProvider spp = SpecificPlaylistFactory.getInstance().findProviderByExtension(playlist.getFilename());
    try
    {
      SpecificPlaylist specificPlaylist = spp.toSpecificPlaylist(lizzyList);
      addXspfMetadata(specificPlaylist, playlist);
      try (FileOutputStream stream = new FileOutputStream(playlist.getFile()))
      {
        specificPlaylist.writeTo(stream, "UTF8");
      }
    }
    catch (Exception ex)
    {
      _logger.error("Failed to write XSPF", ex);
      throw ex;
    }
  }

  private AbstractPlaylistComponent getMedia(PlaylistEntry entry) throws IOException
  {
    _trackToAdd = new Media();
    _trackToAdd.setSource(getContent(entry));
    return _trackToAdd;
  }

  private Content getContent(PlaylistEntry entry) throws IOException
  {
    final boolean _saveRelative = this.playListOptions.getSavePlaylistsWithRelativePaths();
    URI mediaURI;
    if (entry.isURL())
    {
      mediaURI = ((UriPlaylistEntry) entry).getURI();
    }
    else
    {
      FilePlaylistEntry filePlaylistEntry = (FilePlaylistEntry) entry;
      if (filePlaylistEntry.isRelative())
      {
        // replace existing entry with a new relative one
        String relativePath = filePlaylistEntry.getTrackPath().toString();

        // repoint entry object to a new one?
        UNCFile file = new UNCFile(relativePath);
        filePlaylistEntry.setTrackPath(file.toPath());

        if (file.isInUNCFormat())
        {
          relativePath = "file:" + filePlaylistEntry.getTrackPath().toString();
        }
        else
        {
          relativePath = "file://" + filePlaylistEntry.getTrackPath().toString();
        }

        return new Content(relativePath.replace(" ", "%20"));
      }
      else
      {
        mediaURI = ((FilePlaylistEntry) entry).getTrackPath().toUri();
      }
    }
    try
    {
      mediaURI = normalizeFileUri(mediaURI);
    }
    catch (URISyntaxException e)
    {
      throw new RuntimeException(e);
    }
    //repoint entry object to a new one?
    return new Content(mediaURI);
  }

  private void addXspfMetadata(SpecificPlaylist specificPlaylist, Playlist list)
  {
    if (specificPlaylist.toPlaylist().getRootSequence().getComponentsNumber() == list.size())
    {
      christophedelory.playlist.xspf.Playlist castedList = (christophedelory.playlist.xspf.Playlist) specificPlaylist;
      Track track;
      for (int i = 0; i < castedList.getTracks().size(); i++)
      {
        track = castedList.getTracks().get(i);
        track.setTitle(list.getEntries().get(i).getTitle());
        long duration = list.getEntries().get(i).getLength();
        if (duration > 0)
        {
          track.setDuration((int) duration);
        }
      }
    }
  }

  // Files not on UNC drives need to have file:// as the prefix.   // UNCs need file:///.
  private URI normalizeFileUri(URI mediaURI) throws URISyntaxException
  {
    // By default, UNC URIs have file:/ - need to convert to file:///.
    if (!mediaURI.toString().startsWith("file://") && mediaURI.toString().startsWith("file:/"))
    {
      try
      {
        mediaURI = new URI(mediaURI.toString().replace("file:/", "file:///"));
      }
      catch (URISyntaxException ex)
      {
        _logger.error(ex, ex);
      }
    }

    // Non-UNCs have file:////, two of the slashes need to be removed.
    if (mediaURI.toString().contains("////"))
    {
      try
      {
        mediaURI = new URI(mediaURI.toString().replace("////", "//"));
      }
      catch (URISyntaxException ex)
      {
        _logger.error(ex, ex);
        throw ex;
      }
    }
    return mediaURI;
  }
}
