package listfix.swing;

public interface IDocumentComponentListener
{
  void documentComponentOpened(DocumentComponentEvent e);

  void documentComponentClosing(DocumentComponentEvent e);

  void documentComponentClosed(DocumentComponentEvent e);
}
