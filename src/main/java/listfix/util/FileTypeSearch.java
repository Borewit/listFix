package listfix.util;

import listfix.comparators.DirectoryThenFileThenAlphabeticalPathComparator;


import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileTypeSearch
{

  public List<Path> findFiles(Path directoryToSearch, FileFilter filter)
  {
    List<Path> files = new ArrayList<>();
    this.findFiles(files, directoryToSearch, filter);
    return files;
  }

  private void findFiles(List<Path> files, Path directoryToSearch, FileFilter filter)
  {
    if (Files.exists(directoryToSearch))
    {
      try
      {
        Files.list(directoryToSearch)
          .filter(file -> filter.accept(file.toFile()))
          .sorted(new DirectoryThenFileThenAlphabeticalPathComparator())
          .forEach(file -> {
            if (Files.isDirectory(file))
            {
              findFiles(files, file, filter);
            }
            else
            {
              files.add(file);
            }
          });
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }
  }
}
