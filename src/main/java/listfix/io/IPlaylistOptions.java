package listfix.io;

import java.util.Set;

public interface IPlaylistOptions
{
  boolean getAlwaysUseUNCPaths();

  boolean getSavePlaylistsWithRelativePaths();

  String getIgnoredSmallWords();

  /**
   * Returns the maxClosestResults.
   */
  int getMaxClosestResults();

  /**
   * Returns use case-insensitive / exact path name comparison.
   */
  boolean getCaseInsensitiveExactMatching();

  /**
   * Returns list of user defined playlist directories.
   */
  Set<String> getPlaylistDirectories();
}
