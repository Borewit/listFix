package listfix.view.controls;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;
import listfix.view.support.ImageIcons;

class PlaylistTableModel extends AbstractTableModel {
  private Playlist playlist;

  PlaylistTableModel() {
    this.playlist = null;
  }

  public void changePlaylist(Playlist playlist) {
    this.playlist = playlist;
    this.fireTableDataChanged();
  }

  @Override
  public int getRowCount() {
    if (playlist != null) {
      return playlist.size();
    } else {
      return 0;
    }
  }

  @Override
  public int getColumnCount() {
    return 4;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    PlaylistEntry entry = playlist.get(rowIndex);
    switch (columnIndex) {
      case 0 -> {
        return rowIndex + 1;
      }
      case 1 -> {
        if (entry.isURL()) {
          return ImageIcons.IMG_URL;
        } else if (entry.isFixed()) {
          return ImageIcons.IMG_FIXED;
        } else if (entry.isFound()) {
          return ImageIcons.IMG_FOUND;
        } else {
          return ImageIcons.IMG_MISSING;
        }
      }
      case 2 -> {
        return entry.getTrackFileName();
      }
      case 3 -> {
        return entry.getTrackFolder();
      }
      default -> {
        return null;
      }
    }
  }

  @Override
  public String getColumnName(int column) {
    return switch (column) {
      case 0 -> "#";
      case 1 -> "";
      case 2 -> "File Name";
      case 3 -> "Location";
      default -> null;
    };
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0 -> Integer.class;
      case 1 -> ImageIcon.class;
      default -> Object.class;
    };
  }
}
