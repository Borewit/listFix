package listfix.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Converts a List of Files into a <code>java.awt.datatransfer.Transferable</code> of flavour <code>application/x-java-file-list;class=java.util.List</code>
 * @author Borewit
 * @see DataFlavor#javaFileListFlavor
 */
public class FileListTransferable implements Transferable
{
  private List<File> fileList;
  private DataFlavor[] dataFlavors;

  public FileListTransferable(List<File> fileList)
  {
    this.fileList = Collections.unmodifiableList(fileList);
    this.dataFlavors = new DataFlavor[] {DataFlavor.javaFileListFlavor};
  }

  @Override
  public DataFlavor[] getTransferDataFlavors()
  {
    return dataFlavors;
  }

  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor)
  {
    return Arrays.stream(this.dataFlavors).anyMatch(f -> f.equals(flavor));
  }

  @Override
  public List<File> getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
  {
    return this.fileList;
  }
}
