package listfix.swing;

import javax.swing.*;

public interface IDocumentChangeListener<E extends JComponent>
{
  /**
   * Fired when the user tried to close the document
   * @param document Document which user want to close
   * @return true, when document closure is granted, false when blocked
   */
  boolean tryClosingDocument(JDocumentComponent<E> document);

  /**
   * Notification a new document has been opened
   * @param document Document which has been opened
   */
  void documentOpened(JDocumentComponent<E> document);

  /**
   * Notification a document has been closed
   * @param document Document which has been opened
   */
  void documentClosed(JDocumentComponent<E> document);

  /**
   * Notification a document is active
   * @param doc Active document
   */
  void documentActivated(JDocumentComponent<E> doc);
}
