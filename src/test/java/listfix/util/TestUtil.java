package listfix.util;

import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestUtil
{
  public static File createFileFromResource(TemporaryFolder targetFolder, String resourcePath, String filename) throws IOException
  {
    File newFile = targetFolder.newFile(filename);
    try (InputStream inputStream = TestUtil.class.getResourceAsStream(resourcePath))
    {
      assertNotNull(inputStream, "Load test resource: \"" + resourcePath + "\"");
      java.nio.file.Files.copy(
        inputStream,
        newFile.toPath(),
        StandardCopyOption.REPLACE_EXISTING);
    }
    return newFile;
  }
}
