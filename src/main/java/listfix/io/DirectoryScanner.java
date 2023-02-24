

package listfix.io;

import listfix.view.support.ProgressWorker;

import java.io.File;
import java.util.*;

/**
 * Creates a list of the indexed files and subdirectories contained in a list of input directories.
 *
 * @author jcaron
 */

public class DirectoryScanner
{
  private List<String> thisDirList;
  private List<String> thisFileList;
  private int recursiveCount = 0;

  /**
   * @param baseDirs
   * @param task
   */
  public void createMediaLibraryDirectoryAndFileList(Collection<String> baseDirs, ProgressWorker task)
  {
    this.reset();
    for (String baseDir : baseDirs)
    {
      if (new File(baseDir).exists())
      {
        thisDirList.add(baseDir);
        this.recursiveDir(baseDir, task);
      }
    }
  }

  private void recursiveDir(String baseDir, ProgressWorker task)
  {
    recursiveCount++;
    if (!task.getCancelled())
    {
      task.setMessage("<html><body>Scanning Directory #" + recursiveCount + "<BR>" + (baseDir.length() < 70 ? baseDir : baseDir.substring(0, 70) + "...") + "</body></html>");

      File mediaDir = new File(baseDir);
      String[] entryList = mediaDir.list();
      Set<String> fileList = new TreeSet<>();
      Set<String> dirList = new TreeSet<>();

      if (entryList != null)
      {
        File tempFile;
        for (String entryList1 : entryList)
        {
          tempFile = new File(baseDir, entryList1);
          if (tempFile.isDirectory())
          {
            dirList.add(tempFile.getPath());
          }
          else
          {
            if (FileUtils.isMediaFile(tempFile))
            {
              fileList.add(tempFile.getPath());
            }
          }
        }
      }

      thisFileList.addAll(fileList);

      for (String dir : dirList)
      {
        thisDirList.add(dir);
        recursiveDir(dir, task);
      }

      fileList.clear();
      dirList.clear();
    }
  }


  public void reset()
  {
    recursiveCount = 0;
    thisDirList = new ArrayList<>();
    thisFileList = new ArrayList<>();
  }

  /**
   * @return
   */
  public List<String> getFileList()
  {
    return this.thisFileList;
  }

  /**
   * @return
   */
  public List<String> getDirectoryList()
  {
    return this.thisDirList;
  }
}
