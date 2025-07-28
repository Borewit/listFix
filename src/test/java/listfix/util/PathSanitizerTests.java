package listfix.util;

import java.io.File;

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
    boolean isWindows = File.separatorChar == '\\';
    if (isWindows) {
      assertEquals("..\\Dillon\\2011 - This Silence Kills\\03. Thirteen _ Thirtyfive.flac", PathSanitizer.makePath("..\\Dillon\\2011 - This Silence Kills\\03. Thirteen ? Thirtyfive.flac").toString());
      assertEquals(".\\Dillon\\2011 - This Silence Kills\\03. Thirteen _ Thirtyfive.flac", PathSanitizer.makePath(".\\Dillon\\2011 - This Silence Kills\\03. Thirteen ? Thirtyfive.flac").toString());
      assertEquals("\\Dillon\\2011 - This Silence Kills\\03. Thirteen _ Thirtyfive.flac", PathSanitizer.makePath("\\Dillon\\2011 - This Silence Kills\\03. Thirteen ? Thirtyfive.flac").toString());
      assertEquals("Dillon\\2011 - This Silence Kills\\03. Thirteen _ Thirtyfive.flac", PathSanitizer.makePath("Dillon\\2011 - This Silence Kills\\03. Thirteen ? Thirtyfive.flac").toString());
      assertEquals("D:\\Dillon\\2011 - This Silence Kills\\03. Thirteen _ Thirtyfive.flac", PathSanitizer.makePath("D:\\Dillon\\2011 - This Silence Kills\\03. Thirteen ? Thirtyfive.flac").toString());
      assertEquals("D:Dillon\\2011 - This Silence Kills\\03. Thirteen _ Thirtyfive.flac", PathSanitizer.makePath("D:Dillon\\2011 - This Silence Kills\\03. Thirteen ? Thirtyfive.flac").toString());
    }
  }
}
