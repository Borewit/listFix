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

public class ZebraJTable extends javax.swing.JTable
{
	private java.awt.Color rowColors[] = new java.awt.Color[2];
	private boolean drawStripes = false;

	public ZebraJTable()
	{
	}

	public ZebraJTable(int numRows, int numColumns)
	{
		super(numRows, numColumns);
	}

	public ZebraJTable(Object[][] rowData, Object[] columnNames)
	{
		super(rowData, columnNames);
	}

	public ZebraJTable(javax.swing.table.TableModel dataModel)
	{
		super(dataModel);
	}

	public ZebraJTable(javax.swing.table.TableModel dataModel, javax.swing.table.TableColumnModel columnModel)
	{
		super(dataModel, columnModel);
	}

	public ZebraJTable(javax.swing.table.TableModel dataModel, javax.swing.table.TableColumnModel columnModel, javax.swing.ListSelectionModel selectionModel)
	{
		super(dataModel, columnModel, selectionModel);
	}

	public ZebraJTable(java.util.Vector<?> rowData, java.util.Vector<?> columnNames)
	{
		super(rowData, columnNames);
	}

	/** Add stripes between cells and behind non-opaque cells. */
	@Override
	public void paintComponent(java.awt.Graphics g)
	{
		if (!(drawStripes = isOpaque()))
		{
			super.paintComponent(g);
			return;
		}

		// Paint zebra background stripes
		updateZebraColors();
		final java.awt.Insets insets = getInsets();
		final int w = getWidth() - insets.left - insets.right;
		final int h = getHeight() - insets.top - insets.bottom;
		final int x = insets.left;
		int y = insets.top;
		int rowHeight = 16; // A default for empty tables
		final int nItems = getRowCount();
		for (int i = 0; i < nItems; i++, y += rowHeight)
		{
			rowHeight = getRowHeight(i);
			g.setColor(rowColors[i & 1]);
			g.fillRect(x, y, w, rowHeight);
		}
		// Use last row height for remainder of table area
		final int nRows = nItems + (insets.top + h - y) / rowHeight;
		for (int i = nItems; i < nRows; i++, y += rowHeight)
		{
			g.setColor(rowColors[i & 1]);
			g.fillRect(x, y, w, rowHeight);
		}
		final int remainder = insets.top + h - y;
		if (remainder > 0)
		{
			g.setColor(rowColors[nRows & 1]);
			g.fillRect(x, y, w, remainder);
		}

		// Paint component
		setOpaque(false);
		super.paintComponent(g);
		setOpaque(true);
	}

	/** Add background stripes behind rendered cells. */
	@Override
	public java.awt.Component prepareRenderer(
		javax.swing.table.TableCellRenderer renderer, int row, int col)
	{
		final java.awt.Component c = super.prepareRenderer(renderer, row, col);
		if (drawStripes && !isCellSelected(row, col))
		{
			c.setBackground(rowColors[row & 1]);
		}
		return c;
	}

	/** Add background stripes behind edited cells. */
	@Override
	public java.awt.Component prepareEditor(
		javax.swing.table.TableCellEditor editor, int row, int col)
	{
		final java.awt.Component c = super.prepareEditor(editor, row, col);
		if (drawStripes && !isCellSelected(row, col))
		{
			c.setBackground(rowColors[row & 1]);
		}
		return c;
	}

	/** Force the table to fill the viewport's height. */
	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		final java.awt.Component p = getParent();
		if (!(p instanceof javax.swing.JViewport))
		{
			return false;
		}
		return ((javax.swing.JViewport) p).getHeight() > getPreferredSize().height;
	}

	/** Compute zebra background stripe colors. */
	private void updateZebraColors()
	{
		if ((rowColors[0] = getBackground()) == null)
		{
			rowColors[0] = rowColors[1] = java.awt.Color.white;
			return;
		}
		final java.awt.Color sel = getSelectionBackground();
		if (sel == null)
		{
			rowColors[1] = rowColors[0];
			return;
		}
		rowColors[1] = new java.awt.Color(240, 240, 240);
	}
}
