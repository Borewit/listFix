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
import java.util.Objects;
import java.util.stream.Collectors;

public class ITunesXMLReader extends PlaylistReader
{
  private static final Logger _logger = LogManager.getLogger(ITunesXMLReader.class);

  private ITunesMediaLibrary _library;

  public ITunesXMLReader(IPlaylistOptions playListOptions, Path itunesXmlFile)
  {
    super(playListOptions, itunesXmlFile);
    encoding = UnicodeUtils.getEncoding(playlistPath);
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
      return getPlaylistEntries(results, (PlistPlaylist) playlist);
    }
    else
    {
      throw new IOException("Source XML file did not contain a playlist");
    }
  }

  private List<PlaylistEntry> getPlaylistEntries(List<PlaylistEntry> results, PlistPlaylist playlist)
  {
    _library = new ITunesMediaLibrary(playlist);
    Map<String, ITunesTrack> tracks = getLibrary().getTracks();

    results.addAll(
    getLibrary().getTracks().keySet().stream()
      .map(id -> iTunesTrackToPlaylistEntry(tracks.get(id)))
      .filter(Objects::nonNull)
      .collect(Collectors.toList())
    );
    return results;
  }

  @Override
  public List<PlaylistEntry> readPlaylist() throws IOException
  {
    List<PlaylistEntry> results = new ArrayList<>();
    SpecificPlaylist playlist = SpecificPlaylistFactory.getInstance().readFrom(playlistPath.toFile());
    return getPlaylistEntries(results, (PlistPlaylist) playlist);
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
        return new ITunesFilePlaylistEntry(Path.of(new URI(track.getLocation()).getPath()), track.getArtist() + " - " + track.getName(), track.getDuration(), playlistPath, track);
      }
    }
    catch (URISyntaxException ex)
    {
      _logger.error("[ITunesXMLReader] - Failed to create a PlaylistEntry from a ITunesTrack, see exception for details.", ex);
      return null;
    }
  }

  /**
   * Returns the _library.
   */
  public ITunesMediaLibrary getLibrary()
  {
    return _library;
  }
}
