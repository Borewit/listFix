package listfix.swing;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static listfix.swing.DocumentComponentEvent.*;

public class JDocumentComponent extends JPanel
{
  private Path path;
  private ImageIcon icon;

  private List<IDocumentComponentListener> documentComponentListeners = new LinkedList<>();

  public JDocumentComponent(Path path)
  {
    this.path = path;
    this.setTitle(path.getFileName().toString());
    this.setTooltip(path.toString());
    this.setLayout(new GridLayout(1, 1)); // Use all available tab space
  }

  public JDocumentComponent(JComponent mainComponent, Path path, String title, ImageIcon icon) {
    this(path);
    this.setTitle(title);
    super.add(mainComponent);
    this.icon = icon;
  }

  public static JDocumentComponent getDocument(String path) {
    return getDocument(Path.of(path));
  }

  public static JDocumentComponent getDocument(Path path) {
    return new JDocumentComponent(path);
  }

  public JComponent getComponent() {
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

  public ImageIcon getIcon() {
    return this.icon;
  }

  public void setIcon(ImageIcon icon)
  {
    this.icon = icon;
  }

  public void setAllowClosing(boolean tryCloseTab)
  {
    // ToDo
  }

  public Path getPath()
  {
    return path;
  }

  public void addDocumentComponentListener(IDocumentComponentListener listener)
  {
    this.documentComponentListeners.add(listener);
  }

  void notifyOpened() {
    DocumentComponentEvent documentComponentEvent = new DocumentComponentEvent(this, DOCUMENT_COMPONENT_OPENED);
    this.documentComponentListeners.forEach(listener -> listener.documentComponentOpened(documentComponentEvent));
  }

  void notifyClosing() {
    DocumentComponentEvent documentComponentEvent = new DocumentComponentEvent(this, DOCUMENT_COMPONENT_CLOSING);
    this.documentComponentListeners.forEach(listener -> listener.documentComponentClosing(documentComponentEvent));
  }

  void notifyClosed() {
    DocumentComponentEvent documentComponentEvent = new DocumentComponentEvent(this, DOCUMENT_COMPONENT_CLOSED);
    this.documentComponentListeners.forEach(listener -> listener.documentComponentClosed(documentComponentEvent));
  }
}
