package listfix.view.controls;

import javax.swing.*;

public class JTransparentTextArea extends JTextArea
{
  /**
   *
   * @param msg
   */
  public JTransparentTextArea(String msg)
  {
    super(msg);
    this.setOpaque(false);
    this.setEditable(false);
  }
}
