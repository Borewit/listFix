package listfix.util;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PathSanitizer tests")
public class PathSanitizerTests
{
  @Test
  @DisplayName("Handle Windows path containing illegal character \"?\"")
  public void checkMediaMimeTypeMappingsPresence()
  {

    // Windows specific
    Path path = PathSanitizer.makePath("..\\Dillon\\2011 - This Silence Kills\\03. Thirteen ? Thirtyfive.flac");
    assertNotNull(path);
    assertTrue(path.getFileName().toString().startsWith("03. Thirteen "));
    assertNotNull(path.getParent(), "the file should have a parent path");
    assertEquals(path.getParent().getFileName().toString(), "2011 - This Silence Kills");
  }
}
