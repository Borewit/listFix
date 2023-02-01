package listfix.model.playlists;


import listfix.util.OperatingSystem;
import org.junit.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class PlaylistEntryTests
{
  @Test
  public void test() {

    final Path trackPath = Path.of("..\\_World Music\\12. Yuri Buenaventura - Bésame Mucho.flac");
    final String extra = "";
    final Path playlistFile = Path.of("\\\\DiskStation\\music\\_playlists\\Broken.m3u8");

    FilePlaylistEntry filePlaylistEntry = new FilePlaylistEntry(trackPath, extra, playlistFile);
    if(OperatingSystem.isWindows())
    {
      assertEquals("..\\_World Music", filePlaylistEntry.getTrackFolder());
      assertEquals("12. Yuri Buenaventura - Bésame Mucho.flac", filePlaylistEntry.getTrackFileName());
      assertTrue(filePlaylistEntry.isRelative());
      assertEquals("..\\_World Music\\12. Yuri Buenaventura - Bésame Mucho.flac", filePlaylistEntry.getTrackPath().toString());
      assertEquals("\\\\DiskStation\\music\\_World Music\\12. Yuri Buenaventura - Bésame Mucho.flac", filePlaylistEntry.getAbsolutePath().toString());
    }
  }
}
