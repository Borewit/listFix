package listfix.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Sanitize path name to prevent it cannot be read by Path
 */
public class PathSanitizer
{

  private static final boolean IS_WINDOWS = File.separatorChar == '\\';

  public static String sanitize(String input)
  {
    // Use platform-specific separator to split
    String separator = IS_WINDOWS ? "\\" : "/";
    String[] parts = input.split("[/\\\\]+");  // split on both / and \ for safety

    StringBuilder sanitizedPath = new StringBuilder();

    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];

      String sanitizedPart;
      if (IS_WINDOWS && i == 0 && part.substring(0, 2).matches("^[A-Z]:$")) {
        sanitizedPart = part.substring(0, 2) + sanitizeWindowsPath(part.substring(2));
      }
      else {
          sanitizedPart = IS_WINDOWS ? sanitizeWindowsPath(part) : sanitizeUnixPath(part);
      }

      if (i > 0) {
        sanitizedPath.append(File.separator);
      }
      sanitizedPath.append(sanitizedPart);
    }

    return sanitizedPath.toString();
  }

  private static String sanitizeWindowsPath(String input)
  {
    String sanitized = input.replaceAll("[\\\\/:*?\"<>|]", "_");

    if (sanitized.matches("(?i)^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\\..*)?$")) {
      sanitized = "_" + sanitized;
    }

    return sanitized;
  }

  private static String sanitizeUnixPath(String input) {
    return input.replace("/", "_");
  }

  public static Path makePath(String path)
  {
    // Now we can safely call Paths.get after sanitizing
    return Paths.get(sanitize(path));
  }
}
