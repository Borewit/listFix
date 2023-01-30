package listfix.io;

public interface IPlayListOptions
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
