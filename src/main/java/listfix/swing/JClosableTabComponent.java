package listfix.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Component to be used as tabComponent; Contains a JLabel to show the text and a JButton to close
 * the tab it belongs to
 */
public class JClosableTabComponent extends JPanel {
  private final JDocumentTabbedPane pane;

  private final JLabel label;

  public JClosableTabComponent(final JDocumentTabbedPane pane) {
    // unset default FlowLayout' gaps
    super(new FlowLayout(FlowLayout.LEFT, 0, 0));
    if (pane == null) {
      throw new NullPointerException("TabbedPane is null");
    }
    this.pane = pane;
    setOpaque(false);

    label = new JLabel();
    // add more space between the label and the button
    this.label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
    add(this.label);
    // tab button
    JButton button = new TabButton();
    add(button);
    // add more space to the top of the component
    setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
  }

  public ImageIcon getIcon() {
    return (ImageIcon) this.label.getIcon();
  }

  public void setIcon(ImageIcon icon) {
    this.label.setIcon(icon);
  }

  public void setTitle(String title) {
    super.setName(title);
    this.label.setText(title);
  }

  public void setTooltip(String text) {
    super.setToolTipText(text);
  }

  private class TabButton extends JButton implements ActionListener {
    TabButton() {
      int size = 17;
      setPreferredSize(new Dimension(size, size));
      setToolTipText("Close this tab");
      // Make the button looks the same for all Laf's
      setUI(new BasicButtonUI());
      // Make it transparent
      setContentAreaFilled(false);
      // No need to be focusable
      setFocusable(false);
      setBorder(BorderFactory.createEtchedBorder());
      setBorderPainted(false);
      // Making nice rollover effect
      // we use the same listener for all buttons
      addMouseListener(buttonMouseListener);
      setRolloverEnabled(true);
      // Close the proper tab by clicking the button
      addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int i = pane.indexOfTabComponent(JClosableTabComponent.this);
      if (i != -1) {
        pane.tryToClose(i);
      }
    }

    // we don't want to update UI for this button
    @Override
    public void updateUI() {}

    // paint the cross
    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        // shift the image for pressed buttons
        if (getModel().isPressed()) {
          g2.translate(1, 1);
        }
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.BLACK);
        if (getModel().isRollover()) {
          g2.setColor(Color.BLUE);
        }
        int delta = 6;
        g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
        g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
        g2.dispose();
      } finally {
        g2.dispose();
      }
    }
  }

  private static final MouseListener buttonMouseListener =
      new MouseAdapter() {
        @Override
        public void mouseExited(MouseEvent e) {
          Component component = e.getComponent();
          if (component instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) component;
            button.setBorderPainted(false);
          }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
          Component component = e.getComponent();
          if (component instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) component;
            button.setBorderPainted(true);
          }
        }
      };
}
