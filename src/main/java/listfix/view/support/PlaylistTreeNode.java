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
  public Path getUserObject()
  {
    return (Path) super.getUserObject();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof PlaylistTreeNode)
    {
      return this.equals((PlaylistTreeNode) obj);
    }
    return false;
  }

  public boolean equals(PlaylistTreeNode obj)
  {
    Path path = this.getUserObject();
    return path != null && path.equals(obj.getUserObject());
  }

  @Override
  public String toString()
  {
    return this.getUserObject().getFileName().toString();
  }

  @Override
  public String getToolTip()
  {
    return this.getUserObject().toString();
  }
}
