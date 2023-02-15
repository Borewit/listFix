package listfix.swing;


import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

public class JDocumentComponent<M extends JComponent> extends JPanel
{
  private Path path;

  private JClosableTabComponent<M> closableTabComponent;

  private final JDocumentTabbedPane<M> pane;

  public JDocumentComponent(final JDocumentTabbedPane<M> pane, M mainComponent, Path path)
  {
    super.add(mainComponent);
    this.pane = pane;
    this.closableTabComponent = new JClosableTabComponent<M>(pane);

    this.setPath(path);
    this.setLayout(new GridLayout(1, 1)); // Use all available tab space
  }

  public M getComponent()
  {
    return (M) super.getComponent(0);
  }

  public String getTitle()
  {
    return this.getName();
  }

  public ImageIcon getIcon()
  {
    return this.closableTabComponent.getIcon();
  }

  public void setIcon(ImageIcon icon)
  {
    this.closableTabComponent.setIcon(icon);
  }

  public Path getPath()
  {
    return path;
  }

  public void setPath(Path path)
  {
    this.path = path;
    this.closableTabComponent.setTitle(path.getFileName().toString());
    this.closableTabComponent.setTooltip(path.toString());
  }

  public JClosableTabComponent<M> getTabComponent()
  {
    return this.closableTabComponent;
  }
}
