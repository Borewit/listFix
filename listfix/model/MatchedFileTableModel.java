/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2008 Jeremy Caron
 * 
 * This file is part of listFix().
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, please see http://www.gnu.org/licenses/
 */

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