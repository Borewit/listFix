package listfix.model;

public class CustomTableModel extends javax.swing.table.AbstractTableModel 
{    
        private final String[] columnNames = { "File Name", "Status", "Location" };
        private Object[][] data;
        private final boolean[] canEdit = new boolean [] { false, false, false };
        private final Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

        public CustomTableModel(Object[][] input)
        {
            data = input;
        }
        
        public CustomTableModel()
        {
            Object[][] result = new Object[0][3];
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
        
        public Object[] longestValues()
        {
            String[] result = new String[3];
            if (data.length > 0)
            {
                for (int i = 0; i < data.length; i++)
                {
                    for (int j = 0; j < data[i].length; j++)
                    {
                        if (result[j] == null || (result[j].length() < ((String) data[i][j]).length()))
                        {
                            result[j] = (String) data[i][j];
                        }
                    }
                }
            }
            else
            {
                result[0] = "";
                result[1] = "";
                result[2] = "";
            }
            return result;
        }
}