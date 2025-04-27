package listfix.model;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import listfix.model.playlists.PotentialPlaylistEntryMatch;

public class MatchedFileTableModel extends AbstractTableModel {
  private static final long serialVersionUID = -1888652455638101278L;
  private final String[] columnNames = {"File Name", "Score"};
  private Object[][] data;
  private final boolean[] canEdit = new boolean[] {false, false};
  private final Class<?>[] types = new Class<?>[] {String.class, String.class};

  private final Integer sortCol = 1;

  private final Boolean isSortAsc = Boolean.FALSE;

  private List<PotentialPlaylistEntryMatch> vectorData;

  public MatchedFileTableModel(List<PotentialPlaylistEntryMatch> input) {
    vectorData = input;
    updateData(vectorData);
  }

  public final void updateData(List<PotentialPlaylistEntryMatch> input) {
    int n = input.size();
    String[][] tempData = new String[n][2];
    for (int i = 0; i < n; i++) {
      PotentialPlaylistEntryMatch entry = input.get(i);
      tempData[i][0] = entry.getTrack().getFileName().toString();
      tempData[i][1] = entry.getScore() + "";
    }
    data = tempData;
    vectorData = input;
    this.fireTableDataChanged();
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public int getRowCount() {
    return data.length;
  }

  @Override
  public String getColumnName(int col) {
    String suffix = "";
    if (col == sortCol) {
      suffix += isSortAsc ? " >>" : " <<";
    }
    return columnNames[col] + suffix;
  }

  @Override
  public Object getValueAt(int row, int col) {
    return data[row][col];
  }

  @Override
  public Class<?> getColumnClass(int c) {
    return types[c];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return canEdit[columnIndex];
  }

  public Object[] longestValues() {
    String[] result = new String[2];
    if (data.length > 0) {
      for (Object[] data1 : data) {
        for (int j = 0; j < data1.length; j++) {
          if (result[j] == null || (result[j].length() < ((String) data1[j]).length())) {
            result[j] = (String) data1[j];
          }
        }
      }
    } else {
      result[0] = "";
      result[1] = "";
    }
    return result;
  }
}
