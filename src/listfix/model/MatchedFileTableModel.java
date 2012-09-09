/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2012 Jeremy Caron
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

/**
 *
 * @author jcaron
 */
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
	/**
	 *
	 */
	public Integer sortCol = new Integer(1);
	/**
	 *
	 */
	public Boolean isSortAsc = Boolean.FALSE;
	/**
	 *
	 */
	public List<PotentialPlaylistEntryMatch> vectorData;

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
	public void updateData(List<PotentialPlaylistEntryMatch> input)
	{
		int n = input.size();
		String[][] tempData = new String[n][2];
		for (int i = 0; i < n; i++)
		{
            PotentialPlaylistEntryMatch entry = input.get(i);
			tempData[i][0] = entry.getPlaylistFile().getFileName();
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
	public int getColumnCount()
	{
		return columnNames.length;
	}

	/**
	 *
	 * @return
	 */
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
