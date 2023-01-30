package listfix.io;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;
public class FileUtilsTests
{

  @Test
  public void getFileExtension() {
    assertEquals("mp3", FileUtils.getFileExtension("audio.mp3"));
    assertNull(FileUtils.getFileExtension("audio"));
  }

  @Test
  public void isMediaFile() {
    assertTrue(FileUtils.isMediaFile("audio.mp3"), "audio.mp3");
    assertTrue(FileUtils.isMediaFile("audio.MP3"), "audio.MP3");
    assertTrue(FileUtils.isMediaFile("audio.mP3"), "audio.mP3");
    assertFalse(FileUtils.isMediaFile("notepad.exe"), "notepad.exe");
    assertFalse(FileUtils.isMediaFile("audio"), "audio");
  }
}
