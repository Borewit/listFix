package listfix.swing;

import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;

public class JDocumentTabbedPane<G extends JComponent> extends JTabbedPane
{
  private final List<IDocumentChangeListener<G>> documentChangeListeners = new ArrayList<>();

  public JDocumentTabbedPane()
  {
    this.addContainerListener(new ContainerListener()
    {
      @Override
      public void componentAdded(ContainerEvent e)
      {
        if (e.getChild() instanceof JDocumentComponent)
        {
          documentChangeListeners.forEach(listener -> listener.documentOpened((JDocumentComponent<G>) e.getChild()));
        }
      }

      @Override
      public void componentRemoved(ContainerEvent e)
      {
        if (e.getChild() instanceof JDocumentComponent)
        {
          documentChangeListeners.forEach(listener -> listener.documentClosed((JDocumentComponent<G>) e.getChild()));
        }
      }
    });

    this.addChangeListener(changeEvent -> {
      JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
      int index = sourceTabbedPane.getSelectedIndex();
      if (index != -1)
      {
        JDocumentComponent<G> doc = JDocumentTabbedPane.this.getComponentAt(index);
        documentChangeListeners.forEach(listener -> listener.documentActivated(doc));
      }
    });
  }

  public int getDocumentCount()
  {
    return super.getTabCount();
  }

  @Override
  public JDocumentComponent<G> getComponentAt(int documentIndex)
  {
    return (JDocumentComponent<G>) super.getComponentAt(documentIndex);
  }

  public JDocumentComponent<G> getActiveTab()
  {
    int index = super.getSelectedIndex();
    if (index == -1) return null;
    return this.getComponentAt(index);
  }

  public void setActiveDocument(Path path)
  {
    int i = this.getDocumentIndexByPath(path);
    if (i != -1)
    {
      super.setSelectedIndex(i);

    }
  }

  public void setActiveDocument(JDocumentComponent<G> doc)
  {
    super.setSelectedIndex(this.getDocumentIndex(doc));
  }

  public int getDocumentIndex(JDocumentComponent<G> doc)
  {
    for (int i = 0; i < super.getTabCount(); ++i)
    {
      if (this.getComponentAt(i) == doc)
      {
        return i;
      }
    }
    return -1;
  }

  public int getDocumentIndexByPath(Path path)
  {
    for (int i = 0; i < super.getTabCount(); ++i)
    {
      JDocumentComponent<G> doc = this.getComponentAt(i);
      if (path.equals(doc.getPath()))
      {
        return i;
      }
    }
    return -1;
  }

  public int getDocumentIndexByName(String name)
  {
    for (int i = 0; i < super.getTabCount(); ++i)
    {
      JDocumentComponent doc = (JDocumentComponent) super.getComponentAt(i);
      if (name.equals(doc.getName()))
      {
        return i;
      }
    }
    return -1;
  }

  public void nextDocument()
  {
    int nextIndex = (super.getSelectedIndex() + 1) % super.getTabCount();
    super.setSelectedIndex(nextIndex);
  }

  public void prevPlaylist()
  {
    int tabCount = super.getTabCount();
    int nextIndex = (tabCount + super.getSelectedIndex() - 1) % super.getTabCount();
    super.setSelectedIndex(nextIndex);
  }

  public JDocumentComponent<G> getDocument(Path path)
  {
    int i = getDocumentIndexByPath(path);
    return i == -1 ? null : this.getComponentAt(i);
  }

  public JDocumentComponent<G> openDocument(G editor, Path path)
  {
    return this.openDocument(editor, path, null);
  }

  /**
   * Remove document with corresponding path
   * @param path Path to remove
   */
  public void remove(Path path) {
    this.remove(this.getDocument(path));
  }

  public JDocumentComponent<G> openDocument(G editor, Path path, ImageIcon icon)
  {
    JDocumentComponent<G> doc = new JDocumentComponent<G>(this, editor, path);
    doc.setIcon(icon);
    int index = this.getTabCount();
    this.addTab(null, null, doc, doc.getPath().toString());

    JClosableTabComponent<G> tabComponent = doc.getTabComponent();
    this.setTabComponentAt(index, tabComponent);
    tabComponent.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        JDocumentTabbedPane.this.setActiveDocument(doc);
      }
    });

    return doc;
  }

  public boolean renameDocument(Path oldPath, Path newPath)
  {
    JDocumentComponent<G> doc = this.getDocument(oldPath);
    if (doc != null)
    {
      doc.setName(newPath.getFileName().toString());
      return true;
    }
    return false;
  }

  public void closeAll()
  {
    while (super.getTabCount() > 0)
    {
      this.closeDocument(0);
    }
  }

  public void closeActiveDocument()
  {
    int i = super.getSelectedIndex();
    if (i != -1)
    {
      this.closeDocument(i);
    }
  }

  public void closeDocument(int i)
  {
    JDocumentComponent<G> doc = this.getComponentAt(i);
    if (doc != null)
    {
      super.remove(i);
    }
  }

  public void tryToClose(int i)
  {
    JDocumentComponent<G> doc = this.getComponentAt(i);
    for (IDocumentChangeListener<G> listener : documentChangeListeners)
    {
      if (!listener.tryClosingDocument(doc))
      {
        return; // closing cancelled
      }
    }
    // No objected to close the document
    this.remove(i);
  }

  public void addDocumentChangeListener(IDocumentChangeListener<G> listener)
  {
    this.documentChangeListeners.add(listener);
  }

  public void removeCloseDocumentListener(IDocumentChangeListener<G> listener)
  {
    this.documentChangeListeners.remove(listener);
  }

  public List<G> getAllEmbeddedMainComponent()
  {
    List<G> list = new ArrayList<>();
    int tabCount = this.getTabCount();
    for (int i = 0; i < tabCount; ++i)
    {
      JDocumentComponent<G> doc = this.getComponentAt(i);
      list.add(doc.getComponent());
    }
    return list;
  }

}
