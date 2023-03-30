package listfix.view.controls;

import listfix.model.BatchMatchItem;
import listfix.model.playlists.FilePlaylistEntry;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

public class ClosestMatchesSearchScrollableResultsPanel extends JPanel
{
  private List<BatchMatchItem> _items;
  private static final Logger _logger = LogManager.getLogger(ClosestMatchesSearchScrollableResultsPanel.class);
  private int _width;
  private JPopupMenu _playlistEntryRightClickMenu;
  private JScrollPane _uiScrollPane;
  private ZebraJTable _uiTable;
  private BatchMatchItem contextMatchItem = null;

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

    this._uiTable.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        if (SwingUtilities.isRightMouseButton(e) && ClosestMatchesSearchScrollableResultsPanel.this._uiTable.getSelectedRows().length > 0)
        {
          Point p = e.getPoint();
          int rowNr = ClosestMatchesSearchScrollableResultsPanel.this._uiTable.rowAtPoint(e.getPoint());
          ClosestMatchesSearchScrollableResultsPanel.this.contextMatchItem = rowNr == -1 ? null : _items.get(rowNr);
          _logger.debug(String.format("Selected row %d: %s", rowNr, ClosestMatchesSearchScrollableResultsPanel.this.contextMatchItem));
          ClosestMatchesSearchScrollableResultsPanel.this._playlistEntryRightClickMenu.show(e.getComponent(), p.x, p.y);
        }
      }
    });
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
                return match.getTrackFolder();
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
    private final JButton button = new JButton();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
      button.setText("PLAY");
      return button;
    }
  }

  private class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
  {
    private final JTable table;
    private final JButton button = new JButton();

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
        // ToDo match.getTrack().play(match.getPlaylistOptions());
        throw new UnsupportedOperationException("ToDo");
      }
      catch (Exception ex)
      {
        _logger.warn(ex);
      }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
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
        int clickCountToStart = 1;
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
      if (rowIndex >= _items.size())
      {
        return null;
      }

      final BatchMatchItem item = _items.get(rowIndex);
      return switch (columnIndex)
        {
          case 0 -> rowIndex + 1;
          case 1 -> item.getEntry().getTrackFileName();
          case 2 -> "";
          case 3 ->
            item.getSelectedMatch() == null ? "< skip >" : item.getSelectedMatch().getTrack().getFileName().toString();
          default -> null;
        };
    }

    @Override
    public String getColumnName(int column)
    {
      return switch (column)
        {
          case 0 -> "#";
          case 1 -> "Original Name";
          case 2 -> "Preview";
          case 3 -> "Matched Name";
          default -> null;
        };
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
  }

  private class MatchEditor extends AbstractCellEditor implements TableCellEditor
  {
    private final JComboBox<PotentialPlaylistEntryMatch> combo;
    private final MatchComboBoxModel model;

    MatchEditor()
    {
      this.model = new MatchComboBoxModel();
      this.combo = new JComboBox<>(model);
      this.combo.setRenderer(new MyComboBoxRenderer());
      this.combo.setMaximumRowCount(25);
      this.combo.setFocusable(false);
      this.combo.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mousePressed(MouseEvent e)
        {
          if (SwingUtilities.isRightMouseButton(e))
          {
            ClosestMatchesSearchScrollableResultsPanel.this.contextMatchItem = MatchEditor.this.model.getMatchItem();
            ClosestMatchesSearchScrollableResultsPanel.this._playlistEntryRightClickMenu.show(MatchEditor.this.combo, e.getX(), e.getY());
          }
        }
      });
    }

    @Override
    public Object getCellEditorValue()
    {
      return combo.getSelectedIndex() - 1;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
      BatchMatchItem item = _items.get(row);
      model.setMatchedItem(item);
      // _model.setMatches(item);
      combo.setSelectedIndex(item.getSelectedIx());
      return combo;
    }

    private class MyComboBoxRenderer extends BasicComboBoxRenderer
    {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
      {
        JComponent comp = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (isSelected)
        {
          if (index > 0)
          {
            list.setToolTipText(((PotentialPlaylistEntryMatch) value).getTrackFolder());
          }
        }

        return comp;
      }
    }
  }

  private static class MatchComboBoxModel extends DefaultComboBoxModel<PotentialPlaylistEntryMatch>
  {

    private BatchMatchItem batchMatchItem;

    public void setMatchedItem(BatchMatchItem batchMatchItem)
    {
      this.batchMatchItem = batchMatchItem;
      super.removeAllElements();
      super.addAll(batchMatchItem.getMatches());
    }

    public BatchMatchItem getMatchItem()
    {
      return this.batchMatchItem;
    }

    @Override
    public void setSelectedItem(Object anItem)
    {
      _logger.info(String.format("Selected: %s ", anItem));
      this.batchMatchItem.setSelectedMatch((PotentialPlaylistEntryMatch) anItem);
      super.setSelectedItem(anItem);
    }
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   */
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

    _playlistEntryRightClickMenu = new JPopupMenu();

    JMenuItem _miOpenFileLocation = new JMenuItem();
    _miOpenFileLocation.setText("Open match file location");
    _miOpenFileLocation.addActionListener(evt -> {
      // Note that the _miOpenFileLocation.location is location of the original mouse click
      this.openClickedMatchedPlayListEntryLocation();
    });
    _playlistEntryRightClickMenu.add(_miOpenFileLocation);
  }

  private void openClickedMatchedPlayListEntryLocation()
  {
    if (this.contextMatchItem != null)
    {
      PotentialPlaylistEntryMatch match = contextMatchItem.getSelectedMatch();
      if (match != null)
      {
        _logger.debug(String.format("Selected: %s", match));
        Path path = match.getTrack().toAbsolutePath();
        if (path.getParent() != null)
        {
          try
          {
            Desktop.getDesktop().open(path.getParent().toFile());
          }
          catch (IOException e)
          {
            throw new RuntimeException(e);
          }
        }
      }
    }
  }

}
