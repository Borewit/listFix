package listfix.swing;


/**
 *  Event for DocumentComponent.
 */
public class DocumentComponentEvent extends java.awt.AWTEvent
{
  /**
   *  The "document opened" event.  This event is delivered only the first time the document component is made
   *  visible.
   */
  public static final int DOCUMENT_COMPONENT_OPENED = 5999;

  /**
   *  The "document is closing" event. This event is delivered when the user attempts to close the document component,
   *  such as by clicking the document component's close button, or when a program attempts to close the document
   *  component by invoking the <code>closeDocument</code> method. When you handle this event, you have a chance to
   *  prevent it from closed by setAllowClosing to false.
   */
  public static final int DOCUMENT_COMPONENT_CLOSING = 6000;

  /**
   *  The "document closed" event. This event is delivered after the document component has been closed as the result
   *  of a call to the <code>closeDocument</code>.
   */
  public static final int DOCUMENT_COMPONENT_CLOSED = 6001;

  /**
   *  Constructs an <code>DocumentComponentEvent</code> object.
   *
   *  @param source the <code>DocumentComponent</code> object that originated the event
   *  @param id     an integer indicating the type of event
   */
  public DocumentComponentEvent(JDocumentComponent source, int id)
  {
    super(source, id);
  }

  /**
   *  Returns the originator of the event.
   *
   *  @return the document associate with this event
   */
  public JDocumentComponent getDocumentComponent()
  {
    return (JDocumentComponent) super.getSource();
  }
}
