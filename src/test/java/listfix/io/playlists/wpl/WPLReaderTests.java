package listfix.io.playlists.wpl;

import listfix.io.IPlaylistOptions;
import listfix.json.JsonAppOptions;
import listfix.model.playlists.PlaylistEntry;
import listfix.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WPLReaderTests
{
  private IPlaylistOptions playlistOptions;

  @BeforeEach
  public void initOptions()
  {
    this.playlistOptions = new JsonAppOptions();
  }

  private WPLReader buildWplReader(String playlist) throws IOException
  {
    File m3uPlaylistFile = TestUtil.createFileFromResource(this, "/playlists/wpl/" + playlist);
    return new WPLReader(this.playlistOptions, m3uPlaylistFile.toPath());
  }

  @Test
  @DisplayName("Read WPL Playlist: 2seq.wpl")
  public void read_2seq() throws IOException
  {
    WPLReader wplReader = buildWplReader("2seq.wpl");
    List<PlaylistEntry> wplPlaylist = wplReader.readPlaylist();
    assertNotNull(wplPlaylist, "PlaylistFactory should read and construct M3U playlist");
    assertEquals(4, wplPlaylist.size(), "M3U playlist contains 2 tracks");
    assertEquals(StandardCharsets.UTF_8, wplReader.getEncoding());
  }

  @Test
  @DisplayName("Read WPL Playlist: playlist.wpl")
  public void read_playlist() throws IOException
  {
    WPLReader wplReader = buildWplReader("playlist.wpl");
    List<PlaylistEntry> wplPlaylist = wplReader.readPlaylist();
    assertNotNull(wplPlaylist, "PlaylistFactory should read and construct M3U playlist");
    assertEquals(2, wplPlaylist.size(), "M3U playlist contains 2 tracks");
    assertEquals(StandardCharsets.UTF_8, wplReader.getEncoding());
  }
}
