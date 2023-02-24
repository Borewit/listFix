

package listfix.playlists.itunes;

import christophedelory.playlist.SpecificPlaylist;
import christophedelory.playlist.SpecificPlaylistFactory;
import christophedelory.playlist.plist.PlistPlaylist;
import listfix.io.IPlaylistOptions;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;
import listfix.model.playlists.itunes.*;
import listfix.util.TestUtil;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author jcaron, Borewit
 */
public class ITunesPlaylistTests
{

  @Test
  public void write_iTunes_music_library() throws Exception
  {
    File toOpen = TestUtil.createFileFromResource(this, "/playlists/itunes/iTunesMusicLibrary.xml");
    // File toOpen = new File("C:\\Users\\jcaron\\Desktop\\svnListfix\\testing\\iTunesTest.xml");
    SpecificPlaylist playlist = SpecificPlaylistFactory.getInstance().readFrom(toOpen);
    PlistPlaylist plistList = (PlistPlaylist) playlist;
    try (FileOutputStream stream = new FileOutputStream(toOpen))
    {
      plistList.writeTo(stream, "UTF-8");
    }
  }

  @Test
  public void openPlaylist() throws Exception
  {
    final File toOpen = TestUtil.createFileFromResource(this, "/playlists/itunes/iTunesMusicLibrary.xml");

    SpecificPlaylist playlist = SpecificPlaylistFactory.getInstance().readFrom(toOpen);
    PlistPlaylist plistList = (PlistPlaylist) playlist;
    try (FileOutputStream stream = new FileOutputStream(toOpen))
    {
      plistList.writeTo(stream, "UTF-8");
    }
    ITunesMediaLibrary list = new ITunesMediaLibrary(plistList);
    Playlist myList = convertToListFixPlaylist(list, toOpen.toPath(), new IPlaylistOptions()
    {
      @Override
      public boolean getAlwaysUseUNCPaths()
      {
        return false;
      }

      @Override
      public boolean getSavePlaylistsWithRelativePaths()
      {
        return false;
      }

      @Override
      public String getIgnoredSmallWords()
      {
        return "";
      }

      @Override
      public int getMaxClosestResults()
      {
        return 0;
      }

      @Override
      public boolean getCaseInsensitiveExactMatching()
      {
        return true;
      }

      @Override
      public Set<String> getPlaylistDirectories() {
        return new TreeSet<>();
      }
    });
  }

  private static Playlist convertToListFixPlaylist(ITunesMediaLibrary list, Path playlistPath, IPlaylistOptions playListOptions)
  {
    List<PlaylistEntry> newList = new ArrayList<>();
    Map<String, ITunesTrack> tracks = list.getTracks();

    for (String id : tracks.keySet())
    {
      ITunesTrack track = tracks.get(id);
      try
      {
        URI trackUri = new URI(track.getLocation());
        File trackFile = new File(trackUri.getPath());
        newList.add(new ITunesFilePlaylistEntry(trackFile.toPath(), track.getArtist() + " - " + track.getName(), track.getDuration(), playlistPath, track));
      }
      catch (URISyntaxException ex)
      {
        throw new RuntimeException(ex);
      }
    }
    try
    {
      return new ITunesPlaylist(playlistPath.toFile(), newList, list, playListOptions);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
}
