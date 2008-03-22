package listfix.model;

import java.util.Vector;

public class MatchedFileTableModel extends javax.swing.table.AbstractTableModel 
{    
        private final String [] columnNames = { "File Name", "# of keyword matches" };
        private Object[][] data;
        private final boolean[] canEdit = new boolean [] { false, false };
        private final Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

        public MatchedFileTableModel(Object[][] input)
        {
            data = input;
        }
        
        public MatchedFileTableModel(Vector input)
        {
            int n = input.size();
            String [][] tempData = new String[n][2];
            for (int i = 0; i < n; i++)
            {
                tempData[i][0] = ((MatchedPlaylistEntry)input.elementAt(i)).getPlaylistFile().getFileName();
                tempData[i][1] = ((MatchedPlaylistEntry)input.elementAt(i)).getCount() + "";
            }
            data = tempData;
        }
        
        public MatchedFileTableModel()
        {
            Object[][] result = new Object[50][2];
            for (int i = 1; i < 50; i++)
            {
                for (int j = 1; j < 3; j++)
                {
                    result[i][j] = null;
                }
            }
            data = result;
        }
        
        public void updateData(Object[][] input)
        {
            data = input;
            this.fireTableDataChanged();
        }
        
        public int getColumnCount() {
            return columnNames.length;
        }
        
        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }
        
        public java.lang.Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return types[c];
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int rowIndex, int columnIndex) 
        { 
            return canEdit [columnIndex]; 
        }
}