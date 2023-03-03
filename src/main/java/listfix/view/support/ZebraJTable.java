package listfix.view.support;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ZebraJTable extends JTable
{
  private final Color[] rowColors = new Color[2];
  private boolean drawStripes = false;

  public ZebraJTable()
  {
  }

  /**
   * Add stripes between cells and behind non-opaque cells.
   */
  @Override
  public void paintComponent(Graphics g)
  {
    drawStripes = isOpaque();
    if (!drawStripes)
    {
      super.paintComponent(g);
      return;
    }

    // Paint zebra background stripes
    updateZebraColors();
    final Insets insets = getInsets();
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

  /**
   * Add background stripes behind rendered cells.
   */
  @Override
  public Component prepareRenderer(
    TableCellRenderer renderer, int row, int col)
  {
    final Component c = super.prepareRenderer(renderer, row, col);
    if (drawStripes && !isCellSelected(row, col))
    {
      c.setBackground(rowColors[row & 1]);
    }
    return c;
  }

  /**
   * Add background stripes behind edited cells.
   */
  @Override
  public Component prepareEditor(
    TableCellEditor editor, int row, int col)
  {
    final Component c = super.prepareEditor(editor, row, col);
    if (drawStripes && !isCellSelected(row, col))
    {
      c.setBackground(rowColors[row & 1]);
    }
    return c;
  }

  /**
   * Force the table to fill the viewport's height.
   */
  @Override
  public boolean getScrollableTracksViewportHeight()
  {
    final Component p = getParent();
    if (!(p instanceof JViewport))
    {
      return false;
    }
    return p.getHeight() > getPreferredSize().height;
  }

  private void updateZebraColors()
  {
    rowColors[0] = getBackground();
    if (rowColors[0] == null)
    {
      rowColors[0] = Color.white;
      rowColors[1] = Color.white;
      return;
    }
    final Color sel = getSelectionBackground();
    if (sel == null)
    {
      rowColors[1] = rowColors[0];
      return;
    }
    rowColors[1] = new Color(240, 240, 240);
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
      {
        maxWidth = width;
      }
    }

    // add 6 for default intercell spacing
    maxWidth += 6;
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
    scroller.addComponentListener(new ComponentAdapter()
    {
      @Override
      public void componentResized(ComponentEvent evt)
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
    for (int ix = 0; ix < lastIx; ix++)
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
}
