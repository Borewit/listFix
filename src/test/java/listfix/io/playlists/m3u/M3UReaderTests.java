package listfix.io.playlists.m3u;

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

public class M3UReaderTests
{
  private IPlaylistOptions playlistOptions;

  @BeforeEach
  public void initOptions()
  {
    this.playlistOptions = new JsonAppOptions();
  }

  private M3UReader buildM3uReader(String playlist) throws IOException
  {
    File m3uPlaylistFile = TestUtil.createFileFromResource(this, "/playlists/m3u/" + playlist);
    return new M3UReader(this.playlistOptions, m3uPlaylistFile.toPath());
  }

  @Test
  @DisplayName("Read MP3U Playlist UTF-8 no BOM")
  public void readPlaylistM3uUtf8NoBom() throws IOException
  {
    M3UReader m3UReader = buildM3uReader("playlist-utf8.m3u");
    List<PlaylistEntry> m3uPlaylist = m3UReader.readPlaylist();
    assertNotNull(m3uPlaylist, "PlaylistFactory should read and construct M3U playlist");
    assertEquals(2, m3uPlaylist.size(), "M3U playlist contains 2 tracks");
    assertEquals(StandardCharsets.UTF_8, m3UReader.getEncoding());
  }

  @Test
  @DisplayName("Read MP3U Playlist UTF-16-BE with BOM")
  public void readPlaylistM3uUtf16BeWithBom() throws IOException
  {
    M3UReader m3UReader = buildM3uReader("playlist-utf16be-bom.m3u");
    List<PlaylistEntry> m3uPlaylist = m3UReader.readPlaylist();
    assertNotNull(m3uPlaylist, "PlaylistFactory should read and construct M3U playlist");
    assertEquals(2, m3uPlaylist.size(), "M3U playlist contains 2 tracks");
    assertEquals(StandardCharsets.UTF_16BE, m3UReader.getEncoding());
  }

  @Test
  @DisplayName("Read MP3U Playlist UTF-16-LE with BOM")
  public void readPlaylistM3uUtf16LeWithBom() throws IOException
  {
    M3UReader m3UReader = buildM3uReader("playlist-utf16le-bom.m3u");
    List<PlaylistEntry> m3uPlaylist = m3UReader.readPlaylist();
    assertNotNull(m3uPlaylist, "PlaylistFactory should read and construct M3U playlist");
    assertEquals(2, m3uPlaylist.size(), "M3U playlist contains 2 tracks");
    assertEquals(StandardCharsets.UTF_16LE, m3UReader.getEncoding());
  }
}
