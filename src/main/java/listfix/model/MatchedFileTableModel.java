

package listfix.model;

import listfix.model.playlists.PotentialPlaylistEntryMatch;

import java.util.List;


public class MatchedFileTableModel extends javax.swing.table.AbstractTableModel
{
  private static final long serialVersionUID = -1888652455638101278L;
  private final String[] columnNames =
  {
    "File Name", "Score"
  };
  private Object[][] data;
  private final boolean[] canEdit = new boolean[]
  {
    false, false
  };
  private final Class<?>[] types = new Class<?>[]
  {
    java.lang.String.class, java.lang.String.class
  };

  private Integer sortCol = 1;

  private Boolean isSortAsc = Boolean.FALSE;

  private List<PotentialPlaylistEntryMatch> vectorData;

  /**
   *
   * @param input
   */
  public MatchedFileTableModel(List<PotentialPlaylistEntryMatch> input)
  {
    vectorData = input;
    updateData(vectorData);
  }

  /**
   *
   * @param input
   */
  public final void updateData(List<PotentialPlaylistEntryMatch> input)
  {
    int n = input.size();
    String[][] tempData = new String[n][2];
    for (int i = 0; i < n; i++)
    {
            PotentialPlaylistEntryMatch entry = input.get(i);
      tempData[i][0] = entry.getPlaylistFile().getTrackFileName();
      tempData[i][1] = entry.getScore() + "";
    }
    data = tempData;
    vectorData = input;
    this.fireTableDataChanged();
  }

  /**
   *
   * @return
   */
  @Override
  public int getColumnCount()
  {
    return columnNames.length;
  }

  /**
   *
   * @return
   */
  @Override
  public int getRowCount()
  {
    return data.length;
  }

  /**
   *
   * @param col
   * @return
   */
  @Override
  public String getColumnName(int col)
  {
    String suffix = "";
    if (col == sortCol)
    {
      suffix += isSortAsc ? " >>" : " <<";
    }
    return columnNames[col] + suffix;
  }

  /**
   *
   * @param row
   * @param col
   * @return
   */
  @Override
  public java.lang.Object getValueAt(int row, int col)
  {
    return data[row][col];
  }

  /**
   *
   * @param c
   * @return
   */
  @Override
  public Class<?> getColumnClass(int c)
  {
    return types[c];
  }

  /**
   *
   * @param rowIndex
   * @param columnIndex
   * @return
   */
  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return canEdit[columnIndex];
  }

  /**
   *
   * @return
   */
  public Object[] longestValues()
  {
    String[] result = new String[2];
    if (data.length > 0)
    {
      for (Object[] data1 : data)
      {
        for (int j = 0; j < data1.length; j++)
        {
          if (result[j] == null || (result[j].length() < ((String) data1[j]).length()))
          {
            result[j] = (String) data1[j];
          }
        }
      }
    }
    else
    {
      result[0] = "";
      result[1] = "";
    }
    return result;
  }
}
