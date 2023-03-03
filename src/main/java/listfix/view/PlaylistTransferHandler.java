package listfix.view;

import listfix.io.datatransfer.PlaylistTransferObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.activation.DataHandler;
import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class PlaylistTransferHandler extends TransferHandler
{
  private static final Logger logger = LogManager.getLogger(PlaylistTransferHandler.class);
  private final IListFixGui listFixGui;

  public PlaylistTransferHandler(IListFixGui listFixGui)
  {
    this.listFixGui = listFixGui;
  }

  @Override
  public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
  {
    return Arrays.stream(transferFlavors).anyMatch(DataFlavor.javaFileListFlavor::equals);
  }

  @Override
  public boolean importData(TransferHandler.TransferSupport support)
  {
    final Transferable transferable = support.getTransferable();
    try
    {
      if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
      {
        try
        {
          List<File> fileList = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
          this.listFixGui.openFileListDrop(fileList);
          return true;
        }
        catch (UnsupportedFlavorException e)
        {
          throw new RuntimeException(e);
        }
      }
      List<String> mediaList = extractPlaylistsTransfer(transferable);
      // ToDo: Add M3U List
    }
    catch (IOException e)
    {
      logger.warn("Failed process dropped file list", e);
    }
    return false;
  }

  public static List<String> extractPlaylistsTransfer(Transferable transferable) throws IOException
  {
    try
    {
      // Try to extract M3U
      if (transferable.isDataFlavorSupported(PlaylistTransferObject.M3uPlaylistDataFlavor))
      {
        try (InputStream m3uInputStream = (InputStream) transferable.getTransferData(PlaylistTransferObject.M3uPlaylistDataFlavor))
        {
          return PlaylistTransferObject.deserialize(m3uInputStream);
        }
      }

      // Try to extract Unicode Plain Text
      if (transferable.isDataFlavorSupported(DataFlavor.getTextPlainUnicodeFlavor()))
      {
        try (InputStream m3uInputStream = (InputStream) transferable.getTransferData(DataFlavor.getTextPlainUnicodeFlavor()))
        {
          logger.warn("Received plain text, trying to read it as M3U");
          return PlaylistTransferObject.deserialize(m3uInputStream);
        }
      }
    }
    catch (UnsupportedFlavorException ufe)
    {
      // Should not happen
      logger.warn("Trying to extract M3U flavor transfer failed", ufe);
    }
    // No results
    return Collections.emptyList();
  }

  public static Transferable toTransferableFromPaths(Collection<Path> droppedFiles) {
    return toTransferableFromFiles(droppedFiles.stream().map(Path::toFile).collect(Collectors.toList()));
  }

  public static Transferable toTransferableFromFiles(Collection<File> droppedFiles) {
    List<File> immutable = new ArrayList<>(droppedFiles);
    return new DataHandler(immutable, DataFlavor.javaJVMLocalObjectMimeType);
  }

}
