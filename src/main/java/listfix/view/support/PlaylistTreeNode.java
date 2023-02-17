package listfix.view.support;

import javax.swing.tree.DefaultMutableTreeNode;
import java.nio.file.Path;

public class PlaylistTreeNode extends DefaultMutableTreeNode implements ITooltipable
{
  public PlaylistTreeNode(Path path)
  {
    super(path);
  }

  @Override
  public int hashCode()
  {
    return this.getUserObject().hashCode();
  }

  @Override
  public Path getUserObject() {
    return (Path) super.getUserObject();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    return hashCode() == (obj).hashCode();
  }

  @Override
  public String toString() {
    return this.getUserObject().getFileName().toString();
  }

  @Override
  public String getToolTip() {
    return this.getUserObject().toString();
  }
}
