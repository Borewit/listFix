package listfix.model.playlists;

import listfix.io.IPlaylistOptions;
import listfix.json.JsonAppOptions;
import listfix.util.TestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class PlaylistFactoryTest
{
  public static final String playlist_m3u_example = "/playlists/m3u/playlist.m3u";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private IPlaylistOptions playlistOptions;

  @Before
  public void initOptions() {
    this.playlistOptions = new JsonAppOptions();
  }

  @Test
  public void readPlaylistM3u() throws IOException
  {
    File m3uPlaylistFile = TestUtil.createFileFromResource(this.temporaryFolder, "/playlists/m3u/playlist.m3u", "playlist.m3u");

    Playlist m3uPlaylist = PlaylistFactory.getPlaylist(m3uPlaylistFile.toPath(), null, playlistOptions);
    assertNotNull(m3uPlaylist, "PlaylistFactory should read and construct M3U playlist");
    assertEquals(2, m3uPlaylist.getEntries().size(), "M3U playlist contains 2 tracks");
  }

  @Test
  public void readPlaylistPls() throws IOException
  {
    File plsPlaylistFile = TestUtil.createFileFromResource(this.temporaryFolder, "/playlists/pls/playlist.pls", "playlist.pls");

    Playlist plsPlaylist = PlaylistFactory.getPlaylist(plsPlaylistFile.toPath(), null, playlistOptions);
    assertNotNull(plsPlaylist, "PlaylistFactory should read and construct PLS playlist");
    assertEquals(2, plsPlaylist.getEntries().size(), "PLS playlist contains 4 tracks");
  }
}
