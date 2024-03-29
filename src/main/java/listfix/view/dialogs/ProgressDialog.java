package listfix.view.dialogs;

import listfix.view.support.ProgressWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


public class ProgressDialog extends JDialog
{

  public ProgressDialog(Frame parent, boolean modal, ProgressWorker worker, String label)
  {
    this(parent, modal, worker, label, false, true);
  }


  public ProgressDialog(Frame parent, boolean modal, ProgressWorker worker, String label, boolean textMode, boolean canCancel)
  {
    super(parent, modal);
    initComponents();

    Dimension sz = getSize();
    sz.width = 400;
    setSize(sz);

    initWorker(worker);

    _progressTitle.setText(label);

    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowOpened(WindowEvent e)
      {
        _worker.execute();
      }
    });

    if (textMode)
    {
      _progressBar.setVisible(false);
      _pnlMessage.setVisible(true);

      sz.width = 500;
      sz.height = 120;
      setSize(sz);
    }
    else
    {
      _progressBar.setVisible(true);
      _pnlMessage.setVisible(false);
    }

    if (!canCancel)
    {
      _cancelButton.setVisible(false);
      sz.width = 400;
      sz.height = 80;
      setSize(sz);
    }

    setLocationRelativeTo(parent);
  }


  public JProgressBar getProgressBar()
  {
    return _progressBar;
  }


  public JLabel getProgressLabel()
  {
    return _progressTitle;
  }

  public void setMessage(String message)
  {
    _progressMessage.setText(message);
  }

  private void initWorker(ProgressWorker worker)
  {
    _worker = worker;
    PropertyChangeSupport pcs = _worker.getPropertyChangeSupport();

    // update progress bar when progress changes
    pcs.addPropertyChangeListener("progress", new PropertyChangeListener()
    {
      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        int progress = (Integer) evt.getNewValue();
        _progressBar.setValue(progress);
      }
    });

    // Update progress message when message changes
    pcs.addPropertyChangeListener("message", new PropertyChangeListener()
    {
      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        _progressMessage.setText(_worker.getMessage());
      }
    });

    // close dialog when state changes to done
    pcs.addPropertyChangeListener("state", new PropertyChangeListener()
    {
      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        if (_worker.isDone())
        {
          setVisible(false);
        }
      }
    });
  }

  ProgressWorker _worker;

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    GridBagConstraints gridBagConstraints;

    jPanel1 = new JPanel();
    _progressTitle = new JLabel();
    _progressBar = new JProgressBar();
    _pnlMessage = new JPanel();
    _progressMessage = new JLabel();
    _pnlSpacer = new JPanel();
    jPanel2 = new JPanel();
    _cancelButton = new JButton();

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setResizable(false);
    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent evt)
      {
        formWindowClosing();
      }
    });
    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

    jPanel1.setMinimumSize(new Dimension(270, 45));
    jPanel1.setPreferredSize(new Dimension(166, 45));
    jPanel1.setLayout(new GridBagLayout());

    _progressTitle.setText("Title");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(15, 10, 4, 10);
    jPanel1.add(_progressTitle, gridBagConstraints);

    _progressBar.setMinimumSize(new Dimension(250, 14));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(0, 10, 10, 10);
    jPanel1.add(_progressBar, gridBagConstraints);

    _pnlMessage.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5));

    _progressMessage.setText("Message");
    _pnlMessage.add(_progressMessage);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(0, 2, 0, 2);
    jPanel1.add(_pnlMessage, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    jPanel1.add(_pnlSpacer, gridBagConstraints);

    getContentPane().add(jPanel1);

    jPanel2.setMinimumSize(new Dimension(77, 30));
    jPanel2.setPreferredSize(new Dimension(77, 30));
    jPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));

    _cancelButton.setText("Cancel");
    _cancelButton.setAlignmentY(-5.0F);
    _cancelButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent evt)
      {
        _cancelButtonActionPerformed();
      }
    });
    _cancelButton.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        _cancelButtonKeyPressed(evt);
      }
    });
    jPanel2.add(_cancelButton);

    getContentPane().add(jPanel2);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void _cancelButtonActionPerformed()//GEN-FIRST:event__cancelButtonActionPerformed
  {//GEN-HEADEREND:event__cancelButtonActionPerformed
    _worker.cancel(true);
  }//GEN-LAST:event__cancelButtonActionPerformed

  private void _cancelButtonKeyPressed(KeyEvent evt)//GEN-FIRST:event__cancelButtonKeyPressed
  {//GEN-HEADEREND:event__cancelButtonKeyPressed
    if (evt.getKeyCode() == KeyEvent.VK_ESCAPE)
    {
      _worker.cancel(true);
    }
  }//GEN-LAST:event__cancelButtonKeyPressed

  private void formWindowClosing()//GEN-FIRST:event_formWindowClosing
  {//GEN-HEADEREND:event_formWindowClosing
    _worker.cancel(true);
  }//GEN-LAST:event_formWindowClosing


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JButton _cancelButton;
  private JPanel _pnlMessage;
  private JPanel _pnlSpacer;
  private JProgressBar _progressBar;
  private JLabel _progressMessage;
  private JLabel _progressTitle;
  private JPanel jPanel1;
  private JPanel jPanel2;
  // End of variables declaration//GEN-END:variables

}
