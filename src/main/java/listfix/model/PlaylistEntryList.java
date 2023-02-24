

package listfix.model;

import listfix.model.playlists.PlaylistEntry;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

/**
 *
 * Needed to support DnD.
 */
public class PlaylistEntryList implements Transferable
{
  private final List<PlaylistEntry> _list;

  /**
   *
   * @return
   */
  @Override
  public DataFlavor[] getTransferDataFlavors()
  {
    return new DataFlavor[]
      {
        new DataFlavor(PlaylistEntryList.class, "PlaylistEntryList")
      };
  }

  /**
   *
   * @param flavor
   * @return
   */
  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor)
  {
    return flavor.equals(new DataFlavor(PlaylistEntryList.class, "PlaylistEntryList"));
  }

  /**
   *
   * @param flavor
   * @return
   * @throws UnsupportedFlavorException
   * @throws IOException
   */
  @Override
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
  {
    return this;
  }

  /**
   *
   * @param list
   */
  public PlaylistEntryList(List<PlaylistEntry> list)
  {
    _list = list;
  }

  /**
   *
   * @return
   */
  public List<PlaylistEntry> getList()
  {
    return _list;
  }
}
