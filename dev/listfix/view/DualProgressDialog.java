package listfix.view;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class DualProgressDialog extends JDialog
{
    public DualProgressDialog(Frame parent, String taskMsg, String overallMsg)
    {
        super(parent, true);
        
        Container pane = getContentPane();
        GridBagConstraints gbc;
        
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        pane.setLayout(new java.awt.GridBagLayout());

        
        _taskLabel.setText(taskMsg);
        gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new java.awt.Insets(10, 10, 4, 10);
        pane.add(_taskLabel, gbc);

        _taskProgress.setMinimumSize(new java.awt.Dimension(250, 14));
        gbc = new java.awt.GridBagConstraints();
        gbc.gridy = 1;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(0, 10, 0, 10);
        pane.add(_taskProgress, gbc);

        _overallLabel.setText(overallMsg);
        gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridy = 2;
        gbc.insets = new java.awt.Insets(4, 10, 4, 10);
        pane.add(_overallLabel, gbc);

        _overallProgress.setMinimumSize(new java.awt.Dimension(250, 14));
        gbc = new java.awt.GridBagConstraints();
        gbc.gridy = 3;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(0, 10, 10, 10);
        pane.add(_overallProgress, gbc);
        
        pack();

        setSize(400, getHeight());
        setLocationRelativeTo(parent);

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowOpened(WindowEvent e)
            {
                _worker.execute();
            }
        });

    }
    
    public void show(SwingWorker worker)
    {
        _worker = worker;
        
        PropertyChangeSupport pcs = _worker.getPropertyChangeSupport();
        
        // close dialog when state changes to done
        pcs.addPropertyChangeListener("state", new PropertyChangeListener() 
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (_worker.isDone())
                {
                    setVisible(false);
                }
            }
        });
        
        
        setVisible(true);
    }
    
    public JLabel getTaskLabel()
    {
        return _taskLabel;
    }
    
    public JProgressBar getTaskProgressBar()
    {
        return _taskProgress;
    }

    public JLabel getOverallLabel()
    {
        return _overallLabel;
    }

    public JProgressBar getOverallProgressBar()
    {
        return _overallProgress;
    }

    SwingWorker _worker;

    // controls
    JLabel _taskLabel = new JLabel();
    JProgressBar _taskProgress = new JProgressBar();
    JLabel _overallLabel = new JLabel();
    JProgressBar _overallProgress = new JProgressBar();
}
