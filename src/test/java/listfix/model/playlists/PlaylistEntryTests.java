package listfix.model.playlists;


import listfix.util.OperatingSystem;
import org.junit.jupiter.api.Test;


import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class PlaylistEntryTests
{
  @Test
  public void test()
  {
    final Path trackPath = new File("..\\_World Music\\12. Yuri Buenaventura - Bésame Mucho.flac").toPath();
    final String extra = "";

    final Path playlistFile = OperatingSystem.isWindows() ?
        Path.of("\\\\DiskStation\\music\\_playlists\\Broken.m3u8") :
        Path.of("/volume1/music/_playlists/Broken.m3u8");

    FilePlaylistEntry filePlaylistEntry = new FilePlaylistEntry(trackPath, extra, playlistFile);
    assertEquals(Path.of("..", "_World Music").toString(), filePlaylistEntry.getTrackFolder());
    assertEquals("12. Yuri Buenaventura - Bésame Mucho.flac", filePlaylistEntry.getTrackFileName());
    assertTrue(filePlaylistEntry.isRelative());
    assertEquals(Path.of("..", "_World Music", "12. Yuri Buenaventura - Bésame Mucho.flac"), filePlaylistEntry.getTrackPath());
    if (OperatingSystem.isWindows())
    {
      assertEquals("\\\\DiskStation\\music\\_World Music\\12. Yuri Buenaventura - Bésame Mucho.flac", filePlaylistEntry.getAbsolutePath().toString());
    }
    else
    {
      assertEquals("/volume1/music/_World Music/12. Yuri Buenaventura - Bésame Mucho.flac", filePlaylistEntry.getAbsolutePath().toString());
    }
  }
}
