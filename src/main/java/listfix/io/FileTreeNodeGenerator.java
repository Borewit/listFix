package listfix.io;

import listfix.comparators.DirectoryThenFileThenAlphabeticalFileComparator;
import listfix.io.filters.PlaylistFileFilter;
import listfix.view.support.PlaylistTreeNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.tree.TreePath;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;

public class FileTreeNodeGenerator
{
  private static final Logger logger = LogManager.getLogger(FileTreeNodeGenerator.class);

  public static PlaylistTreeNode addNodes(PlaylistTreeNode curTop, Collection<File> playlistDirectories)
  {
    return FileTreeNodeGenerator.addNodes(curTop, playlistDirectories.toArray(new File[]{}));
  }

  /**
   * Add nodes from under "playlistDirectories" into curTop. Highly recursive.
   *
   * @param curTop Tree node, to which the provided files will be added as children. Maybe Null.
   * @param files  Playlist directories or playlist files to add
   * @return Playlist tree
   */
  public static PlaylistTreeNode addNodes(PlaylistTreeNode curTop, File[] files)
  {
    curTop = curTop == null ? new PlaylistTreeNode(new File("#___root___")) : curTop;
    Arrays.sort(files, new DirectoryThenFileThenAlphabeticalFileComparator());
    for (File playlistDir : files)
    {
      if (!playlistDir.exists())
      {
        logger.warn(String.format("Directory does not exist: %s", playlistDir.getName()));
        continue;
      }

      PlaylistTreeNode curDir = new PlaylistTreeNode(playlistDir);
      curTop.add(curDir);

      final File[] inodes = playlistDir.listFiles(new PlaylistFileFilter());
      if (inodes != null && inodes.length > 0)
      {
        addNodes(curDir, inodes);
      }
    }
    return curTop;
  }

  public static File TreePathToFileSystemPath(TreePath node)
  {
    return ((PlaylistTreeNode) node.getLastPathComponent()).getUserObject();
  }
}
