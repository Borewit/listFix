package listfix.view.support;

import java.nio.file.Path;
import javax.swing.tree.DefaultMutableTreeNode;

public class PlaylistTreeNode extends DefaultMutableTreeNode
    implements ITooltipable, Comparable<PlaylistTreeNode> {
  public PlaylistTreeNode(Path path) {
    super(path);
  }

  @Override
  public int hashCode() {
    return this.getUserObject().hashCode();
  }

  @Override
  public Path getUserObject() {
    return (Path) super.getUserObject();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PlaylistTreeNode) {
      return this.compareTo((PlaylistTreeNode) obj) == 0;
    }
    return false;
  }

  @Override
  public String toString() {
    return this.getUserObject().getFileName().toString();
  }

  @Override
  public String getToolTip() {
    return this.getUserObject().toString();
  }

  @Override
  public int compareTo(PlaylistTreeNode o) {
    if (o == null) return -1;
    Path path = this.getUserObject();
    if (path == null) {
      return o.getUserObject() == null ? 0 : -1;
    }
    return path.compareTo(o.getUserObject());
  }
}
