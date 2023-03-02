
package listfix.io;

import listfix.config.IMediaLibrary;
import listfix.model.BatchRepair;
import listfix.model.BatchRepairItem;
import listfix.model.playlists.winamp.generated.Playlist;
import listfix.model.playlists.winamp.generated.Playlists;
import listfix.util.OperatingSystem;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

/**
 * Provides convenience methods for interacting w/ the Winamp Media Library and determining if Winamp is installed.
 *
 * @author jcaron
 */
public class WinampHelper
{
  private static final String HOME_PATH = System.getenv("APPDATA");
  private static final String WINAMP_PATH1 = HOME_PATH + "\\Winamp\\Plugins\\ml\\playlists\\";
  private static final String WINAMP_PATH2 = HOME_PATH + "\\Winamp\\Plugins\\ml\\";
  private static final Logger _logger = LogManager.getLogger(WinampHelper.class);

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
   *
   * @param mediaLibrary Media-library
   * @return A BatchRepair instance
   * @see BatchRepair
   */
  public static BatchRepair getWinampBatchRepair(IMediaLibrary mediaLibrary, IPlaylistOptions filePathOptions)
  {
    try
    {
      final BatchRepair br = new BatchRepair(mediaLibrary, new File(WINAMP_PATH));
      br.setDescription("Batch Repair: Winamp Playlists");
      List<Playlist> winLists = getWinampPlaylists();
      for (Playlist list : winLists)
      {
        br.add(new BatchRepairItem(new File(WINAMP_PATH + list.getFilename()), filePathOptions));
      }
      return br;
    }
    catch (JAXBException ex)
    {
      _logger.error("Error while repairing Winamp playlist", ex);
      return null;
    }
  }

  public static void extractPlaylists(File destDir, IProgressObserver<Void> observer) throws JAXBException, IOException
  {
    // avoid resetting total if part of batch operation
    ProgressAdapter<Void> progress = ProgressAdapter.make(observer);

    if (!destDir.exists())
    {
      if (!destDir.mkdir())
      {
        throw new IOException(String.format("Failed to create directory \"%s\"", destDir));
      }
    }

    List<Playlist> winLists = getWinampPlaylists();
    progress.setTotal(winLists.size());
    for (Playlist list : winLists)
    {
      Path sourceFile = Path.of(WINAMP_PATH, list.getFilename());
      Path targetPath = Path.of(destDir.getPath(), FileUtils.replaceInvalidWindowsFileSystemCharsWithChar(list.getTitle(), '_') + ".m3u8");
      Files.copy(sourceFile, targetPath, StandardCopyOption.REPLACE_EXISTING);

      progress.stepCompleted();
    }
  }

  public static boolean isWinampInstalled()
  {
    return OperatingSystem.isWindows() && !WINAMP_PATH.isEmpty();
  }

  private static List<Playlist> getWinampPlaylists() throws JAXBException
  {
    String playlistPath = WINAMP_PATH + "playlists.xml";
    File listsFile = new File(playlistPath);
    if (listsFile.canRead())
    {
      JAXBContext context = JAXBContext.newInstance("listfix.model.playlists.winamp.generated");
      Unmarshaller unmarshaller = context.createUnmarshaller();
      Playlists lists = (Playlists) unmarshaller.unmarshal(listsFile);
      return lists.getPlaylist();
    }
    return Collections.emptyList();
  }
}
