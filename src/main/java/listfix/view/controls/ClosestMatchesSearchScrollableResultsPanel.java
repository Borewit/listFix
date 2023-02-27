package listfix.view.controls;

import listfix.model.BatchMatchItem;
import listfix.model.playlists.PotentialPlaylistEntryMatch;
import listfix.view.support.ZebraJTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

public class ClosestMatchesSearchScrollableResultsPanel extends JPanel
{
  private List<BatchMatchItem> _items;

  private static final Logger _logger = LogManager.getLogger(ClosestMatchesSearchScrollableResultsPanel.class);
  private int _width;

  public ClosestMatchesSearchScrollableResultsPanel()
  {
    _items = new ArrayList<>();
    initComponents();
    initialize();
  }

  /**
   * Creates new form ClosestMatchesSearchScrollableResultsPanel
   */
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
    ArrayList<RowSorter.SortKey> keys = new ArrayList<>();
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
              return item.getEntry().getTrackFolder();
            }
            else
            {
              PotentialPlaylistEntryMatch match = item.getSelectedMatch();
              if (match != null)
              {
                return match.getPlaylistFile().getTrackFolder();
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
    
    _uiTable.autoResizeColumn(1);
    _uiTable.autoResizeColumn(2);
    _uiTable.autoResizeColumn(3);
    TableColumnModel cm = _uiTable.getColumnModel();
    TableCellRenderer renderer = _uiTable.getDefaultRenderer(Integer.class);
    Component comp = renderer.getTableCellRendererComponent(_uiTable, (_uiTable.getRowCount() + 1) * 10, false, false, 0, 0);
    int width = comp.getPreferredSize().width + 4;
    TableColumn col = cm.getColumn(0);
    col.setMinWidth(width);
    col.setMaxWidth(width);
    col.setPreferredWidth(width);

    _uiTable.setFillerColumnWidth(_uiScrollPane);
  }

  private static class ButtonRenderer implements TableCellRenderer
  {
    private JButton button = new JButton();

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
    private final JTable table;
    private final JButton button = new JButton();
    private int clickCountToStart = 1;

    ButtonEditor(JTable table)
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
      PotentialPlaylistEntryMatch match = item.getSelectedMatch();

      try
      {
        match.getPlaylistFile().play(match.getPlaylistOptions());
      }
      catch (InterruptedException ex)
      {
        // ignore, these happen when people cancel - should not be logged either.
      }
      catch (Exception ex)
      {
        _logger.warn(ex);
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
            return item.getEntry().getTrackFileName();

          case 2:
            return "";
          case 3:
            PotentialPlaylistEntryMatch match = item.getSelectedMatch();
            if (match != null)
            {
              return match.getPlaylistFile().getTrackFileName();
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
    MatchEditor()
    {
      _model = new MatchComboBoxModel();
      _combo = new JComboBox(_model);
      _combo.setRenderer(new MyComboBoxRenderer());
      _combo.setMaximumRowCount(25);
      _combo.setFocusable(false);
    }

    private final JComboBox _combo;
    private final MatchComboBoxModel _model;

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
            list.setToolTipText(((PotentialPlaylistEntryMatch) ((MatchComboBoxModel) list.getModel())._matches.get(index - 1)).getPlaylistFile().getTrackFolder());
          }
        }

        return comp;
      }
    }
  }

  private static class MatchComboBoxModel extends AbstractListModel implements ComboBoxModel
  {
    private List<PotentialPlaylistEntryMatch> _matches;
    private Object _selected;

    public void setMatches(List<PotentialPlaylistEntryMatch> matches)
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
          PotentialPlaylistEntryMatch match = _matches.get(index - 1);
          return Integer.toString(match.getScore()) + ": " + match.getPlaylistFile().getTrackFileName();
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

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    _uiScrollPane = new JScrollPane();
    _uiTable = createTable();

    setLayout(new BorderLayout());

    _uiTable.setAutoCreateRowSorter(true);
    _uiTable.setModel(new MatchTableModel());
    _uiTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    _uiScrollPane.setViewportView(_uiTable);

    add(_uiScrollPane, BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JScrollPane _uiScrollPane;
  private ZebraJTable _uiTable;
  // End of variables declaration//GEN-END:variables

}
