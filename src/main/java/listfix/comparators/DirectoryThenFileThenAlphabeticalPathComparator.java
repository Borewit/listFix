package listfix.comparators;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Sorts directories ahead of files, and then sorts files alphabetically (ignoring case).
 */
public class DirectoryThenFileThenAlphabeticalPathComparator implements Comparator<Path>
{

  @Override
  public int compare(Path a, Path b)
  {
    if (!Files.isDirectory(a) && Files.isDirectory(b))
    {
      return 1;
    }
    else if (Files.isDirectory(a) && !Files.isDirectory(b))
    {
      return -1;
    }
    else
    {
      // both Files are files or both are directories
      return a.compareTo(b);
    }
  }
}
