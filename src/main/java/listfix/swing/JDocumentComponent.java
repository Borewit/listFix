package listfix.swing;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

public class JDocumentComponent extends JPanel
{
  private Path path;
  private ImageIcon icon;

  public JDocumentComponent(Path path)
  {
    this.path = path;
    this.setTitle(path.getFileName().toString());
    this.setTooltip(path.toString());
    this.setLayout(new GridLayout(1, 1)); // Use all available tab space
  }

  public JDocumentComponent(JComponent mainComponent, Path path, String title, ImageIcon icon)
  {
    this(path);
    this.setTitle(title);
    super.add(mainComponent);
    this.icon = icon;
  }

  public static JDocumentComponent getDocument(String path)
  {
    return getDocument(Path.of(path));
  }

  public static JDocumentComponent getDocument(Path path)
  {
    return new JDocumentComponent(path);
  }

  public JComponent getComponent()
  {
    return (JComponent) super.getComponent(0);
  }

  public void setTitle(String title)
  {
    super.setName(title);
  }

  public void setTooltip(String text)
  {
    super.setToolTipText(text);
  }

  public String getTitle()
  {
    return this.getName();
  }

  public ImageIcon getIcon()
  {
    return this.icon;
  }

  public void setIcon(ImageIcon icon)
  {
    this.icon = icon;
  }

  public Path getPath()
  {
    return path;
  }
}
