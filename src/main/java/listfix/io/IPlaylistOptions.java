package listfix.io;

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
}
