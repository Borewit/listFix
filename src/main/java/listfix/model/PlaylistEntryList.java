package listfix.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import listfix.model.playlists.PlaylistEntry;

/** Needed to support DnD. */
public class PlaylistEntryList implements Transferable {
  private final List<PlaylistEntry> _list;

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {new DataFlavor(PlaylistEntryList.class, "PlaylistEntryList")};
  }

  @Override
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor.equals(new DataFlavor(PlaylistEntryList.class, "PlaylistEntryList"));
  }

  @Override
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    return this;
  }

  public PlaylistEntryList(List<PlaylistEntry> list) {
    _list = list;
  }

  public List<PlaylistEntry> getList() {
    return _list;
  }
}
