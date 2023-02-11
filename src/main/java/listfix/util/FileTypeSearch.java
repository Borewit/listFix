package listfix.util;

import listfix.comparators.DirectoryThenFileThenAlphabeticalFileComparator;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileTypeSearch
{
  /**
   * @param directoryToSearch
   * @param filter
   * @return
   */
  public List<File> findFiles(File directoryToSearch, FileFilter filter)
  {
    if (directoryToSearch.exists())
    {
      File[] inodes = directoryToSearch.listFiles(filter);

      if (inodes != null && inodes.length > 0)
      {
        List<File> ol = new ArrayList<>(Arrays.asList(inodes));
        ol.sort(new DirectoryThenFileThenAlphabeticalFileComparator());
        List<File> files = new ArrayList<>();
        for (File file : ol)
        {
          if (file.isDirectory())
          {
            files.addAll(findFiles(file, filter));
          }
          else
          {
            files.add(file);
          }
        }
        return files;
      }
    }
    return Collections.emptyList();
  }
}
