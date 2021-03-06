
/**
 * listFix() - Fix Broken Playlists!
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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import listfix.io.writers.FileCopier;
import listfix.model.BatchRepair;
import listfix.model.BatchRepairItem;
import listfix.model.playlists.winamp.generated.Playlist;
import listfix.model.playlists.winamp.generated.Playlists;
import listfix.util.ExStack;
import listfix.util.OperatingSystem;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;

import org.apache.log4j.Logger;

/**
 * Provides convenience methods for interacting w/ the Winamp Media Library and determining if Winamp is installed.
 * @author jcaron
 */
public class WinampHelper
{
  private static final String HOME_PATH = System.getenv("APPDATA");
  private static final String WINAMP_PATH1 = HOME_PATH + "\\Winamp\\Plugins\\ml\\playlists\\";
  private static final String WINAMP_PATH2 = HOME_PATH + "\\Winamp\\Plugins\\ml\\";
  private static final Logger _logger = Logger.getLogger(WinampHelper.class);

  private static String WINAMP_PATH = "";

  static
  {
    File tester = new File(WINAMP_PATH1);
    if (tester.exists())
    {
      WINAMP_PATH = WINAMP_PATH1;
    }
    else
    {
      tester = new File(WINAMP_PATH2);
      if (tester.exists())
      {
        WINAMP_PATH = WINAMP_PATH2;
      }
    }
  }

  /**
   * Generates an exact match batch repair for the cryptically named playlists in Winamp.
   * @param mediaFiles
   * @return A BatchRepair instance
   * @see BatchRepair
   */
  public static BatchRepair getWinampBatchRepair(String[] mediaFiles)
  {
    try
    {
      final BatchRepair br = new BatchRepair(mediaFiles, new File(WINAMP_PATH));
      br.setDescription("Batch Repair: Winamp Playlists");
      List<listfix.model.playlists.winamp.generated.Playlist> winLists = getWinampPlaylists();
      for (listfix.model.playlists.winamp.generated.Playlist list : winLists)
      {
        br.add(new BatchRepairItem(new File(WINAMP_PATH + list.getFilename())));
      }
      return br;
    }
    catch (JAXBException ex)
    {
      _logger.error(ExStack.toString(ex));
      return null;
    }
  }

  /**
   *
   * @param destDir
   * @param observer
   * @throws JAXBException
   * @throws IOException
   */
  public static void extractPlaylists(File destDir, IProgressObserver observer) throws JAXBException, IOException
  {
    // avoid resetting total if part of batch operation
    boolean hasTotal = observer instanceof ProgressAdapter;
    ProgressAdapter progress = ProgressAdapter.wrap(observer);

    if (!destDir.exists())
    {
      destDir.mkdir();
    }

    List<Playlist> winLists = getWinampPlaylists();
    if (!hasTotal)
    {
      progress.setTotal(winLists.size());
    }
    for (Playlist list : winLists)
    {
      FileCopier.copy(new File(WINAMP_PATH + list.getFilename()),
        new File(destDir.getPath() + System.getProperty("file.separator") + FileUtils.replaceInvalidWindowsFileSystemCharsWithChar(list.getTitle(), '_') + ".m3u8"));
      progress.stepCompleted();
    }
  }

  /**
   *
   * @return
   */
  public static boolean isWinampInstalled()
  {
    return OperatingSystem.isWindows() && !WINAMP_PATH.isEmpty();
  }

  private static List<listfix.model.playlists.winamp.generated.Playlist> getWinampPlaylists() throws JAXBException
  {
    String playlistPath = WINAMP_PATH + "playlists.xml";
    File listsFile = new File(playlistPath);
    if (!listsFile.canRead())
    {
      return null;
    }

    JAXBContext context = JAXBContext.newInstance("listfix.model.playlists.winamp.generated");
    Unmarshaller unmarshaller = context.createUnmarshaller();
    Playlists lists = (Playlists) unmarshaller.unmarshal(listsFile);
    return lists.getPlaylist();
  }
}
