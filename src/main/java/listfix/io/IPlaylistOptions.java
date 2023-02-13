package listfix.io;

import java.util.Set;

public interface IPlaylistOptions
{
  boolean getAlwaysUseUNCPaths();

  boolean getSavePlaylistsWithRelativePaths();

  String getIgnoredSmallWords();

  /**
   * @return the maxClosestResults
   */
  int getMaxClosestResults();

  /**
   * @return use case-insensitive / exact path name comparison
   */
  boolean getCaseInsensitiveExactMatching();

  /**
   * @return List of user defined playlist directories
   */
  Set<String> getPlaylistDirectories();
}
