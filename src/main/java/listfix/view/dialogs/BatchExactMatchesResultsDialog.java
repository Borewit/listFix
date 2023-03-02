

package listfix.view.dialogs;

import listfix.model.BatchRepair;
import listfix.model.BatchRepairItem;
import listfix.model.playlists.Playlist;
import listfix.view.IListFixGui;
import listfix.view.controls.JTransparentTextArea;
import listfix.view.controls.PlaylistEditCtrl;
import listfix.view.controls.PlaylistsList;
import listfix.view.support.DualProgressWorker;
import listfix.view.support.IPlaylistModifiedListener;
import listfix.view.support.ProgressWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * This is the dialog we display for an exact matches search on multiple playlists.
 */
public class BatchExactMatchesResultsDialog extends JDialog
{
  private JButton _btnBrowse;
  private JButton _btnSave;
  private JCheckBox _chkBackup;
  private PlaylistsList _pnlList;
  private JTextField _txtBackup;
  private PlaylistEditCtrl playlistEditCtrl1;
  private boolean _userCancelled = false;
  private static final Logger _logger = LogManager.getLogger(BatchExactMatchesResultsDialog.class);
  private final IListFixGui listFixGui;
  private final BatchRepair _batch;

  /**
   * Creates new form BatchExactMatchesResultsDialog
   */
  public BatchExactMatchesResultsDialog(Frame parent, boolean modal, BatchRepair batch, IListFixGui listFixGui)
  {
    super(parent, batch.getDescription(), modal);
    this.listFixGui = listFixGui;
    this._batch = batch;
    initComponents();
    getRootPane().setDefaultButton(_btnSave);
    _txtBackup.setText(_batch.getDefaultBackupName());

    // load and repair lists
    final DualProgressDialog<Void, DualProgressWorker.ProgressItem<String>> pd = new DualProgressDialog<>(parent, "Finding Exact Matches...", "Please wait...", "Overall Progress:");
    DualProgressWorker<Void, String> dpw = new DualProgressWorker<>()
    {
      @Override
      protected void process(List<ProgressItem<String>> chunks)
      {
        ProgressItem<String> titem = new ProgressItem<>(true, -1, null);
        ProgressItem<String> oitem = new ProgressItem<>(false, -1, null);
        getEffectiveItems(chunks, titem, oitem);

        if (titem.percentComplete >= 0)
        {
          pd.getTaskProgressBar().setValue(titem.percentComplete);
        }
        if (titem.state != null)
        {
          pd.getTaskLabel().setText(titem.state);
        }
        if (oitem.percentComplete >= 0)
        {
          pd.getOverallProgressBar().setValue(oitem.percentComplete);
        }
        if (oitem.state != null)
        {
          pd.getOverallLabel().setText(oitem.state);
        }
      }

      @Override
      protected Void doInBackground()
      {
        _batch.performExactMatchRepair(this, BatchExactMatchesResultsDialog.this.listFixGui.getApplicationConfiguration().getAppOptions());
        return null;
      }
    };
    pd.show(dpw);

    if (!dpw.getCancelled())
    {
      ListSelectionModel lsm = _pnlList.getSelectionModel();
      lsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      lsm.addListSelectionListener(e -> {
        if (e.getValueIsAdjusting())
        {
          return;
        }
        updateSelectedPlaylist();
      });
      _pnlList.initPlaylistsList();

      for (BatchRepairItem item : _batch.getItems())
      {
        IPlaylistModifiedListener listener = this::onPlaylistModified;
        item.getPlaylist().addModifiedListener(listener);
      }

      String listCountTxt;
      if (_batch.getItems().size() == 1)
      {
        listCountTxt = "1 playlist";
      }
      else
      {
        listCountTxt = String.format("%d playlists", _batch.getItems().size());
      }
      _pnlList.setText(listCountTxt);
    }
    else
    {
      _userCancelled = true;
    }
  }

  private void onPlaylistModified(Playlist list)
  {
    _pnlList.playlistModified(list);
  }

  private void updateSelectedPlaylist()
  {
    // Keep the table anchored left...
    _pnlList.anchorLeft();
    int selIx = _pnlList.getSelectedModelRow();
    if (selIx >= 0)
    {
      BatchRepairItem item = _batch.getItem(selIx);
      playlistEditCtrl1.setPlaylist(item.getPlaylist());
    }
  }

  private void initComponents()
  {

    JPanel _backupPanel = new JPanel();
    _chkBackup = new JCheckBox();
    _txtBackup = new JTextField();
    JButton _btnCancel = new JButton();
    _btnSave = new JButton();
    _btnBrowse = new JButton();
    JSplitPane jSplitPane1 = new JSplitPane();
    JPanel jPanel1 = new JPanel();
    JPanel jPanel3 = new JPanel();
    JLabel jLabel1 = new JLabel();
    playlistEditCtrl1 = new PlaylistEditCtrl(this.listFixGui);
    _pnlList = new PlaylistsList(_batch);

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    addWindowListener(new WindowAdapter()
    {
      @Override public void windowClosing(WindowEvent evt)
      {
        formWindowClosing();
      }
    });

    _chkBackup.setText("Backup original files to zip file:");
    _chkBackup.addItemListener(this::onChkBackupItemStateChanged);

    _txtBackup.setEnabled(false);

    _btnCancel.setText("Cancel");
    _btnCancel.addActionListener(this::onBtnCancelActionPerformed);

    _btnSave.setText("Save All Repairs");
    _btnSave.addActionListener(this::onBtnSaveActionPerformed);

    _btnBrowse.setText("...");
    _btnBrowse.setEnabled(false);
    _btnBrowse.addActionListener(this::onBtnBrowseActionPerformed);

    GroupLayout _backupPanelLayout = new GroupLayout(_backupPanel);
    _backupPanel.setLayout(_backupPanelLayout);
    _backupPanelLayout.setHorizontalGroup(
      _backupPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(_backupPanelLayout.createSequentialGroup()
          .addContainerGap()
          .addComponent(_chkBackup)
          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(_txtBackup, GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(_btnBrowse)
          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 98, Short.MAX_VALUE)
          .addComponent(_btnSave)
          .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(_btnCancel)
          .addContainerGap())
    );
    _backupPanelLayout.setVerticalGroup(
      _backupPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(_backupPanelLayout.createSequentialGroup()
          .addContainerGap()
          .addGroup(_backupPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(_chkBackup)
            .addComponent(_txtBackup, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(_btnCancel)
            .addComponent(_btnSave)
            .addComponent(_btnBrowse))
          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    getContentPane().add(_backupPanel, BorderLayout.PAGE_END);

    jSplitPane1.setDividerLocation(184);
    jSplitPane1.setContinuousLayout(true);

    jPanel1.setLayout(new BorderLayout());

    jPanel3.setLayout(new FlowLayout(FlowLayout.LEFT));

    jLabel1.setText("Playlist details");
    jPanel3.add(jLabel1);

    jPanel1.add(jPanel3, BorderLayout.PAGE_START);

    playlistEditCtrl1.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    jPanel1.add(playlistEditCtrl1, BorderLayout.CENTER);

    jSplitPane1.setRightComponent(jPanel1);
    jSplitPane1.setLeftComponent(_pnlList);

    getContentPane().add(jSplitPane1, BorderLayout.CENTER);

    pack();
  }

  private void onBtnBrowseActionPerformed(ActionEvent evt)
  {
    JFileChooser dlg = new JFileChooser();
    if (!_txtBackup.getText().isEmpty())
    {
      dlg.setSelectedFile(new File(_txtBackup.getText()));
    }
    if (dlg.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
    {
      _txtBackup.setText(dlg.getSelectedFile().getAbsolutePath());
    }
  }

  private void onBtnSaveActionPerformed(ActionEvent evt)//GEN-FIRST:event_onBtnSaveActionPerformed
  {//GEN-HEADEREND:event_onBtnSaveActionPerformed
    ProgressWorker<Void, String> worker = new ProgressWorker<>()
    {
      @Override
      protected Void doInBackground() throws Exception
      {
        _batch.save(BatchExactMatchesResultsDialog.this.listFixGui.getApplicationConfiguration().getAppOptions(), false, _chkBackup.isSelected(), _txtBackup.getText(), this);
        return null;
      }
    };
    ProgressDialog pd = new ProgressDialog(null, true, worker, "Saving playlists...");
    pd.setVisible(true);

    try
    {
      worker.get();
    }
    catch (InterruptedException ignore)
    {
      // ignore, these happen when people cancel - should not be logged either.
    }
    catch (ExecutionException eex)
    {
      Throwable ex = eex.getCause();
      String msg = "An error occurred while saving: " + ex.getMessage();
      JOptionPane.showMessageDialog(BatchExactMatchesResultsDialog.this, new JTransparentTextArea(msg), "Save Error", JOptionPane.ERROR_MESSAGE);
      _logger.error(eex);
      return;
    }

    setVisible(false);
  }

  private void onBtnCancelActionPerformed(ActionEvent evt)
  {
    _userCancelled = true;
    setVisible(false);
  }

  private void onChkBackupItemStateChanged(ItemEvent evt)
  {
    boolean isChecked = _chkBackup.isSelected();
    _txtBackup.setEnabled(isChecked);
    _btnBrowse.setEnabled(isChecked);
    if (isChecked)
    {
      _txtBackup.selectAll();
      _txtBackup.requestFocusInWindow();
    }
  }

  private void formWindowClosing()
  {
    _userCancelled = true;
  }

  public boolean getUserCancelled()
  {
    return _userCancelled;
  }
}
