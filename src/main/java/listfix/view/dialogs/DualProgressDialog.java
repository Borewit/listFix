package listfix.view.dialogs;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeSupport;
import javax.swing.*;

/**
 * Dialog displaying double progressbar to show progress of nested background tasks.
 *
 * @param <T> the result type returned by this SwingWorker's doInBackground and get methods
 * @param <V> the type used for carrying out intermediate results by this SwingWorker's publish and
 *     process methods
 */
public class DualProgressDialog<T, V> extends JDialog {
  private SwingWorker<T, V> _worker;
  private JLabel _overallLabel;
  private JProgressBar _overallProgress;
  private JLabel _taskLabel;
  private JProgressBar _taskProgress;

  public DualProgressDialog(Frame parent, String title, boolean modal) {
    super(parent, title, modal);
    initComponents();
  }

  public DualProgressDialog(Frame parent, String title, String taskMsg, String overallMsg) {
    this(parent, title, true);
    _taskLabel.setText(taskMsg);
    _overallLabel.setText(overallMsg);
    pack();
    setSize(400, getHeight());
    setLocationRelativeTo(parent);
    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowOpened(WindowEvent e) {
            _worker.execute();
          }
        });
  }

  public void show(SwingWorker<T, V> worker) {
    _worker = worker;

    PropertyChangeSupport pcs = _worker.getPropertyChangeSupport();

    // close dialog when state changes to done
    pcs.addPropertyChangeListener(
        "state",
        evt -> {
          if (_worker.isDone()) {
            setVisible(false);
          }
        });

    setVisible(true);
  }

  /** This method is called from within the constructor to initialize the form. */
  private void initComponents() {
    GridBagConstraints gridBagConstraints;

    JPanel _middlePanel = new JPanel();
    _taskLabel = new JLabel();
    _taskProgress = new JProgressBar();
    _overallLabel = new JLabel();
    _overallProgress = new JProgressBar();
    // Variables declaration - do not modify//GEN-BEGIN:variables
    JPanel _bottomPanel = new JPanel();
    JButton _cancelButton = new JButton();

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent evt) {
            formWindowClosing();
          }
        });

    _middlePanel.setLayout(new GridBagLayout());

    _taskLabel.setText("Task Progress");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(10, 10, 4, 10);
    _middlePanel.add(_taskLabel, gridBagConstraints);

    _taskProgress.setMinimumSize(new Dimension(250, 14));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(0, 10, 10, 10);
    _middlePanel.add(_taskProgress, gridBagConstraints);

    _overallLabel.setText("Overall Progress");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(10, 10, 4, 10);
    _middlePanel.add(_overallLabel, gridBagConstraints);

    _overallProgress.setMinimumSize(new Dimension(250, 14));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(0, 10, 10, 10);
    _middlePanel.add(_overallProgress, gridBagConstraints);

    getContentPane().add(_middlePanel, BorderLayout.CENTER);

    _bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    _cancelButton.setText("Cancel");
    _cancelButton.addActionListener(this::_cancelButtonActionPerformed);
    _cancelButton.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent evt) {
            _cancelButtonKeyPressed(evt);
          }
        });
    _bottomPanel.add(_cancelButton);

    getContentPane().add(_bottomPanel, BorderLayout.SOUTH);

    pack();
  }

  private void _cancelButtonActionPerformed(ActionEvent ignore) {
    _worker.cancel(true);
  }

  private void formWindowClosing() {
    _worker.cancel(true);
  }

  private void _cancelButtonKeyPressed(KeyEvent evt) {
    if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
      _worker.cancel(true);
    }
  }

  public JLabel getTaskLabel() {
    return _taskLabel;
  }

  public JProgressBar getTaskProgressBar() {
    return _taskProgress;
  }

  public JLabel getOverallLabel() {
    return _overallLabel;
  }

  public JProgressBar getOverallProgressBar() {
    return _overallProgress;
  }
}
