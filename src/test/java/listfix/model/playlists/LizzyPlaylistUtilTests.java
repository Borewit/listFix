package listfix.model.playlists;

import io.github.borewit.lizzy.playlist.PlaylistFormat;
import listfix.io.playlists.LizzyPlaylistUtil;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class LizzyPlaylistUtilTests
{
  @Test
  public void resolvePlaylistExtension() {
    assertEquals(".m3u", LizzyPlaylistUtil.getPreferredExtensionFor(PlaylistFormat.m3u));
  }

  @Test
  public void testPlaylistType() {
    Path asx = Path.of("playlist.asx");
    assertTrue(LizzyPlaylistUtil.isPlaylist(asx), String.format("\"%s\" is a playlist", asx));


    Path m3u8 = Path.of("playlist.m3u8");
    assertTrue(LizzyPlaylistUtil.isPlaylist(m3u8), String.format("\"%s\" is a playlist", m3u8));

    Path mp3 = Path.of("01. Sodade.mp3");
    assertFalse(LizzyPlaylistUtil.isPlaylist(mp3), String.format("\"%s\" is not a playlist", mp3));
  }
}
