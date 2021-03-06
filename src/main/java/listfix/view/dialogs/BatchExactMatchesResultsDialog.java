/*
 * listFix() - Fix Broken Playlists!
 *
 * This file is part of listFix().
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.view.dialogs;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import listfix.controller.GUIDriver;
import listfix.model.BatchRepair;
import listfix.model.BatchRepairItem;
import listfix.model.playlists.Playlist;
import listfix.util.ExStack;
import listfix.view.controls.JTransparentTextArea;
import listfix.view.controls.PlaylistsList;
import listfix.view.support.DualProgressWorker;
import listfix.view.support.IPlaylistModifiedListener;
import listfix.view.support.ProgressWorker;

import org.apache.log4j.Logger;

/**
 * This is the dialog we display for an exact matches search on multiple playlists.
 * @author jcaron
 */
public class BatchExactMatchesResultsDialog extends javax.swing.JDialog
{
  private boolean _userCancelled = false;
  private static final Logger _logger = Logger.getLogger(BatchExactMatchesResultsDialog.class);

  /** Creates new form BatchExactMatchesResultsDialog
   * @param parent
   * @param batch
   * @param modal
   */
  public BatchExactMatchesResultsDialog(java.awt.Frame parent, boolean modal, BatchRepair batch)
  {
    super(parent, batch.getDescription(), modal);
    //super(parent, modal);
    _batch = batch;
    initComponents();
    getRootPane().setDefaultButton(_btnSave);
    _txtBackup.setText(_batch.getDefaultBackupName());

    // load and repair lists
    final DualProgressDialog pd = new DualProgressDialog(parent, "Finding Exact Matches...", "Please wait...", "Overall Progress:");
    DualProgressWorker dpw = new DualProgressWorker<Void, String>()
    {
      @Override
      protected void process(List<ProgressItem<String>> chunks)
      {
        ProgressItem<String> titem = new ProgressItem<String>(true, -1, null);
        ProgressItem<String> oitem = new ProgressItem<String>(false, -1, null);
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
      protected Void doInBackground() throws Exception
      {
        _batch.performExactMatchRepair(this);
        return null;
      }
    };
    pd.show(dpw);

    if (!dpw.getCancelled())
    {
      ListSelectionModel lsm = _pnlList.getSelectionModel();
      lsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      lsm.addListSelectionListener(new ListSelectionListener()
      {
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
          if (e.getValueIsAdjusting())
          {
            return;
          }
          updateSelectedPlaylist();
        }
      });
      _pnlList.initPlaylistsList();

      for (BatchRepairItem item : _batch.getItems())
      {
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

  private final IPlaylistModifiedListener listener = new IPlaylistModifiedListener()
  {
    @Override
    public void playlistModified(Playlist list)
    {
      onPlaylistModified(list);
    }
  };

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

  private BatchRepair _batch;

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _backupPanel = new javax.swing.JPanel();
        _chkBackup = new javax.swing.JCheckBox();
        _txtBackup = new javax.swing.JTextField();
        _btnCancel = new javax.swing.JButton();
        _btnSave = new javax.swing.JButton();
        _btnBrowse = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        playlistEditCtrl1 = new listfix.view.controls.PlaylistEditCtrl();
        _pnlList = new PlaylistsList(_batch);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        _chkBackup.setText("Backup original files to zip file:");
        _chkBackup.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                onChkBackupItemStateChanged(evt);
            }
        });

        _txtBackup.setEnabled(false);

        _btnCancel.setText("Cancel");
        _btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBtnCancelActionPerformed(evt);
            }
        });

        _btnSave.setText("Save All Repairs");
        _btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBtnSaveActionPerformed(evt);
            }
        });

        _btnBrowse.setText("...");
        _btnBrowse.setEnabled(false);
        _btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBtnBrowseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout _backupPanelLayout = new javax.swing.GroupLayout(_backupPanel);
        _backupPanel.setLayout(_backupPanelLayout);
        _backupPanelLayout.setHorizontalGroup(
            _backupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_backupPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(_chkBackup)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_txtBackup, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_btnBrowse)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 98, Short.MAX_VALUE)
                .addComponent(_btnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_btnCancel)
                .addContainerGap())
        );
        _backupPanelLayout.setVerticalGroup(
            _backupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(_backupPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(_backupPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_chkBackup)
                    .addComponent(_txtBackup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_btnCancel)
                    .addComponent(_btnSave)
                    .addComponent(_btnBrowse))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(_backupPanel, java.awt.BorderLayout.PAGE_END);

        jSplitPane1.setDividerLocation(184);
        jSplitPane1.setContinuousLayout(true);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText("Playlist details");
        jPanel3.add(jLabel1);

        jPanel1.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        playlistEditCtrl1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel1.add(playlistEditCtrl1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(jPanel1);
        jSplitPane1.setLeftComponent(_pnlList);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void onBtnBrowseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnBrowseActionPerformed
    {//GEN-HEADEREND:event_onBtnBrowseActionPerformed
    JFileChooser dlg = new JFileChooser();
    if (!_txtBackup.getText().isEmpty())
    {
      dlg.setSelectedFile(new File(_txtBackup.getText()));
    }
    if (dlg.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
    {
      _txtBackup.setText(dlg.getSelectedFile().getAbsolutePath());
    }
    }//GEN-LAST:event_onBtnBrowseActionPerformed

    private void onBtnSaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnSaveActionPerformed
    {//GEN-HEADEREND:event_onBtnSaveActionPerformed
    ProgressWorker<Void, Void> worker = new ProgressWorker<Void, Void>()
    {
      @Override
      protected Void doInBackground() throws Exception
      {
        boolean saveRelative = GUIDriver.getInstance().getAppOptions().getSavePlaylistsWithRelativePaths();
        _batch.save(saveRelative, false, _chkBackup.isSelected(), _txtBackup.getText(), this);
        return null;
      }
    };
    ProgressDialog pd = new ProgressDialog(null, true, worker, "Saving playlists...");
    pd.setVisible(true);

    try
    {
      worker.get();
    }
    catch (InterruptedException ex)
    {
      // ignore, these happen when people cancel - should not be logged either.
    }
    catch (ExecutionException eex)
    {
      Throwable ex = eex.getCause();
      String msg = "An error occurred while saving: " + ex.getMessage();
      JOptionPane.showMessageDialog(BatchExactMatchesResultsDialog.this, new JTransparentTextArea(msg), "Save Error", JOptionPane.ERROR_MESSAGE);
      _logger.error(ExStack.toString(eex));
      return;
    }

    setVisible(false);
    }//GEN-LAST:event_onBtnSaveActionPerformed

    private void onBtnCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnCancelActionPerformed
    {//GEN-HEADEREND:event_onBtnCancelActionPerformed
    _userCancelled = true;
    setVisible(false);
    }//GEN-LAST:event_onBtnCancelActionPerformed

    private void onChkBackupItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_onChkBackupItemStateChanged
    {//GEN-HEADEREND:event_onChkBackupItemStateChanged
    boolean isChecked = _chkBackup.isSelected();
    _txtBackup.setEnabled(isChecked);
    _btnBrowse.setEnabled(isChecked);
    if (isChecked)
    {
      _txtBackup.selectAll();
      _txtBackup.requestFocusInWindow();
    }
    }//GEN-LAST:event_onChkBackupItemStateChanged

  private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
  {//GEN-HEADEREND:event_formWindowClosing
    _userCancelled = true;
  }//GEN-LAST:event_formWindowClosing

  /**
   * @return the _userCancelled
   */
  public boolean isUserCancelled()
  {
    return getUserCancelled();
  }

  /**
   * @param userCancelled the _userCancelled to set
   */
  public void setUserCancelled(boolean userCancelled)
  {
    this._userCancelled = userCancelled;
  }

  /**
   * @return the _userCancelled
   */
  public boolean getUserCancelled()
  {
    return _userCancelled;
  }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel _backupPanel;
    private javax.swing.JButton _btnBrowse;
    private javax.swing.JButton _btnCancel;
    private javax.swing.JButton _btnSave;
    private javax.swing.JCheckBox _chkBackup;
    private listfix.view.controls.PlaylistsList _pnlList;
    private javax.swing.JTextField _txtBackup;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSplitPane jSplitPane1;
    private listfix.view.controls.PlaylistEditCtrl playlistEditCtrl1;
    // End of variables declaration//GEN-END:variables
}
