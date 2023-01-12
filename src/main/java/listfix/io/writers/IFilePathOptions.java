package listfix.io.writers;

public interface IFilePathOptions
{
  boolean getAlwaysUseUNCPaths();

  boolean getSavePlaylistsWithRelativePaths();

  String getIgnoredSmallWords();
}
