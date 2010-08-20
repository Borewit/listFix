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

import java.util.Vector;

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
	public Integer sortCol = new Integer(1);
	public Boolean isSortAsc = Boolean.FALSE;
	public Vector<MatchedPlaylistEntry> vectorData = new Vector<MatchedPlaylistEntry>();

	public MatchedFileTableModel(Vector<MatchedPlaylistEntry> input)
	{
		vectorData = input;
		updateData(vectorData);
	}

	public void updateData(Vector<MatchedPlaylistEntry> input)
	{
		int n = input.size();
		String[][] tempData = new String[n][2];
		for (int i = 0; i < n; i++)
		{
			tempData[i][0] = input.elementAt(i).getPlaylistFile().getFileName();
			tempData[i][1] = input.elementAt(i).getScore() + "";
		}
		data = tempData;
		vectorData = input;
		this.fireTableDataChanged();
	}

	public int getColumnCount()
	{
		return columnNames.length;
	}

	public int getRowCount()
	{
		return data.length;
	}

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
		String[] result = new String[2];
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
		}
		return result;
	}
}
