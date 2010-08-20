/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2009 Jeremy Caron
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

import java.util.List;

public class RepairedPlaylistResultTableModel extends javax.swing.table.AbstractTableModel
{
	private final String[] columnNames =
	{
		"File Name", "Total Entries", "Missing Entries Before", "Missing Entries After", "Updated Entries", "Saved"
	};
	private Object[][] data;
	private final boolean[] canEdit = new boolean[]
	{
		false, false, false, false, false, false
	};
	private final Class<?>[] types = new Class<?>[]
	{
		java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
	};
	public Integer sortCol = new Integer(1);
	public Boolean isSortAsc = Boolean.FALSE;
	public List<RepairedPlaylistResult> vectorData = null;

	public RepairedPlaylistResultTableModel(List<RepairedPlaylistResult> input)
	{
		vectorData = input;
		updateData(vectorData);
	}

	public RepairedPlaylistResultTableModel()
	{
		Object[][] result = new Object[50][6];
		for (int i = 0; i < 50; i++)
		{
			for (int j = 0; j < 6; j++)
			{
				result[i][j] = null;
			}
		}
		data = result;
	}

	public void updateData(List<RepairedPlaylistResult> input)
	{
		int n = input.size();
		String[][] tempData = new String[n][6];
		for (int i = 0; i < n; i++)
		{
			tempData[i][0] = input.get(i).getPlaylist().getFilename();
			tempData[i][1] = input.get(i).getPlaylist().getEntryCount() + "";
			tempData[i][2] = input.get(i).getStartLostCount() + "";
			tempData[i][3] = input.get(i).getEndLostCount() + "";
			tempData[i][4] = (input.get(i).getEndFoundCount() - input.get(i).getStartFoundCount()) + "";
			tempData[i][5] = input.get(i).isWrittenSuccessfully() ? "YES" : "ERROR";

		}
		data = tempData;
		this.fireTableDataChanged();
	}

	public void updateData(Object[][] input)
	{
		data = input;
		this.fireTableDataChanged();
	}

	@Override
	public int getColumnCount()
	{
		return columnNames.length;
	}

	@Override
	public int getRowCount()
	{
		return data.length;
	}

	@Override
	public String getColumnName(int col)
	{
//        String suffix = "";
//        if (col == sortCol)
//        {
//            suffix += isSortAsc ? " >>" : " <<";
//        }
		return columnNames[col]; // + suffix;
	}

	@Override
	public java.lang.Object getValueAt(int row, int col)
	{
		return data[row][col];
	}

	@Override
	public Class<?> getColumnClass(int c)
	{
		return types[c];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return canEdit[columnIndex];
	}

	public Object[] longestValues()
	{
		String[] result = new String[6];
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
			result[3] = "";
			result[4] = "";
			result[5] = "";
		}
		return result;
	}
}
