package listfix.comparators;

import java.io.File;
import java.util.Comparator;

/** Sorts directories ahead of files, and then sorts files alphabetically (ignoring case). */
public class DirectoryThenFileThenAlphabeticalFileComparator implements Comparator<File> {

  @Override
  public int compare(File a, File b) {
    if (!a.isDirectory() && b.isDirectory()) {
      return 1;
    } else if (a.isDirectory() && !b.isDirectory()) {
      return -1;
    } else {
      // both Files are files or both are directories
      return a.getName().compareToIgnoreCase(b.getName());
    }
  }
}
