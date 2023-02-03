package listfix.swing;

import javax.swing.*;

public class JDocumentPane extends JTabbedPane
{

  public JDocumentPane()
  {
  }

  public int getDocumentCount()
  {
    return super.getTabCount();
  }

  public JDocumentComponent getDocumentAt(int documentIndex)
  {
    return (JDocumentComponent) super.getComponent(documentIndex);
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
    int i = super.getTabCount();
    super.addTab(document.getTitle(), document.getIcon(), document, document.getPath().toString());
    document.notifyOpened();
  }

  public boolean closeDocument(String name)
  {
    int i = getDocumentIndexByName(name);
    if (i != -1)
    {
      this.closeDocument(i);
      return true;
    }
    return false;
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


  public String getActiveDocumentName()
  {
    JDocumentComponent doc = this.getActiveDocument();
    return doc == null ? null : doc.getName();
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
    if (doc != null) {
      doc.notifyClosing();
      super.remove(i);
      doc.notifyClosed();
    }
  }
}
