package listfix.view;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public abstract class FileListTransferHandler extends TransferHandler
{

  private final Logger _logger = LogManager.getLogger(FileListTransferHandler.class);

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
  public boolean importData(TransferSupport support)
  {
    final Transferable transferable = support.getTransferable();
    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
    {
      try
      {
        handleFileList((List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor));
      }
      catch (Exception e)
      {
        _logger.error("Transfer Java-file-list failed", e);
      }
    }
    return false;
  }

  /**
   * Called when a Java-File-List is transferred
   * @param fileList Transferred Java-File-List
   */
  public abstract boolean handleFileList(List<File> fileList);
}
