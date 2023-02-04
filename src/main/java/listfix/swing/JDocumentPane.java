package listfix.swing;

import javax.swing.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.LinkedList;
import java.util.List;

public class JDocumentPane extends JTabbedPane
{

  private final List<IDocumentChangeListener> documentChangeListeners = new LinkedList<>();

  public JDocumentPane()
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
  }

  public int getDocumentCount()
  {
    return super.getTabCount();
  }

  public JDocumentComponent getDocumentAt(int documentIndex)
  {
    return (JDocumentComponent) super.getComponentAt(documentIndex);
  }

  public JDocumentComponent getActiveDocument()
  {
    int index = super.getSelectedIndex();
    if (index == -1) return null;
    return this.getDocumentAt(index);
  }

  public void setActiveDocument(String name)
  {
    int i = this.getDocumentIndexByName(name);
    if (i != -1)
    {
      super.setSelectedIndex(i);

    }
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

  public boolean isDocumentOpened(String name)
  {
    int i = this.getDocumentIndexByName(name);
    return i != -1 && i == super.getSelectedIndex();
  }

  public void prevDocument()
  {
    int nextIndex = (super.getSelectedIndex() - 1) % super.getTabCount();
    super.setSelectedIndex(nextIndex);
  }

  public JDocumentComponent getDocument(String name)
  {
    int i = getDocumentIndexByName(name);
    return i == -1 ? null : this.getDocumentAt(i);
  }

  public void openDocument(JDocumentComponent document)
  {
    int index = super.getTabCount();
    this.addTab(document.getTitle(), document.getIcon(), document, document.getPath().toString());

    JPanel tabComponent = new JButtonTabComponent(this, document.getIcon());
    this.setTabComponentAt(index, tabComponent);
  }

  public boolean renameDocument(String oldName, String newName)
  {
    JDocumentComponent doc = getDocument(oldName);
    if (doc != null)
    {
      doc.setName(newName);
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
    JDocumentComponent doc = this.getDocumentAt(i);
    if (doc != null)
    {
      super.remove(i);
    }
  }

  public void tryToClose(int i)
  {
    JDocumentComponent doc = this.getDocumentAt(i);
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

}
