/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2012 Jeremy Caron
 * 
 *  This file is part of listFix().
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, please see http://www.gnu.org/licenses/
 */

/*
 * MultiListBatchClosestMatchResultsDialog.java
 *
 * Created on Mar 28, 2011, 6:26:47 PM
 */

package listfix.view.dialogs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import listfix.controller.GUIDriver;
import listfix.model.BatchRepair;
import listfix.model.BatchRepairItem;
import listfix.model.Playlist;
import listfix.util.ExStack;
import listfix.view.controls.ClosestMatchesSearchScrollableResultsPanel;
import listfix.view.controls.JTransparentTextArea;
import listfix.view.controls.PlaylistsList;
import listfix.view.support.DualProgressWorker;
import listfix.view.support.IPlaylistModifiedListener;
import listfix.view.support.ProgressWorker;
import org.apache.log4j.Logger;

/**
 * This is the results dialog we display when running a batch closest matches search on all entries in multiple playlists.
 * @author jcaron
 */

public class MultiListBatchClosestMatchResultsDialog extends javax.swing.JDialog
{
	private BatchRepair _batch;
	private boolean _userCancelled = false;
	private boolean _userAccepted = false;
	private static final Logger _logger = Logger.getLogger(MultiListBatchClosestMatchResultsDialog.class);

    /** Creates new form MultiListBatchClosestMatchResultsDialog */
    public MultiListBatchClosestMatchResultsDialog(java.awt.Frame parent, boolean modal)
	{
        super(parent, modal);
        initComponents();
    }

	public MultiListBatchClosestMatchResultsDialog(java.awt.Frame parent, boolean modal, BatchRepair br)
	{
		super(parent, br.getDescription(), modal);
		_batch = br;
		initComponents();

		getRootPane().setDefaultButton(_btnSave);
		_txtBackup.setText(_batch.getDefaultBackupName());

		// load and repair lists
		final DualProgressDialog pd = new DualProgressDialog(parent, "Finding Closest Matches...", "Please wait...", "Overall Progress:");
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
				_batch.performClosestMatchRepair(this);
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
			_pnlResults.setResults(item.getClosestMatches());
		}
	}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        _pnlResults = new ClosestMatchesSearchScrollableResultsPanel();
        _pnlList = new PlaylistsList(_batch);
        _pnlBackup = new javax.swing.JPanel();
        _pnlRight = new javax.swing.JPanel();
        _btnSave = new javax.swing.JButton();
        _btnCancel = new javax.swing.JButton();
        _pnlLeft = new javax.swing.JPanel();
        _chkBackup = new javax.swing.JCheckBox();
        _txtBackup = new javax.swing.JTextField();
        _btnBrowse = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jSplitPane1.setDividerLocation(150);
        jSplitPane1.setRightComponent(_pnlResults);
        jSplitPane1.setLeftComponent(_pnlList);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        _pnlBackup.setLayout(new java.awt.BorderLayout());

        _btnSave.setText("Save All Repairs");
        _btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnSaveActionPerformed(evt);
            }
        });
        _pnlRight.add(_btnSave);

        _btnCancel.setText("Cancel");
        _btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnCancelActionPerformed(evt);
            }
        });
        _pnlRight.add(_btnCancel);

        _pnlBackup.add(_pnlRight, java.awt.BorderLayout.EAST);

        _chkBackup.setText("Backup original files to zip file:");
        _chkBackup.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                _chkBackuponChkBackupItemStateChanged(evt);
            }
        });
        _pnlLeft.add(_chkBackup);

        _txtBackup.setEnabled(false);
        _txtBackup.setPreferredSize(new java.awt.Dimension(150, 20));
        _pnlLeft.add(_txtBackup);

        _btnBrowse.setText("...");
        _btnBrowse.setEnabled(false);
        _btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnBrowseActionPerformed(evt);
            }
        });
        _pnlLeft.add(_btnBrowse);

        _pnlBackup.add(_pnlLeft, java.awt.BorderLayout.WEST);

        getContentPane().add(_pnlBackup, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
	{//GEN-HEADEREND:event_formWindowClosing
		_userCancelled = true;
	}//GEN-LAST:event_formWindowClosing

	private void _btnSaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__btnSaveActionPerformed
	{//GEN-HEADEREND:event__btnSaveActionPerformed
		_userAccepted = true;
		
		if (_pnlResults.getSelectedRow() > -1 && _pnlResults.getSelectedColumn() == 3)
		{
			TableCellEditor cellEditor = _pnlResults.getCellEditor(_pnlResults.getSelectedRow(), _pnlResults.getSelectedColumn());
			cellEditor.stopCellEditing();
		}
		
		ProgressWorker<Void, Void> worker = new ProgressWorker<Void, Void>()
		{
			@Override
			protected Void doInBackground() throws IOException
			{
				boolean saveRelative = GUIDriver.getInstance().getAppOptions().getSavePlaylistsWithRelativePaths();
				_batch.save(saveRelative, true, _chkBackup.isSelected(), _txtBackup.getText(), this);
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
			JOptionPane.showMessageDialog(MultiListBatchClosestMatchResultsDialog.this, new JTransparentTextArea(msg), "Save Error", JOptionPane.ERROR_MESSAGE);
			_logger.error(ExStack.toString(ex));
			return;
		}
		
		setVisible(false);
	}//GEN-LAST:event__btnSaveActionPerformed

	private void _btnCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__btnCancelActionPerformed
	{//GEN-HEADEREND:event__btnCancelActionPerformed
		_userCancelled = true;
		setVisible(false);
	}//GEN-LAST:event__btnCancelActionPerformed

	private void _chkBackuponChkBackupItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event__chkBackuponChkBackupItemStateChanged
	{//GEN-HEADEREND:event__chkBackuponChkBackupItemStateChanged
		boolean isChecked = _chkBackup.isSelected();
		_txtBackup.setEnabled(isChecked);
		_btnBrowse.setEnabled(isChecked);
		if (isChecked)
		{
			_txtBackup.selectAll(); 
			_txtBackup.requestFocusInWindow();
		}
 	}//GEN-LAST:event__chkBackuponChkBackupItemStateChanged

	private void _btnBrowseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__btnBrowseActionPerformed
	{//GEN-HEADEREND:event__btnBrowseActionPerformed
		JFileChooser dlg = new JFileChooser(); 		
		if (!_txtBackup.getText().isEmpty()) 		
		{ 			
			dlg.setSelectedFile(new File(_txtBackup.getText())); 		
		} 		
		if (dlg.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) 		
		{ 			
			_txtBackup.setText(dlg.getSelectedFile().getAbsolutePath()); 		
		} 	
	}//GEN-LAST:event__btnBrowseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _btnBrowse;
    private javax.swing.JButton _btnCancel;
    private javax.swing.JButton _btnSave;
    private javax.swing.JCheckBox _chkBackup;
    private javax.swing.JPanel _pnlBackup;
    private javax.swing.JPanel _pnlLeft;
    private listfix.view.controls.PlaylistsList _pnlList;
    private listfix.view.controls.ClosestMatchesSearchScrollableResultsPanel _pnlResults;
    private javax.swing.JPanel _pnlRight;
    private javax.swing.JTextField _txtBackup;
    private javax.swing.JSplitPane jSplitPane1;
    // End of variables declaration//GEN-END:variables

	/**
	 * @param userCancelled the _userCancelled to set
	 */
	public void setUserCancelled(boolean userCancelled)
	{
		_userCancelled = userCancelled;
	}

	public boolean getUserCancelled()
	{
		return _userCancelled;
	}

	/**
	 * @return the _userAccepted
	 */ 
	public boolean isUserAccepted()
	{
		return _userAccepted;
	}

	/**
	 * @param userAccepted the _userAccepted to set
	 */ public void setUserAccepted(boolean userAccepted)
	{
		this._userAccepted = userAccepted;
	}

}
