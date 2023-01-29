package listfix.config;

import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class MediaLibraryConfigurationTests
{
  @Test
  public void normalizeFileSetToUNC()
  {

    Set<String> fileList = new TreeSet<>();
    fileList.add("\\\\DiskStation\\music\\Album\\track01.mp3");
    fileList.add("\\\\DiskStation\\music\\Album\\track02.mp3");
    fileList.add("\\\\DiskStation\\music\\Album\\track03.mp3");
    // fileList.add("M:\\Album\\track04.mp3");
    int originalLength = fileList.size();

    MediaLibraryConfiguration.normalizeFileSetToUNC(fileList);
    assertEquals(originalLength, fileList.size(), "Should not remove any files from Set");
    String[] files = fileList.toArray(new String[0]);
    assertAll("String",
      () -> assertEquals("\\\\DiskStation\\music\\Album\\track01.mp3", files[0]),
      () -> assertEquals("\\\\DiskStation\\music\\Album\\track02.mp3", files[1]),
      () -> assertEquals("\\\\DiskStation\\music\\Album\\track03.mp3", files[2])
      // () -> assertEquals( "\\\\DiskStation\\music\\Album\\track04.mp3", files[3])
    );
  }
}
