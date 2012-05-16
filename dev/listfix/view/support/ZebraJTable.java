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

package listfix.view.support;

import java.awt.Component;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

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

//	public ZebraJTable(java.util.Vector<?> rowData, java.util.Vector<?> columnNames)
//	{
//		super(rowData, columnNames);
//	}

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
		int tempRowHeight = 16; // A default for empty tables
		final int nItems = getRowCount();
		for (int i = 0; i < nItems; i++, y += tempRowHeight)
		{
			tempRowHeight = getRowHeight(i);
			g.setColor(rowColors[i & 1]);
			g.fillRect(x, y, w, tempRowHeight);
		}
		// Use last row height for remainder of table area
		final int nRows = nItems + (insets.top + h - y) / tempRowHeight;
		for (int i = nItems; i < nRows; i++, y += tempRowHeight)
		{
			g.setColor(rowColors[i & 1]);
			g.fillRect(x, y, w, tempRowHeight);
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

    public int autoResizeColumn(int colIx)
    {
        return autoResizeColumn(colIx, false, -1);
    }
	
	public int autoResizeColumn(int colIx, boolean fixedWidth)
    {
        return autoResizeColumn(colIx, fixedWidth, -1);
    }

    public int autoResizeColumn(int colIx, boolean fixedWidth, int minWidth)
    {
        TableCellRenderer renderer = getCellRenderer(0, colIx);
        int maxWidth = 0;
        for (int rowIx = 0; rowIx < getRowCount(); rowIx++)
        {
            Object val = getValueAt(rowIx, colIx);
            Component comp = renderer.getTableCellRendererComponent(this, val, false, false, rowIx, colIx);
            int width = comp.getPreferredSize().width;
            if (width > maxWidth)
                maxWidth = width;
        }
        // add 2 for default intercell spacing
        maxWidth += 2;
		if (maxWidth < minWidth)
		{
			maxWidth = minWidth;
		}
        TableColumn col = getColumnModel().getColumn(colIx);
        col.setPreferredWidth(maxWidth);
        if (fixedWidth)
        {
            col.setMaxWidth(maxWidth);
            col.setMinWidth(maxWidth);
        }
        return maxWidth;
    }
    
    public void initFillColumnForScrollPane(final JScrollPane scroller)
    {
        scroller.addComponentListener(new java.awt.event.ComponentAdapter() 
        {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) 
            {
                setFillerColumnWidth(scroller);
            }
        });
    }

    public void setFillerColumnWidth(JScrollPane scroller)
    {
        TableColumnModel cm = getColumnModel();
        int colCount = cm.getColumnCount();
        int lastIx = colCount - 1;
        int normWidth = 0;
        for (int ix=0; ix < lastIx; ix++)
        {
            normWidth += cm.getColumn(ix).getPreferredWidth();
        }
        int viewWidth = scroller.getViewport().getWidth();
        TableColumn fillCol = cm.getColumn(lastIx);
        if (normWidth < viewWidth)
        {
            fillCol.setPreferredWidth(viewWidth - normWidth);
        }
        else
        {
            fillCol.setPreferredWidth(0);
        }
    }

    public static class IntRenderer extends DefaultTableCellRenderer
    {
		private static final NumberFormat _intFormatter = NumberFormat.getIntegerInstance();
		
        public IntRenderer()
        {
            super();
            setHorizontalAlignment(JLabel.RIGHT);
        }

        @Override
        protected void setValue(Object value)
        {
            setText((value == null) ? "" : _intFormatter.format(value));
        }
    }
}
