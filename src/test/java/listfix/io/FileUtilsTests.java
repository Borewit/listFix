package listfix.io;

import listfix.util.OperatingSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileUtilsTests
{

  @TempDir
  public File mediaFolder;

  @Test
  public void getFileExtension()
  {
    assertEquals("mp3", FileUtils.getFileExtension("audio.mp3"));
    assertNull(FileUtils.getFileExtension("audio"));
  }

  @Test
  public void isMediaFile()
  {
    assertTrue(FileUtils.isMediaFile("audio.mp3"), "audio.mp3");
    assertTrue(FileUtils.isMediaFile("audio.MP3"), "audio.MP3");
    assertTrue(FileUtils.isMediaFile("audio.mP3"), "audio.mP3");
    assertFalse(FileUtils.isMediaFile("notepad.exe"), "notepad.exe");
    assertFalse(FileUtils.isMediaFile("audio"), "audio");
  }

  @Test
  public void getRelativePath() throws IOException
  {
    File playlistFolder = Files.createDirectory(this.mediaFolder.toPath().resolve("playlists")).toFile();
    File playlistFile = new File(playlistFolder, "playlist.m3u8");
    assertTrue(playlistFile.createNewFile(), "Create playlist.m3u8");
    File trackFile = Path.of(this.mediaFolder.getPath(), "Madonna", "Like a Prayer.mp3").toFile();
    assertEquals(Path.of("..", "Madonna", "Like a Prayer.mp3").toString(), FileUtils.getRelativePath(trackFile, playlistFile));
  }

  @Test
  public void getRelativePathWithUncommonRoot() throws IOException
  {
    Path trackPath = Path.of("C:", "music", "track.flac");
    Path playlistFile = Path.of("D:", "playlist", "playlist.m3u8");
    Path result = FileUtils.getRelativePath(trackPath, playlistFile);
    if (OperatingSystem.isWindows())
    {
      assertEquals(trackPath, result, "getRelativePath() should preserve absolute path of there is no common root");
    }
    // ToDo: check this can be ported to Unix as well
  }
}
