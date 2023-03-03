package listfix.view.support;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class TreeTooltipRenderer extends DefaultTreeCellRenderer
{
  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
  {
    if (value instanceof ITooltipable)
    {
      super.setToolTipText(((ITooltipable) value).getToolTip());
    }
    return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
  }
}
