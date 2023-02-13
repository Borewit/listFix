package listfix.config;

import java.util.Set;

public interface IMediaLibrary
{
  /**
   * User configured top level media directories.
   * Used to search media files in
   */
  Set<String> getMediaDirectories();

  /**
   * Cached nested directories, derived from media-directories
   */
  Set<String> getNestedDirectories();

  /**
   * Cached nested files, derived from media-directories
   */
  Set<String> getNestedMediaFiles();
}
