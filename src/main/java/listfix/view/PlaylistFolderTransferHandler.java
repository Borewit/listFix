package listfix.view;

import listfix.io.FileTreeNodeGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used to drag playlist from playlists-directories pane to editor
 */
public class PlaylistFolderTransferHandler extends TransferHandler
{
  private final IListFixGui listFixGui;
  private final JTree playlistDirectoryTree;

  private final Logger _logger = LogManager.getLogger(PlaylistFolderTransferHandler.class);

  public PlaylistFolderTransferHandler(JTree playlistDirectoryTree, IListFixGui listFixGui)
  {
    this.playlistDirectoryTree = playlistDirectoryTree;
    this.listFixGui = listFixGui;
  }

  @Override
  public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
  {
    return Arrays.stream(transferFlavors).anyMatch(DataFlavor.javaFileListFlavor::equals);
  }

  /**
   * Causes a transfer to occur from a clipboard or a drag and drop operation.
   *
   * @param support the object containing the details of the transfer, not <code>null</code>.
   * @return true if the data was inserted into the component, false otherwise
   */
  @Override
  public boolean importData(TransferHandler.TransferSupport support)
  {
    final Transferable transferable = support.getTransferable();
    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
    {
      try
      {
        List<File> fileList = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
        return fileList.stream()
          .filter(File::isDirectory)
          .map(folder -> {
            this.listFixGui.addPlaylistFolder(folder);
            return true;
          })
          .reduce(false, (t, v) -> true);
      }
      catch (Exception e)
      {
        _logger.error("Dragging onto playlists directory failed", e);
      }
    }
    return false;
  }

  @Override
  public int getSourceActions(JComponent c)
  {
    return MOVE;
  }

  @Override
  protected Transferable createTransferable(JComponent c)
  {
    if (c instanceof JTree)
    {
      int[] rows = playlistDirectoryTree.getSelectionRows();
      if (rows == null)
        return null;
      List<File> fileList = Arrays.stream(rows).mapToObj(selRow -> {
        TreePath selPath = playlistDirectoryTree.getPathForRow(selRow);
        return FileTreeNodeGenerator.treePathToFileSystemPath(selPath).toFile();
      }).collect(Collectors.toList());

      return new FileListTransferable(fileList);
    }
    _logger.warn(String.format("JComponent type not supported for transfer %s", c.getClass().getName()));
    return null;
  }
}
