package listfix.io;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
  public static final Set<String> mediaExtensions =
      Stream.of(
              "mp3", "wma", "flac", "ogg", "wav", "midi", "cda", "mpg", "mpeg", "m2v", "avi", "m4v",
              "flv", "mid", "mp2", "mp1", "aac", "asx", "m4a", "mp4", "m4v", "nsv", "aiff", "au",
              "wmv", "asf", "mpc")
          .collect(Collectors.toCollection(HashSet::new));

  /**
   * Normalize the filename or path string to NFC form. This method is used for internal comparisons
   * only.
   *
   * @param filename the string to normalize.
   * @return normalized string.
   */
  public static String normalizeFilename(String filename) {
    return filename == null ? null : Normalizer.normalize(filename, Form.NFC);
  }

  public static File findDeepestPathToExist(File file) {
    if (file == null || file.exists()) {
      return file;
    }
    return findDeepestPathToExist(file.getParentFile());
  }

  public static boolean isMediaFile(File file) {
    return isMediaFile(file.getName());
  }

  public static boolean isMediaFile(Path file) {
    return isMediaFile(file.getFileName().toString());
  }

  public static boolean isMediaFile(String filename) {
    // For internal comparisons (extension extraction), normalization is safe.
    filename = normalizeFilename(filename);
    String extension = getFileExtension(filename);
    if (extension != null) {
      return mediaExtensions.contains(extension.toLowerCase());
    }
    return false;
  }

  public static Optional<String> getExtension(String name) {
    // Internal operation: normalize for consistent extension extraction.
    name = normalizeFilename(name);
    int ix = name.lastIndexOf('.');
    if (ix >= 0 && ix < name.length() - 1) {
      return Optional.of(name.substring(ix + 1));
    }
    return Optional.empty();
  }

  public static boolean isURL(String trackText) {
    // For URL checks, normalization is safe as URLs should be ASCII-compatible.
    trackText = normalizeFilename(trackText);
    try {
      if (trackText.startsWith("file:")) {
        return false;
      } else {
        new URL(trackText).toURI();
        return true;
      }
    } catch (MalformedURLException | URISyntaxException e) {
      return false;
    }
  }

  public static String getRelativePath(File trackPath, File playlistPath) {
    return getRelativePath(trackPath.toPath(), playlistPath.toPath()).toString();
  }

  public static Path getRelativePath(Path trackPath, Path playlistPath) {
    // Use the raw path strings for file system operations.
    Path rawTrackPath = trackPath;
    Path rawPlaylistPath = playlistPath;
    Path offset =
        Files.isDirectory(rawPlaylistPath) ? rawPlaylistPath : rawPlaylistPath.getParent();
    String uncPath = UNCFile.from(offset.normalize().toFile()).getUNCPath();
    try {
      return Path.of(uncPath).relativize(rawTrackPath);
    } catch (IllegalArgumentException exception) {
      return rawTrackPath;
    }
  }

  public static String replaceInvalidWindowsFileSystemCharsWithChar(
      String input, char replacement) {
    // Use the raw input for file system sanitization.
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char x = input.charAt(i);
      if (Constants.INVALID_WINDOWS_FILENAME_CHARACTERS.indexOf(x) > -1) {
        result.append(replacement);
      } else {
        result.append(x);
      }
    }
    return result.toString();
  }

  /**
   * Delete recursive.
   *
   * @param directory Folder to delete.
   */
  public static void deleteDirectory(Path directory) throws IOException {
    try (Stream<Path> walk = Files.walk(directory)) {
      walk.map(Path::toFile).forEach(File::delete);
    }
  }

  /**
   * Extract the extension from the provided filename.
   *
   * @param filename Filename.
   * @return extension without leading dot, e.g.: "mp3".
   */
  public static String getFileExtension(String filename) {
    // For extension extraction, normalization is used.
    filename = normalizeFilename(filename);
    int index = filename.lastIndexOf(".");
    return index == -1 ? null : filename.substring(index + 1);
  }
}
