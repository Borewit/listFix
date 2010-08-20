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

package listfix.view.support;

import java.awt.event.MouseEvent;
import java.util.Collections;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import listfix.comparators.MatchedPlaylistEntryColumnSorterComparator;
import listfix.model.MatchedFileTableModel;

public class ClosestMatchColumnListener extends java.awt.event.MouseAdapter
{
	private JTable table;
	private MatchedFileTableModel tableModel;

	public ClosestMatchColumnListener(JTable t, MatchedFileTableModel tm)
	{
		table = t;
		tableModel = tm;
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		TableColumnModel colModel = table.getColumnModel();
		int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
		int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

		if (modelIndex < 0)
		{
			return;
		}
		if (tableModel.sortCol == modelIndex)
		{
			tableModel.isSortAsc = !tableModel.isSortAsc;
		}
		else
		{
			tableModel.sortCol = modelIndex;
		}

		for (int i = 0; i < 2; i++)
		{
			TableColumn column = colModel.getColumn(i);
			column.setHeaderValue(tableModel.getColumnName(column.getModelIndex()));
		}
		table.getTableHeader().repaint();

		Collections.sort(tableModel.vectorData, new MatchedPlaylistEntryColumnSorterComparator(tableModel.isSortAsc, tableModel.sortCol));
		tableModel.updateData(tableModel.vectorData);

		table.tableChanged(new TableModelEvent(table.getModel()));
		table.repaint();
	}
}
