package listfix.view.dialogs;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import listfix.model.BatchRepair;
import listfix.model.BatchRepairItem;
import listfix.model.playlists.Playlist;
import listfix.view.support.ImageIcons;

public class PlaylistsTableModel extends AbstractTableModel {

  public PlaylistsTableModel(BatchRepair items) {
    _items = items;
  }

  @Override
  public int getRowCount() {
    return _items == null ? 0 : _items.getItems().size();
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public String getColumnName(int column) {
    return switch (column) {
      case 0 -> "";
      case 1 -> "Name";
      default -> null;
    };
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    if (columnIndex == 0) {
      return ImageIcon.class;
    } else {
      return Object.class;
    }
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (_items == null || _items.isEmpty()) {
      return null;
    } else {
      BatchRepairItem item = _items.getItem(rowIndex);
      Playlist list = item.getPlaylist();
      switch (columnIndex) {
        case 0 -> {
          if (list != null) {
            if (list.getMissingCount() > 0) {
              return ImageIcons.IMG_MISSING;
            } else if (list.getFixedCount() > 0) {
              return ImageIcons.IMG_FIXED;
            }
          }
          return ImageIcons.IMG_FOUND;
        }
        case 1 -> {
          return item.getDisplayName();
        }
        default -> {
          return null;
        }
      }
    }
  }

  private final BatchRepair _items;
}
