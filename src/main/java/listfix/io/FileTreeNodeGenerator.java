package listfix.io;

import listfix.comparators.DirectoryThenFileThenAlphabeticalPathComparator;
import listfix.model.playlists.Playlist;
import listfix.view.support.PlaylistTreeNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

public class FileTreeNodeGenerator
{
  private static final Logger logger = LogManager.getLogger(FileTreeNodeGenerator.class);

  public static PlaylistTreeNode addNodes(PlaylistTreeNode curTop, Collection<Path> playlistDirectories)
  {
    return FileTreeNodeGenerator.addNodes(curTop, playlistDirectories.stream());
  }

  /**
   * Add nodes from under "playlistDirectories" into curTop. Highly recursive.
   *
   * @param curTop Tree node, to which the provided files will be added as children. Maybe Null.
   * @param files  Playlist directories or playlist files to add
   * @return Playlist tree
   */
  public static PlaylistTreeNode addNodes(PlaylistTreeNode curTop, Stream<Path> files)
  {
    PlaylistTreeNode currentNode = curTop == null ? new PlaylistTreeNode(Path.of("#___root___")) : curTop;
    files
      .filter(file -> Files.isDirectory(file) || Playlist.isPlaylist(file))
      .sorted(new DirectoryThenFileThenAlphabeticalPathComparator())
      .map(PlaylistTreeNode::new)
      .forEach(node -> {
        Path path = node.getUserObject();
        if (Files.isDirectory(path))
        {
          try
          {
            try (Stream<Path> children = Files.list(path))
            {
              addNodes(node, children); // recursion
            }
          }
          catch (IOException e)
          {
            logger.error(String.format("Failed to list files in directory: %s", path), e);
            throw new RuntimeException(e);
          }
        }
        currentNode.add(node);
      });
    return currentNode;
  }


  public static Path treePathToFileSystemPath(TreePath node)
  {
    return ((PlaylistTreeNode) node.getLastPathComponent()).getUserObject();
  }
}
