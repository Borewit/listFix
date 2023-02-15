package listfix.swing;

import javax.swing.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class JDocumentTabbedPane<G extends JComponent> extends JTabbedPane
{
  private final List<IDocumentChangeListener> documentChangeListeners = new LinkedList<>();

  public JDocumentTabbedPane()
  {
    this.addContainerListener(new ContainerListener()
    {
      @Override
      public void componentAdded(ContainerEvent e)
      {
        if (e.getChild() instanceof JDocumentComponent)
        {
          documentChangeListeners.forEach(listener -> listener.documentOpened((JDocumentComponent) e.getChild()));
        }
      }

      @Override
      public void componentRemoved(ContainerEvent e)
      {
        if (e.getChild() instanceof JDocumentComponent)
        {
          documentChangeListeners.forEach(listener -> listener.documentClosed((JDocumentComponent) e.getChild()));
        }
      }
    });

    this.addChangeListener(changeEvent -> {
      JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
      int index = sourceTabbedPane.getSelectedIndex();
      if (index != -1)
      {
        JDocumentComponent<G> doc = JDocumentTabbedPane.this.getPlaylistAt(index);
        documentChangeListeners.forEach(listener -> listener.documentActivated(doc));
      }
    });
  }

  public int getDocumentCount()
  {
    return super.getTabCount();
  }

  public JDocumentComponent<G> getPlaylistAt(int documentIndex)
  {
    return (JDocumentComponent<G>) super.getComponentAt(documentIndex);
  }

  public JDocumentComponent<G> getActiveTab()
  {
    int index = super.getSelectedIndex();
    if (index == -1) return null;
    return this.getPlaylistAt(index);
  }

  public void setActiveDocument(String name)
  {
    int i = this.getDocumentIndexByName(name);
    if (i != -1)
    {
      super.setSelectedIndex(i);

    }
  }

  public void setActiveDocument(Path path)
  {
    int i = this.getDocumentIndexByPath(path);
    if (i != -1)
    {
      super.setSelectedIndex(i);

    }
  }

  public int getDocumentIndexByPath(Path path)
  {
    for (int i = 0; i < super.getTabCount(); ++i)
    {
      JDocumentComponent doc = (JDocumentComponent) super.getComponentAt(i);
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

  public void nextPlaylist()
  {
    int nextIndex = (super.getSelectedIndex() + 1) % super.getTabCount();
    super.setSelectedIndex(nextIndex);
  }

  @Deprecated // Use Path instead
  public boolean isPlaylistOpened(String name)
  {
    int i = this.getDocumentIndexByName(name);
    return i != -1 && i == super.getSelectedIndex();
  }

  public void prevPlaylist()
  {
    int tabCount = super.getTabCount();
    int nextIndex = (tabCount + super.getSelectedIndex() - 1) % super.getTabCount();
    super.setSelectedIndex(nextIndex);
  }

  @Deprecated // Use Path instead
  public JDocumentComponent<G> getDocument(String name)
  {
    int i = getDocumentIndexByName(name);
    return i == -1 ? null : this.getPlaylistAt(i);
  }

  public JDocumentComponent<G> getDocument(Path path)
  {
    int i = getDocumentIndexByPath(path);
    return i == -1 ? null : this.getPlaylistAt(i);
  }

  public JDocumentComponent<G> openDocument(G editor, Path path)
  {
    return this.openDocument(editor, path, null);
  }

  public JDocumentComponent<G> openDocument(G editor, Path path, ImageIcon icon)
  {
    JDocumentComponent<G> doc = new JDocumentComponent<G>(this, editor, path);
    doc.setIcon(icon);
    int index = this.getTabCount();
    this.addTab(doc.getTitle(), doc.getIcon(), doc, doc.getPath().toString());

    JClosableTabComponent tabComponent = doc.getTabComponent();
    this.setTabComponentAt(index, tabComponent);
    tabComponent.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        JDocumentTabbedPane.this.setActiveDocument(doc.getPath());
      }
    });



    return doc;
  }

  public boolean renameDocument(Path oldPath, Path newPath)
  {
    JDocumentComponent jPlaylist = this.getDocument(oldPath);
    if (jPlaylist != null)
    {
      jPlaylist.setName(newPath.getFileName().toString());
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

  public void closeActivePlaylist()
  {
    int i = super.getSelectedIndex();
    if (i != -1)
    {
      this.closeDocument(i);
    }
  }

  public void closeDocument(int i)
  {
    JDocumentComponent doc = this.getPlaylistAt(i);
    if (doc != null)
    {
      super.remove(i);
    }
  }

  public void tryToClose(int i)
  {
    JDocumentComponent doc = this.getPlaylistAt(i);
    for (IDocumentChangeListener listener : documentChangeListeners)
    {
      if (!listener.tryClosingDocument(doc))
      {
        return; // closing cancelled
      }
    }
    // No objected to close the document
    this.remove(i);
  }

  public void addDocumentChangeListener(IDocumentChangeListener listener)
  {
    this.documentChangeListeners.add(listener);
  }

  public void removeCloseDocumentListener(IDocumentChangeListener listener)
  {
    this.documentChangeListeners.remove(listener);
  }

  public List<G> getAllEmbeddedMainComponent()
  {
    List<G> list = new LinkedList<>();
    int tabCount = this.getTabCount();
    for (int i = 0; i < tabCount; ++i)
    {
      JDocumentComponent<G> doc = (JDocumentComponent<G>) this.getComponentAt(i);
      list.add(doc.getComponent());
    }
    return list;
  }

}
