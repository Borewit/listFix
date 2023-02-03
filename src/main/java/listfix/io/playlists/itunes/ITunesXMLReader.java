/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2014 Jeremy Caron
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

import christophedelory.playlist.SpecificPlaylist;
import christophedelory.playlist.SpecificPlaylistFactory;
import christophedelory.playlist.plist.PlistPlaylist;
import listfix.io.IPlaylistOptions;
import listfix.io.playlists.PlaylistReader;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.PlaylistEntry;
import listfix.model.playlists.itunes.ITunesFilePlaylistEntry;
import listfix.model.playlists.itunes.ITunesMediaLibrary;
import listfix.model.playlists.itunes.ITunesTrack;
import listfix.model.playlists.itunes.ITunesUriPlaylistEntry;
import listfix.util.UnicodeUtils;
import listfix.view.support.IProgressObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jcaron
 */
public class ITunesXMLReader extends PlaylistReader
{
  private String _encoding;
  private static final Logger _logger = LogManager.getLogger(ITunesXMLReader.class);

  private ITunesMediaLibrary _library;

  /**
   *
   * @param itunesXmlFile
   */
  public ITunesXMLReader(IPlaylistOptions playListOptions, Path itunesXmlFile)
  {
    super(playListOptions, itunesXmlFile);
    _encoding = UnicodeUtils.getEncoding(playlistPath);
  }

  @Override
  public String getEncoding()
  {
    return _encoding;
  }

  @Override
  public void setEncoding(String encoding)
  {
    _encoding = encoding;
  }

  @Override
  public PlaylistType getPlaylistType()
  {
    return PlaylistType.ITUNES;
  }

  @Override
  public List<PlaylistEntry> readPlaylist(IProgressObserver<String> input) throws IOException
  {
    List<PlaylistEntry> results = new ArrayList<>();
    SpecificPlaylist playlist = SpecificPlaylistFactory.getInstance().readFrom(playlistPath.toFile());
    if (playlist != null)
    {
      PlistPlaylist plistList = (PlistPlaylist) playlist;
      _library = new ITunesMediaLibrary(plistList);
      Map<String, ITunesTrack> tracks = getLibrary().getTracks();

      for (String id : tracks.keySet())
      {
        PlaylistEntry convertedTrack = iTunesTrackToPlaylistEntry(tracks.get(id));
        if (convertedTrack != null)
        {
          results.add(convertedTrack);
        }
      }
      return results;
    }
    else
    {
      throw new IOException("Source XML file did not contain a playlist");
    }
  }

  @Override
  public List<PlaylistEntry> readPlaylist() throws IOException
  {
    List<PlaylistEntry> results = new ArrayList<>();
    SpecificPlaylist playlist = SpecificPlaylistFactory.getInstance().readFrom(playlistPath.toFile());
    PlistPlaylist plistList = (PlistPlaylist) playlist;
    _library = new ITunesMediaLibrary(plistList);
    Map<String, ITunesTrack> tracks = getLibrary().getTracks();

    for (String id : tracks.keySet())
    {
      PlaylistEntry convertedTrack = iTunesTrackToPlaylistEntry(tracks.get(id));
      if (convertedTrack != null)
      {
        results.add(convertedTrack);
      }
    }
    return results;
  }

  private PlaylistEntry iTunesTrackToPlaylistEntry(ITunesTrack track)
  {
    try
    {
      if (track.getTrackType().equals(ITunesTrack.URL))
      {
        // result.setTrackId(track.getTrackId());
        return new ITunesUriPlaylistEntry(new URI(track.getLocation()), track);
      }
      else
      {
        // result.setTrackId(track.getTrackId());
        return new ITunesFilePlaylistEntry(Path.of((new URI(track.getLocation())).getPath()), track.getArtist() + " - " + track.getName(), track.getDuration(), playlistPath, track);
      }
    }
    catch (URISyntaxException ex)
    {
      _logger.error("[ITunesXMLReader] - Failed to create a PlaylistEntry from a ITunesTrack, see exception for details.", ex);
      return null;
    }
  }

  /**
   * @return the _library
   */
  public ITunesMediaLibrary getLibrary()
  {
    return _library;
  }
}
