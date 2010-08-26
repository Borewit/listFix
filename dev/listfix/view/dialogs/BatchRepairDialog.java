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

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import listfix.controller.GUIDriver;
import listfix.model.BatchRepair;
import listfix.model.BatchRepairItem;
import listfix.model.Playlist;
import listfix.view.support.DualProgressWorker;
import listfix.view.support.FontHelper;
import listfix.view.support.IPlaylistModifiedListener;
import listfix.view.support.ProgressWorker;

public class BatchRepairDialog extends javax.swing.JDialog
{
	/** Creates new form BatchRepairDialog */
	public BatchRepairDialog(java.awt.Frame parent, boolean modal, BatchRepair batch)
	{
		super(parent, batch.getDescription(), modal);
		//super(parent, modal);
		_batch = batch;
		initComponents();
		playlistEditCtrl1.suppressOpenButton();
		getRootPane().setDefaultButton(_btnSave);
		_txtBackup.setText(_batch.getDefaultBackupName());

		_uiLists.setShowHorizontalLines(false);
		_uiLists.setShowVerticalLines(false);
		_uiLists.getTableHeader().setFont(new Font("Verdana", 0, 9));

		// load and repair lists
		final DualProgressDialog pd = new DualProgressDialog(parent, "Please wait...", "Loading Batch Repairs...");
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
				_batch.load(this);
				return null;
			}
		};
		pd.show(dpw);

		InitPlaylistsList();

		for (BatchRepairItem item : batch.getItems())
		{
			item.getPlaylist().addModifiedListener(listener);
		}

		String listCountTxt;
		if (batch.getItems().size() == 1)
		{
			listCountTxt = "1 playlist";
		}
		else
		{
			listCountTxt = String.format("%d playlists", batch.getItems().size());
		}
		_labListCount.setText(listCountTxt);


	}

	private final IPlaylistModifiedListener listener = new IPlaylistModifiedListener()
	{
		public void playlistModified(Playlist list)
		{
			onPlaylistModified(list);
		}
	};

	private void InitPlaylistsList()
	{
		_uiLists.setFont(new Font("Verdana", 0, 9));
		_uiLists.initFillColumnForScrollPane(_uiScrollLists);

		_uiLists.autoResizeColumn(0);
		_uiLists.autoResizeColumn(1);
		_uiLists.autoResizeColumn(2);

		// selections
		_uiLists.setColumnSelectionAllowed(false);
		_uiLists.setCellSelectionEnabled(false);
		_uiLists.setRowSelectionAllowed(true);
		ListSelectionModel lsm = _uiLists.getSelectionModel();
		lsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lsm.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
				{
					return;
				}
				UpdateSelectedPlaylist();
			}
		});

		// sort playlists by filename
		RowSorter sorter = _uiLists.getRowSorter();
		List<RowSorter.SortKey> keys = new ArrayList<RowSorter.SortKey>();
		keys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sorter.setSortKeys(keys);
	}

	private void onPlaylistModified(Playlist list)
	{
		int uiIndex = _uiLists.getSelectedRow();
		int selIx = _uiLists.getSelectedRow();
		if (selIx >= 0)
		{
			selIx = _uiLists.convertRowIndexToModel(selIx);
			BatchRepairItem item = _batch.getItem(selIx);
			try
			{
				((PlaylistsTableModel) _uiLists.getModel()).fireTableDataChanged();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			_uiLists.setRowSelectionInterval(uiIndex, uiIndex);
		}
	}

	private void UpdateSelectedPlaylist()
	{
		int selIx = _uiLists.getSelectedRow();
		if (selIx >= 0)
		{
			selIx = _uiLists.convertRowIndexToModel(selIx);
			BatchRepairItem item = _batch.getItem(selIx);
			playlistEditCtrl1.setPlaylist(item.getPlaylist());
		}
		else
		{
			playlistEditCtrl1.setPlaylist(null);
		}
	}
	BatchRepair _batch;

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        _chkBackup = new javax.swing.JCheckBox();
        _txtBackup = new javax.swing.JTextField();
        _btnCancel = new javax.swing.JButton();
        _btnSave = new javax.swing.JButton();
        _btnBrowse = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        playlistEditCtrl1 = new listfix.view.PlaylistEditCtrl();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        _labListCount = new javax.swing.JLabel();
        _uiScrollLists = new javax.swing.JScrollPane();
        _uiLists = new listfix.view.support.ZebraJTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        _chkBackup.setFont(new java.awt.Font("Verdana", 0, 9));
        _chkBackup.setText("Backup original files to zip file:");
        _chkBackup.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                onChkBackupItemStateChanged(evt);
            }
        });

        _txtBackup.setFont(new java.awt.Font("Verdana", 0, 9));
        _txtBackup.setEnabled(false);

        _btnCancel.setText("Cancel");
        _btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBtnCancelActionPerformed(evt);
            }
        });

        _btnSave.setFont(new java.awt.Font("Verdana", 0, 9));
        _btnSave.setText("Save Repairs");
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(_chkBackup)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_txtBackup, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_btnBrowse)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 104, Short.MAX_VALUE)
                .addComponent(_btnSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(_btnCancel)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_chkBackup)
                    .addComponent(_txtBackup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_btnCancel)
                    .addComponent(_btnSave)
                    .addComponent(_btnBrowse))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);

        jSplitPane1.setDividerLocation(184);
        jSplitPane1.setContinuousLayout(true);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setFont(new java.awt.Font("Verdana", 0, 9));
        jLabel1.setText("Playlist details");
        jPanel3.add(jLabel1);

        jPanel1.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        playlistEditCtrl1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel1.add(playlistEditCtrl1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(jPanel1);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel5.setFont(new java.awt.Font("Verdana", 0, 9));
        jPanel5.setLayout(new java.awt.GridBagLayout());

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 9));
        jLabel2.setText("Playlists");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        jPanel5.add(jLabel2, gridBagConstraints);

        _labListCount.setForeground(javax.swing.UIManager.getDefaults().getColor("controlShadow"));
        _labListCount.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        _labListCount.setText("0 lists");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jPanel5.add(_labListCount, gridBagConstraints);

        jPanel4.add(jPanel5, java.awt.BorderLayout.PAGE_START);

        _uiScrollLists.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        _uiLists.setAutoCreateRowSorter(true);
        _uiLists.setModel(new PlaylistsTableModel());
        _uiLists.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        _uiLists.setFont(new java.awt.Font("Verdana", 0, 9));
        _uiLists.getTableHeader().setReorderingAllowed(false);
        _uiScrollLists.setViewportView(_uiLists);

        jPanel4.add(_uiScrollLists, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel4);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void onBtnBrowseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnBrowseActionPerformed
    {//GEN-HEADEREND:event_onBtnBrowseActionPerformed
		JFileChooser dlg = new JFileChooser();
		if (!_txtBackup.getText().isEmpty())
		{
			dlg.setSelectedFile(new File(_txtBackup.getText()));
			FontHelper.setFileChooserFont(dlg.getComponents());
		}
		if (dlg.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			_txtBackup.setText(dlg.getSelectedFile().getAbsolutePath());
			FontHelper.setFileChooserFont(dlg.getComponents());
		}
    }//GEN-LAST:event_onBtnBrowseActionPerformed

    private void onBtnSaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnSaveActionPerformed
    {//GEN-HEADEREND:event_onBtnSaveActionPerformed
		ProgressWorker<Void, Void> worker = new ProgressWorker<Void, Void>()
		{
			@Override
			protected Void doInBackground() throws IOException
			{
				boolean saveRelative = GUIDriver.getInstance().getAppOptions().getSavePlaylistsWithRelativePaths();
				_batch.save(saveRelative, _chkBackup.isSelected(), _txtBackup.getText(), this);
				return null;
			}
		};
		ProgressDialog pd = new ProgressDialog(null, true, worker, "Loading playlists...");
		pd.setVisible(true);

		try
		{
			worker.get();
		}
		catch (InterruptedException ex)
		{
			// ignore
		}
		catch (ExecutionException eex)
		{
			Throwable ex = eex.getCause();
			String msg = "An error occurred while saving:\n" + ex.getMessage();
			JOptionPane.showMessageDialog(BatchRepairDialog.this, msg, "Save Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		setVisible(false);
    }//GEN-LAST:event_onBtnSaveActionPerformed

    private void onBtnCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnCancelActionPerformed
    {//GEN-HEADEREND:event_onBtnCancelActionPerformed
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
	private static ImageIcon _imgMissing = new ImageIcon(BatchRepairDialog.class.getResource("/images/icon-missing.gif"));
	private static ImageIcon _imgFound = new ImageIcon(BatchRepairDialog.class.getResource("/images/icon-found.gif"));
	private static ImageIcon _imgFixed = new ImageIcon(BatchRepairDialog.class.getResource("/images/icon-fixed.gif"));

	private class PlaylistsTableModel extends AbstractTableModel
	{
		public PlaylistsTableModel()
		{
			_items = _batch.getItems();
		}

		public int getRowCount()
		{
			return _items.size();
		}

		public int getColumnCount()
		{
			return 4;
		}

		@Override
		public String getColumnName(int column)
		{
			switch (column)
			{
				case 0:
					return "";
				case 1:
					return "Name";
				case 2:
					return "Location";
				default:
					return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			if (columnIndex == 0)
			{
				return ImageIcon.class;
			}
			else
			{
				return Object.class;
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			BatchRepairItem item = _items.get(rowIndex);
			switch (columnIndex)
			{
				case 0:
					Playlist list = item.getPlaylist();
					if (list.getMissingCount() > 0)
					{
						return _imgMissing; // red
					}
					else if (list.getFixedCount() > 0)
					{
						return _imgFixed; // light green
					}
					else
					{
						return _imgFound; // dark green
					}
				case 1:
					return item.getDisplayName();

				case 2:
					return item.getPath();

				default:
					return null;
			}
		}
		private List<BatchRepairItem> _items;
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _btnBrowse;
    private javax.swing.JButton _btnCancel;
    private javax.swing.JButton _btnSave;
    private javax.swing.JCheckBox _chkBackup;
    private javax.swing.JLabel _labListCount;
    private javax.swing.JTextField _txtBackup;
    private listfix.view.support.ZebraJTable _uiLists;
    private javax.swing.JScrollPane _uiScrollLists;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSplitPane jSplitPane1;
    private listfix.view.PlaylistEditCtrl playlistEditCtrl1;
    // End of variables declaration//GEN-END:variables
}
