/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2012 Jeremy Caron
 * 
 *  This file is part of listFix().
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.view.controls;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import listfix.model.BatchMatchItem;
import listfix.model.MatchedPlaylistEntry;
import listfix.util.ExStack;
import listfix.view.support.ZebraJTable;

import org.apache.log4j.Logger;

/**
 *
 * @author jcaron
 */
public class ClosestMatchesSearchScrollableResultsPanel extends javax.swing.JPanel
{
	private List<BatchMatchItem> _items;

	private static final Logger _logger = Logger.getLogger(ClosestMatchesSearchScrollableResultsPanel.class);
	private int _width;

	public ClosestMatchesSearchScrollableResultsPanel()
	{
		_items = new ArrayList<BatchMatchItem>();
		initComponents();
		initialize();
	}
    /** Creates new form ClosestMatchesSearchScrollableResultsPanel */
    public ClosestMatchesSearchScrollableResultsPanel(List<BatchMatchItem> items)
	{
		_items = items;
		initComponents();
		initialize();
    }

	private void initialize()
	{			
		TableColumnModel cm = _uiTable.getColumnModel();		
		cm.getColumn(2).setCellRenderer(new ButtonRenderer());
		cm.getColumn(2).setCellEditor(new ButtonEditor(_uiTable));
		cm.getColumn(3).setCellEditor(new MatchEditor());
		
		int cwidth = 0;
		cm.getColumn(4).setMinWidth(0);
		_uiTable.initFillColumnForScrollPane(_uiScrollPane);
		cwidth += _uiTable.autoResizeColumn(1);
		cwidth += cm.getColumn(2).getWidth();
		cwidth += _uiTable.autoResizeColumn(3, false, 350);
		_uiTable.setFillerColumnWidth(_uiScrollPane);
		TableCellRenderer renderer = _uiTable.getDefaultRenderer(Integer.class);
		Component comp = renderer.getTableCellRendererComponent(_uiTable, (_items.size() + 1) * 10, false, false, 0, 0);
		int width = comp.getPreferredSize().width + 4;
		TableColumn col = cm.getColumn(0);
		col.setMinWidth(width);
		col.setMaxWidth(width);
		col.setPreferredWidth(width);
		cwidth += width + 20;
		_uiTable.setCellSelectionEnabled(true);
		_width = cwidth;
		_uiTable.setShowHorizontalLines(false);
		_uiTable.setShowVerticalLines(false);
		
		// set sort to #
		ArrayList<RowSorter.SortKey> keys = new ArrayList<RowSorter.SortKey>();
		keys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		_uiTable.getRowSorter().setSortKeys(keys);
		_uiTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public int getSelectedRow()
	{
		return _uiTable.getSelectedRow();
	}

	public int getSelectedColumn()
	{
		return _uiTable.getSelectedColumn();
	}

	public TableCellEditor getCellEditor(int row, int column)
	{
		return _uiTable.getCellEditor(row, column);
	}

	public int getTableWidth()
	{
		return _width;
	}

	@Override
	public int getWidth()
	{
		return _uiScrollPane.getWidth();
	}

	private ZebraJTable createTable()
	{
		return new ZebraJTable()
		{
			@Override
			public String getToolTipText(MouseEvent event)
			{
				Point point = event.getPoint();
				int rawRowIx = rowAtPoint(point);
				int rawColIx = columnAtPoint(point);
				if (rawRowIx >= 0 && rawColIx >= 0)
				{
					int rowIx = convertRowIndexToModel(rawRowIx);
					int colIx = convertColumnIndexToModel(rawColIx);
					if (rowIx >= 0 && rowIx < _items.size() && (colIx == 1 || colIx == 2 || colIx == 3))
					{
						BatchMatchItem item = _items.get(rowIx);
						if (colIx == 1)
						{
							return item.getEntry().getPath();
						}
						else
						{
							MatchedPlaylistEntry match = item.getSelectedMatch();
							if (match != null)
							{
								return match.getPlaylistFile().getPath();
							}
						}
					}
				}
				return super.getToolTipText(event);
			}
		};
	}

	public void setResults(List<BatchMatchItem> closestMatches)
	{
		_items = closestMatches;
		initialize();
		((MatchTableModel) _uiTable.getModel()).fireTableDataChanged();
		resizeAllColumns();
	}

	private void resizeAllColumns()
	{
		// resize columns to fit
		int cwidth = 0;
		cwidth += _uiTable.autoResizeColumn(1);
		cwidth += _uiTable.autoResizeColumn(2);
		cwidth += _uiTable.autoResizeColumn(3);
		TableColumnModel cm = _uiTable.getColumnModel();
		TableCellRenderer renderer = _uiTable.getDefaultRenderer(Integer.class);
		Component comp = renderer.getTableCellRendererComponent(_uiTable, (_uiTable.getRowCount() + 1) * 10, false, false, 0, 0);
		int width = comp.getPreferredSize().width + 4;
		TableColumn col = cm.getColumn(0);
		col.setMinWidth(width);
		col.setMaxWidth(width);
		col.setPreferredWidth(width);
		// _uiTable.initFillColumnForScrollPane(_uiScrollPane);
		_uiTable.setFillerColumnWidth(_uiScrollPane);
	}

	private class ButtonRenderer implements TableCellRenderer
	{
		JButton button = new JButton();

		@Override
		public Component getTableCellRendererComponent(JTable table,
			Object value,
			boolean isSelected,
			boolean hasFocus,
			int row, int column)
		{
			button.setText("PLAY");
			return button;
		}
	}

	private class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
	{
		JTable table;
		JButton button = new JButton();
		int clickCountToStart = 1;

		public ButtonEditor(JTable table)
		{
			this.table = table;
			button.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			int row = table.getEditingRow();
			int rowIx = table.getRowSorter().convertRowIndexToModel(row);
			BatchMatchItem item = _items.get(rowIx);
			MatchedPlaylistEntry match = item.getSelectedMatch();

			try
			{
				match.getPlaylistFile().play();
			}
			catch (IOException ex)
			{
				_logger.warn(ExStack.toString(ex));
			}
			catch (InterruptedException ex)
			{
				// ignore, these happen when people cancel - should not be logged either.
			}
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
			Object value,
			boolean isSelected,
			int row, int column)
		{
			button.setText("PLAY");
			return button;
		}

		@Override
		public Object getCellEditorValue()
		{
			return button.getText();
		}

		@Override
		public boolean isCellEditable(EventObject anEvent)
		{
			if (anEvent instanceof MouseEvent)
			{
				return ((MouseEvent) anEvent).getClickCount() >= clickCountToStart;
			}
			return true;
		}

		@Override
		public boolean shouldSelectCell(EventObject anEvent)
		{
			return true;
		}

		@Override
		public boolean stopCellEditing()
		{
			return super.stopCellEditing();
		}

		@Override
		public void cancelCellEditing()
		{
			super.cancelCellEditing();
		}
	}

	private class MatchTableModel extends AbstractTableModel
	{
		@Override
		public int getRowCount()
		{
			return _items.size();
		}

		@Override
		public int getColumnCount()
		{
			return 5;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			if (rowIndex < _items.size())
			{
				final BatchMatchItem item = _items.get(rowIndex);
				switch (columnIndex)
				{
					case 0:
						return rowIndex + 1;

					case 1:
						return item.getEntry().getFileName();

					case 2:
						return "";
					case 3:
						MatchedPlaylistEntry match = item.getSelectedMatch();
						if (match != null)
						{
							return match.getPlaylistFile().getFileName();
						}
						else
						{
							return "< skip >";
						}

					default:
						return null;
				}
			}
			return null;
		}

		@Override
		public String getColumnName(int column)
		{
			switch (column)
			{
				case 0:
					return "#";
				case 1:
					return "Original Name";
				case 2:
					return "Preview";
				case 3:
					return "Matched Name";
				default:
					return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			return columnIndex == 0 ? Integer.class : Object.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			//return super.isCellEditable(rowIndex, columnIndex);
			return columnIndex == 2 || columnIndex == 3;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			if (columnIndex == 3)
			{
				int ix = (Integer) aValue;
				//Log.write("set %d to %d", rowIndex, ix);
				BatchMatchItem item = _items.get(rowIndex);
				item.setSelectedIx(ix);
			}
		}
	}

	private class MatchEditor extends AbstractCellEditor implements TableCellEditor
	{
		public MatchEditor()
		{
			_model = new MatchComboBoxModel();
			_combo = new JComboBox(_model);
			_combo.setRenderer(new MyComboBoxRenderer());
			_combo.setMaximumRowCount(25);
			_combo.setFocusable(false);
		}
		JComboBox _combo;
		MatchComboBoxModel _model;

		@Override
		public Object getCellEditorValue()
		{
			return _combo.getSelectedIndex() - 1;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
		{
			BatchMatchItem item = _items.get(row);
			_model.setMatches(item.getMatches());
			_combo.setSelectedIndex(item.getSelectedIx() + 1);
			return _combo;
		}

		private class MyComboBoxRenderer extends BasicComboBoxRenderer
		{
			@Override
			public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus)
			{
				JComponent comp = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (isSelected)
				{
					if (index > 0)
					{
						list.setToolTipText(((MatchedPlaylistEntry)((MatchComboBoxModel)list.getModel())._matches.get(index - 1)).getPlaylistFile().getPath());
					}
				}

				return comp;
			}
		}
	}

	private static class MatchComboBoxModel extends AbstractListModel implements ComboBoxModel
	{
		List<MatchedPlaylistEntry> _matches;
		Object _selected;

		public void setMatches(List<MatchedPlaylistEntry> matches)
		{
			_matches = matches;
			_selected = null;
			fireContentsChanged(this, 0, _matches.size());
		}

		@Override
		public int getSize()
		{
			return _matches != null ? _matches.size() + 1 : 0;
		}

		@Override
		public Object getElementAt(int index)
		{
			if (_matches != null)
			{
				if (index > 0)
				{
					MatchedPlaylistEntry match = _matches.get(index - 1);
					return Integer.toString(match.getScore()) + ": " + match.getPlaylistFile().getFileName();
				}
				else
				{
					return "< skip >";
				}
			}
			else
			{
				return null;
			}
		}

		@Override
		public void setSelectedItem(Object anItem)
		{
			_selected = anItem;
		}

		@Override
		public Object getSelectedItem()
		{
			return _selected;
		}
	}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _uiScrollPane = new javax.swing.JScrollPane();
        _uiTable = createTable();

        setLayout(new java.awt.BorderLayout());

        _uiTable.setAutoCreateRowSorter(true);
        _uiTable.setModel(new MatchTableModel());
        _uiTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        _uiScrollPane.setViewportView(_uiTable);

        add(_uiScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane _uiScrollPane;
    private listfix.view.support.ZebraJTable _uiTable;
    // End of variables declaration//GEN-END:variables

}
