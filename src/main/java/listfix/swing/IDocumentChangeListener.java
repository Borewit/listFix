package listfix.swing;

public interface IDocumentChangeListener
{
  /**
   * Fired when the user tried to close the document
   * @param document Document which user want to close
   * @return true, when document closure is granted, false when blocked
   */
  boolean tryClosingDocument(JDocumentComponent document);

  /**
   * Notification a new document has been opened
   * @param document Document which has been opened
   */
  void documentOpened(JDocumentComponent document);

  /**
   * Notification a document has been closed
   * @param document Document which has been opened
   */
  void documentClosed(JDocumentComponent document);
}
