/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2014 Jeremy Caron
 *
 * This file is part of listFix().
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, please see http://www.gnu.org/licenses/
 */

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

  /**
   *
   */
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
