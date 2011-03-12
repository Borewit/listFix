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

package listfix.view;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;
import java.io.IOException;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import listfix.controller.GUIDriver;

import listfix.io.AudioFileFilter;
import listfix.io.PlaylistEntryFileCopier;
import listfix.io.PlaylistFileChooserFilter;

import listfix.model.BatchMatchItem;
import listfix.model.EditFilenameResult;
import listfix.model.MatchedPlaylistEntry;
import listfix.model.Playlist;
import listfix.model.PlaylistEntry;
import listfix.model.PlaylistEntryList;
import listfix.util.ArrayFunctions;

import listfix.view.dialogs.ReorderPlaylistDialog;
import listfix.view.dialogs.EditFilenameDialog;
import listfix.view.dialogs.BatchClosestMatchResultsDialog;
import listfix.view.dialogs.ProgressDialog;
import listfix.view.dialogs.BatchRepairDialog;
import listfix.view.dialogs.ClosestMatchChooserDialog;

import listfix.view.support.FontHelper;
import listfix.view.support.IPlaylistModifiedListener;
import listfix.view.support.ProgressWorker;
import listfix.view.support.ZebraJTable;

public class PlaylistEditCtrl extends javax.swing.JPanel
{
	private static final NumberFormat _intFormatter = NumberFormat.getIntegerInstance();
	private static final DataFlavor _playlistEntryListFlavor = new DataFlavor(PlaylistEntryList.class, "PlaylistEntyList");
	private static final DataFlavor _playlistFlavor = new DataFlavor(Playlist.class, "Playlist");
	int currentlySelectedRow = 0;

	public PlaylistEditCtrl()
	{
		initComponents();
		initPlaylistTable();
		SwingUtilities.updateComponentTreeUI(this);
	}

	private void onPlaylistModified(Playlist list)
	{
		boolean hasSelected = _uiTable.getSelectedRowCount() > 0;
		_btnReload.setEnabled(list == null ? false : list.isModified());
		_btnSave.setEnabled(list == null ? false : list.isModified());
		_btnUp.setEnabled(_isSortedByFileIx && hasSelected && _uiTable.getSelectedRow() > 0);
		_btnDown.setEnabled(_isSortedByFileIx && hasSelected && _uiTable.getSelectedRow() < _uiTable.getRowCount() - 1);
		_btnPlay.setEnabled(_playlist != null);
		getTableModel().fireTableDataChanged();
	}

	private void addItems()
	{
		JFileChooser chooser = new JFileChooser();
		FontHelper.recursiveSetFont(chooser.getComponents());
		chooser.addChoosableFileFilter(new AudioFileFilter());
		chooser.setMultiSelectionEnabled(true);
		if (GUIDriver.getInstance().hasAddedMediaDirectory())
		{
			chooser.setCurrentDirectory(new File(GUIDriver.getInstance().getMediaDirs()[0]));
		}
		if (chooser.showOpenDialog(getParentFrame()) == JFileChooser.APPROVE_OPTION)
		{
			final File[] files = chooser.getSelectedFiles();
			if (files.length == 0)
			{
				return;
			}

			showWaitCursor(true);

			ProgressWorker worker = new ProgressWorker()
			{
				@Override
				protected Object doInBackground() throws IOException
				{
					int selected = _uiTable.getSelectedRow();
					int insertIx = selected >= 0 ? _uiTable.convertRowIndexToModel(_uiTable.getSelectedRow()) : -1;

					if (insertIx >= 0)
					{
						int count = _playlist.add(insertIx, files, this);
						firstIx = insertIx;
						lastIx = firstIx + count - 1;
					}
					else
					{
						firstIx = 0;
						_playlist.add(files, this);
						lastIx = files.length - 1;
					}

					return null;
				}
				int firstIx;
				int lastIx;

				@Override
				protected void done()
				{
					try
					{
						get();
					}
					catch (InterruptedException ex)
					{
					}
					catch (ExecutionException ex)
					{
						showWaitCursor(false);
						JOptionPane.showMessageDialog(PlaylistEditCtrl.this.getParentFrame(), ex, "Add File Error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					// update list and select new items
					PlaylistTableModel model = getTableModel();
					if (_isSortedByFileIx)
					{
						// rows are in playlist order, so use quick update and selection methods
						_uiTable.clearSelection();
						_uiTable.addRowSelectionInterval(firstIx, lastIx);
					}
					else
					{
						// rows are in unknown order, so find and select each new row
						model.fireTableDataChanged();
						for (int ix = firstIx; ix <= lastIx; ix++)
						{
							int viewIx = _uiTable.convertRowIndexToView(ix);
							_uiTable.addRowSelectionInterval(viewIx, viewIx);
						}
					}
					resizeAllColumns();
				}
			};

			ProgressDialog pd = new ProgressDialog(null, true, worker, "Adding items...");
			pd.setVisible(true);

			showWaitCursor(false);
		}
	}

	private void deleteSelectedRows()
	{
		int[] rows = getSelectedRows();
		_playlist.remove(rows);
		PlaylistTableModel model = getTableModel();
		model.fireTableDataChanged();
	}

	private void moveSelectedRowsUp()
	{
		int[] rows = getSelectedRows();
		_playlist.moveUp(rows);
		_uiTable.clearSelection();
		for (int ix : rows)
		{
			_uiTable.addRowSelectionInterval(ix, ix);
		}
	}

	private void moveSelectedRowsDown()
	{
		int[] rows = getSelectedRows();
		_playlist.moveDown(rows);
		_uiTable.clearSelection();
		for (int ix : rows)
		{
			_uiTable.addRowSelectionInterval(ix, ix);
		}
	}

	private void locateMissingFiles()
	{
		final String[] libraryFiles = GUIDriver.getInstance().getMediaLibraryFileList();
		ProgressWorker worker = new ProgressWorker<List<Integer>, Void>()
		{
			@Override
			protected List<Integer> doInBackground()
			{
				return _playlist.repair(libraryFiles, this);
			}

			@Override
			protected void done()
			{
				try
				{
					_uiTable.clearSelection();
					List<Integer> fixed = get();
					for (Integer fixIx : fixed)
					{
						int viewIx = _uiTable.convertRowIndexToView(fixIx.intValue());
						_uiTable.addRowSelectionInterval(viewIx, viewIx);
					}
				}
				catch (CancellationException ex)
				{
				}
				catch (InterruptedException ex)
				{
				}
				catch (ExecutionException ex)
				{
				}
			}
		};
		ProgressDialog pd = new ProgressDialog(getParentFrame(), true, worker, "Repairing...");
		pd.setVisible(true);
	}

	private void reorderList()
	{
		Playlist.SortIx sortIx = Playlist.SortIx.None;
		boolean isDescending = false;
		if (!_isSortedByFileIx)
		{
			RowSorter sorter = _uiTable.getRowSorter();
			List<RowSorter.SortKey> keys = sorter.getSortKeys();
			if (keys.size() > 0)
			{
				RowSorter.SortKey key = keys.get(0);
				switch (key.getColumn())
				{
//                    case 0:
//                        // #
//                        order = Playlist.SortOrder.None;
//                        break;

					case 1:
						// status
						sortIx = Playlist.SortIx.Status;
						isDescending = key.getSortOrder() == SortOrder.DESCENDING;
						break;

					case 2:
						// filename
						sortIx = Playlist.SortIx.Filename;
						isDescending = key.getSortOrder() == SortOrder.DESCENDING;
						break;

					case 3:
						// path
						sortIx = Playlist.SortIx.Path;
						isDescending = key.getSortOrder() == SortOrder.DESCENDING;
						break;
				}
			}
		}

		ReorderPlaylistDialog dlg = new ReorderPlaylistDialog(getParentFrame(), true, sortIx, isDescending);
		dlg.setLocationRelativeTo(getParentFrame());
		dlg.setVisible(true);
		sortIx = dlg.getSelectedSortIx();
		if (sortIx != Playlist.SortIx.None)
		{
			showWaitCursor(true);

			_playlist.reorder(sortIx, dlg.getIsDescending());

			RowSorter sorter = _uiTable.getRowSorter();
			ArrayList<RowSorter.SortKey> keys = new ArrayList<RowSorter.SortKey>();
			keys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
			sorter.setSortKeys(keys);

			PlaylistTableModel model = getTableModel();
			model.fireTableDataChanged();

			showWaitCursor(false);
		}
	}

	private void editFilenames()
	{
		int[] rows = _uiTable.getSelectedRows();
		for (int row : rows)
		{
			int rowIx = _uiTable.convertRowIndexToModel(row);
			PlaylistEntry entry = _playlist.get(rowIx);
			EditFilenameResult response = EditFilenameDialog.showDialog(getParentFrame(), "Edit Filename", true, entry.getFileName());
			if (response.getResultCode() == EditFilenameDialog.OK)
			{
				_playlist.changeEntryFileName(rowIx, response.getFileName());
				//entry.setFileName(response.getFileName());
				getTableModel().fireTableRowsUpdated(rowIx, rowIx);
			}
		}
	}

	private void findClosestMatches()
	{
		final int rowIx = _uiTable.convertRowIndexToModel(_uiTable.getSelectedRow());
		final PlaylistEntry entry = _playlist.get(rowIx);
		final String[] libraryFiles = GUIDriver.getInstance().getMediaLibraryFileList();
		ProgressWorker worker = new ProgressWorker<List<MatchedPlaylistEntry>, Void>()
		{
			@Override
			protected List<MatchedPlaylistEntry> doInBackground()
			{
				return entry.findClosestMatches(libraryFiles, this);
			}

			@Override
			protected void done()
			{
				try
				{
					List<MatchedPlaylistEntry> matches = get();
					ClosestMatchChooserDialog dlg = new ClosestMatchChooserDialog(getParentFrame(), matches, true);
					dlg.center();
					dlg.setVisible(true);
					if (dlg.getResultCode() == ClosestMatchChooserDialog.OK)
					{
						if (_playlist.get(rowIx).getFile().compareTo(matches.get(dlg.getChoice()).getPlaylistFile().getFile()) != 0)
						{
							_playlist.replace(rowIx, matches.get(dlg.getChoice()).getPlaylistFile());
							getTableModel().fireTableRowsUpdated(rowIx, rowIx);
						}
					}
				}
				catch (InterruptedException ex)
				{
				}
				catch (CancellationException ex)
				{
				}
				catch (ExecutionException ex)
				{
				}
			}
		};
		ProgressDialog pd = new ProgressDialog(getParentFrame(), true, worker, "Finding closest matches...");
		pd.setVisible(true);
		_uiTable.setRowSelectionInterval(rowIx, rowIx);
	}

	private void bulkFindClosestMatches()
	{
		final String[] libraryFiles = GUIDriver.getInstance().getMediaLibraryFileList();
		ProgressWorker<List<BatchMatchItem>, Void> worker = new ProgressWorker<List<BatchMatchItem>, Void>()
		{
			@Override
			protected List<BatchMatchItem> doInBackground()
			{
				return _playlist.findClosestMatches(libraryFiles, this);
			}
		};
		ProgressDialog pd = new ProgressDialog(getParentFrame(), true, worker, "Finding closest matches for all missing files...");
		pd.setVisible(true);

		final List<BatchMatchItem> items;
		try
		{
			items = worker.get();
			if (items.isEmpty())
			{
				return;
			}
		}
		catch (CancellationException ex)
		{
			// do nothing, the user cancelled
			return;
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this.getParentFrame(), ex);
			return;
		}

		BatchClosestMatchResultsDialog dlg = new BatchClosestMatchResultsDialog(getParentFrame(), items);
		dlg.setLocationRelativeTo(getParentFrame());
		dlg.setVisible(true);
		if (dlg.isAccepted())
		{
			_uiTable.clearSelection();
			List<Integer> fixed = _playlist.applyClosestMatchSelections(items);
			for (Integer fixIx : fixed)
			{
				int viewIx = _uiTable.convertRowIndexToView(fixIx.intValue());
				_uiTable.addRowSelectionInterval(viewIx, viewIx);
			}
		}
	}

	private void replaceSelectedEntries()
	{
		int[] rows = _uiTable.getSelectedRows();
		for (int row : rows)
		{
			int rowIx = _uiTable.convertRowIndexToModel(row);
			PlaylistEntry entry = _playlist.get(rowIx);

			JFileChooser chooser = new JFileChooser();
			FontHelper.recursiveSetFont(chooser.getComponents());
			chooser.addChoosableFileFilter(new AudioFileFilter());
			chooser.setDialogTitle("Replacing '" + entry.getFileName() + "'");
			if (GUIDriver.getInstance().hasAddedMediaDirectory())
			{
				chooser.setCurrentDirectory(new File(GUIDriver.getInstance().getMediaDirs()[0]));
			}

			if (!entry.isURL())
			{
				chooser.setSelectedFile(entry.getAbsoluteFile());
			}
			if (chooser.showOpenDialog(getParentFrame()) == JFileChooser.APPROVE_OPTION)
			{
				File file = chooser.getSelectedFile();

				// make sure the replacement file is not a playlist
				if (Playlist.isPlaylist(file))
				{
					JOptionPane.showMessageDialog(getParentFrame(), "You cannot replace a file with a playlist file. Use Add File instead.", "Replace File Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				PlaylistEntry newEntry = new PlaylistEntry(file, null, _playlist.getFile());
				_playlist.replace(rowIx, newEntry);
				getTableModel().fireTableRowsUpdated(rowIx, rowIx);
			}
		}

	}

	private void removeDuplicates()
	{
		int dupCount = _playlist.removeDuplicates();
		if (dupCount > 0)
		{
			getTableModel().fireTableDataChanged();
		}
		String msg = dupCount == 1 ? "Removed 1 duplicate" : String.format("Removed %d duplicates", dupCount);
		JOptionPane.showMessageDialog(getParentFrame(), msg, "Duplicates Removed", JOptionPane.INFORMATION_MESSAGE);
	}

	private void removeMissing()
	{
		int count = _playlist.removeMissing();
		if (count > 0)
		{
			getTableModel().fireTableDataChanged();
		}
		String msg = count == 1 ? "Removed 1 missing entry" : String.format("Removed %d missing entries", count);
		JOptionPane.showMessageDialog(getParentFrame(), msg, "Missing Entries Removed", JOptionPane.INFORMATION_MESSAGE);
	}

	private void savePlaylist()
	{
		if (_playlist.isNew())
		{
			JFileChooser chooser = new JFileChooser();
			FontHelper.recursiveSetFont(chooser.getComponents());
			chooser.addChoosableFileFilter(new PlaylistFileChooserFilter());
			chooser.setSelectedFile(_playlist.getFile());
			int rc = chooser.showSaveDialog(getParentFrame());
			if (rc == JFileChooser.APPROVE_OPTION)
			{
				try
				{
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					File playlist = chooser.getSelectedFile();

					// make sure file has correct extension
					String normalizedName = playlist.getName().trim().toLowerCase();
					if (!Playlist.isPlaylist(playlist)
						|| (!normalizedName.endsWith(".m3u") && !normalizedName.endsWith(".m3u8") && !normalizedName.endsWith(".pls")))
					{
						if (_playlist.isUtfFormat())
						{
							playlist = new File(playlist.getPath() + ".m3u8");
						}
						else
						{
							playlist = new File(playlist.getPath() + ".m3u");
						}
					}

					final File finalPlaylistFile = playlist;
					String oldPath = _playlist.getFile().getCanonicalPath();
					ProgressWorker worker = new ProgressWorker<Void, Void>()
					{
						@Override
						protected Void doInBackground() throws IOException
						{
							boolean saveRelative = GUIDriver.getInstance().getAppOptions().getSavePlaylistsWithRelativePaths();
							_playlist.saveAs(finalPlaylistFile, saveRelative, this);
							return null;
						}
					};
					ProgressDialog pd = new ProgressDialog(getParentFrame(), true, worker, "Saving...");
					pd.setVisible(true);

					worker.get();

					if (getParentFrame() instanceof GUIScreen)
					{
						((GUIScreen) getParentFrame()).updateCurrentTab(_playlist);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, "Sorry, there was an error saving your playlist.  Please try again, or file a bug report.");
				}
				finally
				{
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}
		else
		{
			try
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				ProgressWorker worker = new ProgressWorker<Void, Void>()
				{
					@Override
					protected Void doInBackground() throws IOException
					{
						boolean saveRelative = GUIDriver.getInstance().getAppOptions().getSavePlaylistsWithRelativePaths();
						_playlist.save(saveRelative, this);
						return null;
					}
				};
				ProgressDialog pd = new ProgressDialog(getParentFrame(), true, worker, "Saving...");
				pd.setVisible(true);
				worker.get();
			}
			catch (Exception ex)
			{
				Logger.getLogger(GUIScreen.class.getName()).log(Level.SEVERE, null, ex);
				JOptionPane.showMessageDialog(this, "Sorry, there was an error saving your playlist.  Please try again, or file a bug report.");
			}
			finally
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	private void playSelectedEntries()
	{
		try
		{
			int[] rows;
			if (_uiTable.getSelectedRowCount() > 0)
			{
				rows = _uiTable.getSelectedRows();
			}
			else
			{
				rows = new int[_uiTable.getRowCount()];
				for (int r = 0; r < rows.length; r++)
				{
					rows[r] = r;
				}
			}

			for (int r = 0; r < rows.length; r++)
			{
				rows[r] = _uiTable.convertRowIndexToModel(rows[r]);
			}

			// Get a list of the selected rows that aren't missing, effectively stripping those entries from the selection
			List<Integer> rowList = new ArrayList<Integer>();
			for (int i : rows)
			{
				if (_playlist.get(i).isFound() || _playlist.get(i).isURL())
				{
					rowList.add(i);
				}
			}
			rows = ArrayFunctions.integerListToArray(rowList);

			// Get a temp playlist
			Playlist tempList = _playlist.getSublist(rows);

			// Sanity check, don't launch an empty list
			if (tempList.size() > 0)
			{
				tempList.play();
			}
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(getParentFrame(), "Could not open these playlist entries, error was: \n\n" + ex.toString());
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

        _playlistEntryRightClickMenu = new javax.swing.JPopupMenu();
        _miEditFilename = new javax.swing.JMenuItem();
        _miReplace = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        _miFindClosest = new javax.swing.JMenuItem();
        _miBatchFindClosest = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        _miRemoveDups = new javax.swing.JMenuItem();
        _miRemoveMissing = new javax.swing.JMenuItem();
        _miCopyFiles = new javax.swing.JMenuItem();
        _uiToolbar = new javax.swing.JToolBar();
        _btnSave = new javax.swing.JButton();
        _btnReload = new javax.swing.JButton();
        _btnAdd = new javax.swing.JButton();
        _btnDelete = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        _btnUp = new javax.swing.JButton();
        _btnDown = new javax.swing.JButton();
        _btnReorder = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        _btnPlay = new javax.swing.JButton();
        _btnMagicFix = new javax.swing.JButton();
        _btnLocate = new javax.swing.JButton();
        _uiTableScrollPane = new javax.swing.JScrollPane();
        _uiTable = createTable();

        _playlistEntryRightClickMenu.setFont(new java.awt.Font("SansSerif", 0, 10)); // NOI18N

        _miEditFilename.setFont(new java.awt.Font("SansSerif", 0, 10));
        _miEditFilename.setText("Edit Filename");
        _miEditFilename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onMenuEditFilenameActionPerformed(evt);
            }
        });
        _playlistEntryRightClickMenu.add(_miEditFilename);

        _miReplace.setFont(new java.awt.Font("SansSerif", 0, 10));
        _miReplace.setText("Replace Selected Entry");
        _miReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onMenuReplaceSelectedEntry(evt);
            }
        });
        _playlistEntryRightClickMenu.add(_miReplace);
        _playlistEntryRightClickMenu.add(jSeparator3);

        _miFindClosest.setFont(new java.awt.Font("SansSerif", 0, 10));
        _miFindClosest.setText("Find Closest Matches");
        _miFindClosest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onMenuFindClosestActionPerformed(evt);
            }
        });
        _playlistEntryRightClickMenu.add(_miFindClosest);

        _miBatchFindClosest.setFont(new java.awt.Font("SansSerif", 0, 10));
        _miBatchFindClosest.setText("Batch Find Closest Matches");
        _miBatchFindClosest.setToolTipText("Finds best closest match for all missing files in list");
        _miBatchFindClosest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onMenuBatchFindClosestMatchActionPerformed(evt);
            }
        });
        _playlistEntryRightClickMenu.add(_miBatchFindClosest);
        _playlistEntryRightClickMenu.add(jSeparator4);

        _miRemoveDups.setFont(new java.awt.Font("SansSerif", 0, 10));
        _miRemoveDups.setText("Remove Duplicates");
        _miRemoveDups.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onMenuRemoveDuplicatesActionPerformed(evt);
            }
        });
        _playlistEntryRightClickMenu.add(_miRemoveDups);

        _miRemoveMissing.setFont(new java.awt.Font("SansSerif", 0, 10));
        _miRemoveMissing.setText("Remove Missing");
        _miRemoveMissing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onMenuRemoveMissingActionPerformed(evt);
            }
        });
        _playlistEntryRightClickMenu.add(_miRemoveMissing);

        _miCopyFiles.setFont(new java.awt.Font("SansSerif", 0, 10));
        _miCopyFiles.setText("Copy Files");
        _miCopyFiles.setToolTipText("");
        _miCopyFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onMenuCopyFiles(evt);
            }
        });
        _playlistEntryRightClickMenu.add(_miCopyFiles);

        setLayout(new java.awt.BorderLayout());

        _uiToolbar.setFloatable(false);
        _uiToolbar.setRollover(true);

        _btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save.gif"))); // NOI18N
        _btnSave.setToolTipText("Save");
        _btnSave.setEnabled(_playlist == null ? false : _playlist.isModified());
        _btnSave.setFocusable(false);
        _btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnSave.setMaximumSize(new java.awt.Dimension(31, 31));
        _btnSave.setMinimumSize(new java.awt.Dimension(31, 31));
        _btnSave.setPreferredSize(new java.awt.Dimension(31, 31));
        _btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBtnSaveActionPerformed(evt);
            }
        });
        _uiToolbar.add(_btnSave);

        _btnReload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/gtk-refresh.png"))); // NOI18N
        _btnReload.setToolTipText("Reload");
        _btnReload.setEnabled(_playlist == null ? false : _playlist.isModified());
        _btnReload.setFocusable(false);
        _btnReload.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnReload.setMaximumSize(new java.awt.Dimension(31, 31));
        _btnReload.setMinimumSize(new java.awt.Dimension(31, 31));
        _btnReload.setPreferredSize(new java.awt.Dimension(31, 31));
        _btnReload.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnReload.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                _btnReloadMouseClicked(evt);
            }
        });
        _uiToolbar.add(_btnReload);

        _btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/edit-add.gif"))); // NOI18N
        _btnAdd.setToolTipText("Append/Insert");
        _btnAdd.setEnabled(false);
        _btnAdd.setFocusable(false);
        _btnAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnAdd.setMaximumSize(new java.awt.Dimension(31, 31));
        _btnAdd.setPreferredSize(new java.awt.Dimension(31, 31));
        _btnAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBtnAddActionPerformed(evt);
            }
        });
        _uiToolbar.add(_btnAdd);

        _btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/edit-delete-2.gif"))); // NOI18N
        _btnDelete.setToolTipText("Delete");
        _btnDelete.setEnabled(false);
        _btnDelete.setFocusable(false);
        _btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnDelete.setMaximumSize(new java.awt.Dimension(31, 31));
        _btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBtnDeleteActionPerformed(evt);
            }
        });
        _uiToolbar.add(_btnDelete);
        _uiToolbar.add(jSeparator1);

        _btnUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow-up.gif"))); // NOI18N
        _btnUp.setToolTipText("Move Up");
        _btnUp.setEnabled(false);
        _btnUp.setFocusable(false);
        _btnUp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnUp.setMaximumSize(new java.awt.Dimension(31, 31));
        _btnUp.setMinimumSize(new java.awt.Dimension(31, 31));
        _btnUp.setPreferredSize(new java.awt.Dimension(31, 31));
        _btnUp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBtnUpActionPerformed(evt);
            }
        });
        _uiToolbar.add(_btnUp);

        _btnDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/arrow_down.gif"))); // NOI18N
        _btnDown.setToolTipText("Move Down");
        _btnDown.setEnabled(false);
        _btnDown.setFocusable(false);
        _btnDown.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnDown.setMaximumSize(new java.awt.Dimension(31, 31));
        _btnDown.setMinimumSize(new java.awt.Dimension(31, 31));
        _btnDown.setPreferredSize(new java.awt.Dimension(31, 31));
        _btnDown.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBtnDownActionPerformed(evt);
            }
        });
        _uiToolbar.add(_btnDown);

        _btnReorder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/edit-reorder.gif"))); // NOI18N
        _btnReorder.setToolTipText("Change Playlist Order");
        _btnReorder.setEnabled(false);
        _btnReorder.setFocusable(false);
        _btnReorder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnReorder.setMaximumSize(new java.awt.Dimension(31, 31));
        _btnReorder.setMinimumSize(new java.awt.Dimension(31, 31));
        _btnReorder.setPreferredSize(new java.awt.Dimension(31, 31));
        _btnReorder.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnReorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBtnReorderActionPerformed(evt);
            }
        });
        _uiToolbar.add(_btnReorder);
        _uiToolbar.add(jSeparator2);

        _btnPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/play.gif"))); // NOI18N
        _btnPlay.setToolTipText("Play Selected");
        _btnPlay.setEnabled(_playlist != null);
        _btnPlay.setFocusable(false);
        _btnPlay.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnPlay.setMaximumSize(new java.awt.Dimension(31, 31));
        _btnPlay.setMinimumSize(new java.awt.Dimension(31, 31));
        _btnPlay.setPreferredSize(new java.awt.Dimension(31, 31));
        _btnPlay.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBtnPlayActionPerformed(evt);
            }
        });
        _uiToolbar.add(_btnPlay);

        _btnMagicFix.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/magic-fix.png"))); // NOI18N
        _btnMagicFix.setToolTipText("Fix Everything");
        _btnMagicFix.setEnabled(_playlist == null ? false : _playlist.getFile().exists());
        _btnMagicFix.setFocusable(false);
        _btnMagicFix.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnMagicFix.setMaximumSize(new java.awt.Dimension(31, 31));
        _btnMagicFix.setMinimumSize(new java.awt.Dimension(31, 31));
        _btnMagicFix.setPreferredSize(new java.awt.Dimension(31, 31));
        _btnMagicFix.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnMagicFix.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnMagicFixonBtnLocateActionPerformed(evt);
            }
        });
        _uiToolbar.add(_btnMagicFix);

        _btnLocate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/edit-find.gif"))); // NOI18N
        _btnLocate.setToolTipText("Find Exact Matches");
        _btnLocate.setEnabled(_playlist == null ? false : _playlist.getFile().exists());
        _btnLocate.setFocusable(false);
        _btnLocate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _btnLocate.setMaximumSize(new java.awt.Dimension(31, 31));
        _btnLocate.setMinimumSize(new java.awt.Dimension(31, 31));
        _btnLocate.setPreferredSize(new java.awt.Dimension(31, 31));
        _btnLocate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _btnLocate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBtnLocateActionPerformed(evt);
            }
        });
        _uiToolbar.add(_btnLocate);

        add(_uiToolbar, java.awt.BorderLayout.PAGE_START);

        _uiTableScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        _uiTable.setAutoCreateRowSorter(true);
        _uiTable.setModel(new PlaylistTableModel());
        _uiTable.setDragEnabled(true);
        _uiTable.setFillsViewportHeight(true);
        _uiTable.setFont(new java.awt.Font("SansSerif", 0, 10));
        _uiTable.setGridColor(new java.awt.Color(153, 153, 153));
        _uiTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        _uiTable.setRowHeight(20);
        _uiTable.getTableHeader().setReorderingAllowed(false);
        _uiTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                _uiTableMousePressed(evt);
            }
        });
        _uiTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                _uiTableKeyPressed(evt);
            }
        });
        _uiTableScrollPane.setViewportView(_uiTable);

        add(_uiTableScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void onBtnSaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnSaveActionPerformed
    {//GEN-HEADEREND:event_onBtnSaveActionPerformed
		savePlaylist();
    }//GEN-LAST:event_onBtnSaveActionPerformed

    private void onBtnAddActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnAddActionPerformed
    {//GEN-HEADEREND:event_onBtnAddActionPerformed
		addItems();
    }//GEN-LAST:event_onBtnAddActionPerformed

    private void onBtnDeleteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnDeleteActionPerformed
    {//GEN-HEADEREND:event_onBtnDeleteActionPerformed
		deleteSelectedRows();
    }//GEN-LAST:event_onBtnDeleteActionPerformed

    private void onBtnUpActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnUpActionPerformed
    {//GEN-HEADEREND:event_onBtnUpActionPerformed
		moveSelectedRowsUp();
    }//GEN-LAST:event_onBtnUpActionPerformed

    private void onBtnDownActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnDownActionPerformed
    {//GEN-HEADEREND:event_onBtnDownActionPerformed
		moveSelectedRowsDown();
    }//GEN-LAST:event_onBtnDownActionPerformed

    private void onBtnLocateActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnLocateActionPerformed
    {//GEN-HEADEREND:event_onBtnLocateActionPerformed
		locateMissingFiles();
    }//GEN-LAST:event_onBtnLocateActionPerformed

    private void onBtnReorderActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnReorderActionPerformed
    {//GEN-HEADEREND:event_onBtnReorderActionPerformed
		reorderList();
    }//GEN-LAST:event_onBtnReorderActionPerformed

    private void onMenuEditFilenameActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onMenuEditFilenameActionPerformed
    {//GEN-HEADEREND:event_onMenuEditFilenameActionPerformed
		editFilenames();
    }//GEN-LAST:event_onMenuEditFilenameActionPerformed

    private void onMenuFindClosestActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onMenuFindClosestActionPerformed
    {//GEN-HEADEREND:event_onMenuFindClosestActionPerformed
		findClosestMatches();
    }//GEN-LAST:event_onMenuFindClosestActionPerformed

    private void onMenuReplaceSelectedEntry(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onMenuReplaceSelectedEntry
    {//GEN-HEADEREND:event_onMenuReplaceSelectedEntry
		replaceSelectedEntries();
    }//GEN-LAST:event_onMenuReplaceSelectedEntry

    private void onMenuRemoveDuplicatesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onMenuRemoveDuplicatesActionPerformed
    {//GEN-HEADEREND:event_onMenuRemoveDuplicatesActionPerformed
		removeDuplicates();
    }//GEN-LAST:event_onMenuRemoveDuplicatesActionPerformed

    private void onMenuRemoveMissingActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onMenuRemoveMissingActionPerformed
    {//GEN-HEADEREND:event_onMenuRemoveMissingActionPerformed
		removeMissing();
    }//GEN-LAST:event_onMenuRemoveMissingActionPerformed

    private void onBtnPlayActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnPlayActionPerformed
    {//GEN-HEADEREND:event_onBtnPlayActionPerformed
		playSelectedEntries();
    }//GEN-LAST:event_onBtnPlayActionPerformed

    private void onMenuCopyFiles(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onMenuCopyFiles
    {//GEN-HEADEREND:event_onMenuCopyFiles
		JFileChooser destDirFileChooser = new JFileChooser();
		destDirFileChooser.setDialogTitle("Choose a destination directory...");
		destDirFileChooser.setAcceptAllFileFilterUsed(false);
		destDirFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		FontHelper.recursiveSetFont(destDirFileChooser.getComponents());
		int response = destDirFileChooser.showOpenDialog(getParentFrame());
		if (response == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				final File destDir = destDirFileChooser.getSelectedFile();

				ProgressWorker<Void, Void> worker = new ProgressWorker<Void, Void>()
				{
					@Override
					protected Void doInBackground()
					{
						PlaylistEntryFileCopier copier = new PlaylistEntryFileCopier(_playlist, destDir, this);
						copier.copy();
						return null;
					}
				};
				ProgressDialog pd = new ProgressDialog(getParentFrame(), true, worker, "Copying Files...");
				pd.setVisible(true);

				try
				{
					worker.get();
				}
				catch (CancellationException ex)
				{
				}
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(getParentFrame(), "An error has occured, 1 or more files were not copied.");
				e.printStackTrace();
			}
		}
    }//GEN-LAST:event_onMenuCopyFiles

    private void onMenuBatchFindClosestMatchActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onMenuBatchFindClosestMatchActionPerformed
    {//GEN-HEADEREND:event_onMenuBatchFindClosestMatchActionPerformed
		bulkFindClosestMatches();
    }//GEN-LAST:event_onMenuBatchFindClosestMatchActionPerformed

	private void _uiTableMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event__uiTableMousePressed
	{//GEN-HEADEREND:event__uiTableMousePressed
		if (SwingUtilities.isLeftMouseButton(evt))
		{
			currentlySelectedRow = _uiTable.rowAtPoint(evt.getPoint());
			if (currentlySelectedRow != -1 && evt.getClickCount() == 2 && (evt.getModifiers() & ActionEvent.CTRL_MASK) <= 0)
			{
				playSelectedEntries();
			}
		}
	}//GEN-LAST:event__uiTableMousePressed

	private void _btnReloadMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event__btnReloadMouseClicked
	{//GEN-HEADEREND:event__btnReloadMouseClicked
		if (_playlist.isModified())
		{
			Object[] options =
			{
				"Discard Changes and Reload", "Cancel"
			};
			int rc = JOptionPane.showOptionDialog(this.getParentFrame(), "The current list is modified, do you really want to discard these changes and reload from source?\n", "Confirm Reload",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (rc == JOptionPane.NO_OPTION)
			{
				return;
			}
			else
			{
				showWaitCursor(true);

				ProgressWorker worker = new ProgressWorker()
				{
					@Override
					protected Object doInBackground() throws IOException
					{
						_playlist.reload(this);
						return null;
					}
					int firstIx;
					int lastIx;

					@Override
					protected void done()
					{
						try
						{
							get();
						}
						catch (CancellationException ex)
						{
							return;
						}
						catch (InterruptedException ex)
						{
							return;
						}
						catch (ExecutionException ex)
						{
							showWaitCursor(false);
							JOptionPane.showMessageDialog(PlaylistEditCtrl.this.getParentFrame(), ex, "Reload Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						getTableModel().fireTableDataChanged();
						resizeAllColumns();
					}
				};

				ProgressDialog pd = new ProgressDialog(null, true, worker, "Reloading...");
				pd.setVisible(true);

				showWaitCursor(false);
			}
		}
	}//GEN-LAST:event__btnReloadMouseClicked

	private void openInternal()
	{
		JFileChooser jM3UChooser = new JFileChooser();
		FontHelper.recursiveSetFont(jM3UChooser.getComponents());
		jM3UChooser.addChoosableFileFilter(new PlaylistFileChooserFilter());
		if (_playlist != null)
		{
			jM3UChooser.setSelectedFile(_playlist.getFile());
		}

		int response = jM3UChooser.showOpenDialog(getParentFrame());
		if (response == JFileChooser.APPROVE_OPTION)
		{
			final File file = jM3UChooser.getSelectedFile();
			final String[] libraryFiles;
			if (GUIDriver.getInstance().getAppOptions().getAutoLocateEntriesOnPlaylistLoad())
			{
				libraryFiles = GUIDriver.getInstance().getMediaLibraryFileList();
			}
			else
			{
				libraryFiles = null;
			}

			ProgressWorker<Playlist, Void> worker = new ProgressWorker<Playlist, Void>()
			{
				@Override
				protected Playlist doInBackground() throws Exception
				{
					Playlist list = new Playlist(file, this);
					if (libraryFiles != null)
					{
						list.repair(libraryFiles, this);
					}
					return list;
				}

				@Override
				protected void done()
				{
					Playlist list;
					try
					{
						list = get();
					}
					catch (CancellationException ex)
					{
						return;
					}
					catch (Exception ex)
					{
						JOptionPane.showMessageDialog(PlaylistEditCtrl.this.getParentFrame(), ex, "Open Playlist Error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					((GUIScreen) getParentFrame()).updateCurrentTab(list);
				}
			};
			ProgressDialog pd = new ProgressDialog((GUIScreen) getParentFrame(), true, worker, "Loading...");
			pd.setVisible(true);
		}
		else
		{
			jM3UChooser.cancelSelection();
		}
	}

	private void _uiTableKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event__uiTableKeyPressed
	{//GEN-HEADEREND:event__uiTableKeyPressed
		int keyVal = evt.getKeyCode();
		if (keyVal == KeyEvent.VK_DELETE)
		{
			deleteSelectedRows();
		}
		else if (keyVal == KeyEvent.VK_ENTER)
		{
			playSelectedEntries();
		}
	}//GEN-LAST:event__uiTableKeyPressed

	private void _btnMagicFixonBtnLocateActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__btnMagicFixonBtnLocateActionPerformed
	{//GEN-HEADEREND:event__btnMagicFixonBtnLocateActionPerformed
		locateMissingFiles();
		bulkFindClosestMatches();
	}//GEN-LAST:event__btnMagicFixonBtnLocateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _btnAdd;
    private javax.swing.JButton _btnDelete;
    private javax.swing.JButton _btnDown;
    private javax.swing.JButton _btnLocate;
    private javax.swing.JButton _btnMagicFix;
    private javax.swing.JButton _btnPlay;
    private javax.swing.JButton _btnReload;
    private javax.swing.JButton _btnReorder;
    private javax.swing.JButton _btnSave;
    private javax.swing.JButton _btnUp;
    private javax.swing.JMenuItem _miBatchFindClosest;
    private javax.swing.JMenuItem _miCopyFiles;
    private javax.swing.JMenuItem _miEditFilename;
    private javax.swing.JMenuItem _miFindClosest;
    private javax.swing.JMenuItem _miRemoveDups;
    private javax.swing.JMenuItem _miRemoveMissing;
    private javax.swing.JMenuItem _miReplace;
    private javax.swing.JPopupMenu _playlistEntryRightClickMenu;
    private listfix.view.support.ZebraJTable _uiTable;
    private javax.swing.JScrollPane _uiTableScrollPane;
    private javax.swing.JToolBar _uiToolbar;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    // End of variables declaration//GEN-END:variables
	private static ImageIcon _imgMissing = new ImageIcon(BatchRepairDialog.class.getResource("/images/icon-missing.png"));
	private static ImageIcon _imgFound = new ImageIcon(BatchRepairDialog.class.getResource("/images/icon-found.png"));
	private static ImageIcon _imgFixed = new ImageIcon(BatchRepairDialog.class.getResource("/images/icon-fixed.png"));
	private static ImageIcon _imgUrl = new ImageIcon(BatchRepairDialog.class.getResource("/images/icon-url.png"));

	public Playlist getPlaylist()
	{
		return _playlist;
	}

	public void setPlaylist(Playlist list)
	{
		setPlaylist(list, false);
	}

	public void setPlaylist(Playlist list, boolean force)
	{
		if (_playlist == list && !force)
		{
			return;
		}
		_playlist = list;

		((PlaylistTableModel) _uiTable.getModel()).fireTableDataChanged();

		boolean hasPlaylist = _playlist != null;
		_btnAdd.setEnabled(hasPlaylist);
		_btnLocate.setEnabled(hasPlaylist);
		_btnMagicFix.setEnabled(hasPlaylist);
		_btnReorder.setEnabled(hasPlaylist);
		_btnReload.setEnabled(hasPlaylist && _playlist.isModified());
		_btnPlay.setEnabled(hasPlaylist);
		_btnSave.setEnabled(_playlist == null ? false : _playlist.isModified());

		if (_playlist != null && !_playlist.isEmpty())
		{
			resizeAllColumns();
		}

		if (_playlist != null)
		{
			_playlist.addModifiedListener(listener);
		}
	}
	private Playlist _playlist;
	private final IPlaylistModifiedListener listener = new IPlaylistModifiedListener()
	{
		public void playlistModified(Playlist list)
		{
			onPlaylistModified(list);
		}
	};

	private void showWaitCursor(boolean isWaiting)
	{
		if (isWaiting)
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		else
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	private void resizeAllColumns()
	{
		// resize columns to fit
		int cwidth = 0;
		cwidth += _uiTable.autoResizeColumn(1, true);
		cwidth += _uiTable.autoResizeColumn(2);
		cwidth += _uiTable.autoResizeColumn(3);
		TableColumnModel cm = _uiTable.getColumnModel();
		TableCellRenderer renderer = _uiTable.getDefaultRenderer(Integer.class);
		Component comp = renderer.getTableCellRendererComponent(_uiTable, (_uiTable.getRowCount() + 1) * 10, false, false, 0, 0);
		int width = comp.getPreferredSize().width + 4;
		TableColumn col = cm.getColumn(0);
		col.setMinWidth(width);
		col.setMaxWidth(width);
		col.setPreferredWidth(width);
		cwidth += width;
	}
	private boolean _isSortedByFileIx;

	private void refreshAddTooltip(boolean hasSelected)
	{
		if (!_isSortedByFileIx)
		{
			_btnAdd.setToolTipText("Append File or Playlist (sort by # to insert)");
		}
		else if (hasSelected)
		{
			_btnAdd.setToolTipText("Insert File or Playlist after selected item");
		}
		else
		{
			_btnAdd.setToolTipText("Append File or Playlist");
		}
	}

	private PlaylistTableModel getTableModel()
	{
		return (PlaylistTableModel) _uiTable.getModel();
	}

	private Frame getParentFrame()
	{
		return JOptionPane.getFrameForComponent(this);
	}

	private int[] getSelectedRows()
	{
		int[] rows = _uiTable.getSelectedRows();
		RowSorter sorter = _uiTable.getRowSorter();
		for (int ix = 0; ix < rows.length; ix++)
		{
			rows[ix] = sorter.convertRowIndexToModel(rows[ix]);
		}
		return rows;
	}

	private ZebraJTable createTable()
	{
		return new ZebraJTable()
		{
			@Override
			public String getToolTipText(MouseEvent event)
			{
				Point point = event.getPoint();
				int rawRowIx = rowAtPoint(point);
				int rawColIx = columnAtPoint(point);
				if (rawRowIx >= 0 && rawColIx >= 0)
				{
					int rowIx = convertRowIndexToModel(rawRowIx);
					int colIx = convertColumnIndexToModel(rawColIx);
					if (rowIx >= 0 && rowIx < _playlist.size() && (colIx == 1))
					{
						PlaylistEntry entry = _playlist.get(rowIx);
						return (entry.isURL() ? "URL" : entry.getStatus().toString());
					}
				}
				return super.getToolTipText(event);
			}
		};
	}

	private boolean selectedRowsContainFoundEntry()
	{
		for (int row : _uiTable.getSelectedRows())
		{
			if (_playlist.get(_uiTable.convertRowIndexToModel(row)).isFound() || _playlist.get(_uiTable.convertRowIndexToModel(row)).isURL())
			{
				return true;
			}
		}
		return false;
	}

	private void initPlaylistTable()
	{
		_uiTable.setDefaultRenderer(Integer.class, new IntRenderer());
		_uiTable.initFillColumnForScrollPane(_uiTableScrollPane);

		_uiTable.setShowHorizontalLines(false);
		_uiTable.setShowVerticalLines(false);
		_uiTable.getTableHeader().setFont(new Font("SansSerif", 0, 10));

		// resize columns
		TableColumnModel cm = _uiTable.getColumnModel();
		cm.getColumn(0).setPreferredWidth(20);
		cm.getColumn(1).setPreferredWidth(20);
		cm.getColumn(2).setPreferredWidth(100);
		cm.getColumn(3).setPreferredWidth(100);
		_uiTable.setFillerColumnWidth(_uiTableScrollPane);

		// add selection listener
		_uiTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
				{
					return;
				}

				boolean hasSelected = _uiTable.getSelectedRowCount() > 0;
				_btnDelete.setEnabled(hasSelected);
				_btnUp.setEnabled(_isSortedByFileIx && hasSelected && _uiTable.getSelectedRow() > 0);
				_btnDown.setEnabled(_isSortedByFileIx && hasSelected && _uiTable.getSelectedRow() < _uiTable.getRowCount() - 1);
				_btnPlay.setEnabled(_playlist != null && ( _uiTable.getSelectedRow() < 0 || ( _uiTable.getSelectedRows().length > 0 && selectedRowsContainFoundEntry() ) ) );
				_btnReload.setEnabled(_playlist == null ? false : _playlist.isModified());
				_btnSave.setEnabled(_playlist == null ? false : _playlist.isModified());
				if (_isSortedByFileIx)
				{
					refreshAddTooltip(hasSelected);
				}
			}
		});

		// add sort listener
		RowSorter sorter = _uiTable.getRowSorter();
		sorter.addRowSorterListener(new RowSorterListener()
		{
			public void sorterChanged(RowSorterEvent e)
			{
				RowSorter sorter = e.getSource();
				List<RowSorter.SortKey> keys = sorter.getSortKeys();
				if ((keys.size() < 1) || (keys.get(0).getColumn() != 0) || (keys.get(0).getSortOrder() != SortOrder.ASCENDING))
				{
					// # is not the first sort column - disable move up / move down
					if (_isSortedByFileIx)
					{
						_isSortedByFileIx = false;
						_btnUp.setEnabled(false);
						_btnDown.setEnabled(false);
						_btnUp.setToolTipText("Move Up (sort by # to enable)");
						_btnDown.setToolTipText("Move Down (sort by # to enable)");
						refreshAddTooltip(false);
					}
				}
				else
				{
					if (!_isSortedByFileIx)
					{
						_isSortedByFileIx = true;
						boolean hasSelected = _uiTable.getSelectedRowCount() > 0;
						_btnUp.setEnabled(hasSelected);
						_btnDown.setEnabled(hasSelected);
						_btnUp.setToolTipText("Move Up");
						_btnDown.setToolTipText("Move Down");
						refreshAddTooltip(hasSelected);
					}
				}
			}
		});

		// set sort to #
		ArrayList<RowSorter.SortKey> keys = new ArrayList<RowSorter.SortKey>();
		keys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(keys);

		// add popup menu to list, handle's right click actions.
		_uiTable.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				showMenu(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				showMenu(e);
			}

			private void showMenu(MouseEvent e)
			{
				if (_playlist != null)
				{
					Point p = e.getPoint();
					int rowIx = _uiTable.rowAtPoint(p);
					if (e.isPopupTrigger())
					{
						boolean isOverItem = rowIx >= 0;
						if (isOverItem && (e.getModifiers() & ActionEvent.CTRL_MASK) > 0)
						{
							_uiTable.getSelectionModel().addSelectionInterval(rowIx, rowIx);
						}
						else if ((isOverItem && _uiTable.getSelectedRowCount() == 0)
							|| (!_uiTable.isRowSelected(rowIx)))
						{
							_uiTable.getSelectionModel().setSelectionInterval(rowIx, rowIx);
						}

						_miEditFilename.setEnabled(isOverItem);
						_miFindClosest.setEnabled(isOverItem);
						_miReplace.setEnabled(isOverItem);

						if (_uiTable.getSelectedRowCount() > 1)
						{
							_miReplace.setText("Replace Selected Entries");
							_miEditFilename.setText("Edit Filenames");
						}
						else
						{
							_miEditFilename.setText("Edit Filename");
							_miReplace.setText("Replace Selected Entry");
						}

						_playlistEntryRightClickMenu.show(e.getComponent(), p.x, p.y);
					}
					else
					{
						if (rowIx < 0)
						{
							_uiTable.clearSelection();
						}
					}
				}
			}
		});

		// drag-n-drop support
		_uiTable.setTransferHandler(new TransferHandler()
		{
			@Override
			public boolean canImport(TransferHandler.TransferSupport info)
			{
				if (!info.isDataFlavorSupported(_playlistEntryListFlavor) 
					&& !info.isDataFlavorSupported(DataFlavor.stringFlavor)
					&& !info.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
					return false;
				}

				JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
				if (dl.getRow() == -1)
				{
					return false;
				}

				return _isSortedByFileIx;
			}

			public boolean importData(TransferHandler.TransferSupport info)
			{
				if (!info.isDrop())
				{
					return false;
				}

				// Check for custom PlaylistEntryList flavor
				if (!info.isDataFlavorSupported(_playlistEntryListFlavor) 
					&& !info.isDataFlavorSupported(DataFlavor.stringFlavor)
					&& !info.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
					return false;
				}

				final JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();

				if (info.isDataFlavorSupported(_playlistEntryListFlavor))
				{
					// Get the entries that are being dropped.
					Transferable t = info.getTransferable();
					try
					{
						PlaylistEntryList data = (PlaylistEntryList) t.getTransferData(_playlistEntryListFlavor);
						List<PlaylistEntry> entries = data.getList();
						int removedAt = 0;
						int insertAtUpdated = dl.getRow();
						int i = 0;
						for (PlaylistEntry entry : entries)
						{
							// remove them all, we'll re-add them in bulk...
							removedAt = _playlist.remove(entry);

							// Was the thing we just removed above where we're inserting?
							if (removedAt < insertAtUpdated)
							{
								insertAtUpdated--;
							}
							i++;
						}

						_playlist.addAll(insertAtUpdated, entries);

						return true;
					}
					catch (Exception e)
					{
						return false;
					}
				}
				else if (info.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
					return HandleFileListFlavor(info, dl);
				}
				else
				{					
					return HandleStringFlavor(info, dl);
				}
			}

			private boolean HandleFileListFlavor(TransferSupport info, final JTable.DropLocation dl)
			{
				Transferable t = info.getTransferable();
				DataFlavor[] flavors = t.getTransferDataFlavors();
				for (int i = 0; i < flavors.length; i++)
				{
					try
					{
						int insertAt = dl.getRow();
						final List list;
						list = (List)t.getTransferData(flavors[i]);
						for (int j = 0; j < list.size(); j++)
						{
							final File tempFile = (File)list.get(j);
							if (tempFile instanceof File)
							{
								if (Playlist.isPlaylist(tempFile))
								{
									final String[] libraryFiles;
									if (GUIDriver.getInstance().getAppOptions().getAutoLocateEntriesOnPlaylistLoad())
									{
										libraryFiles = GUIDriver.getInstance().getMediaLibraryFileList();
									}
									else
									{
										libraryFiles = null;
									}
									ProgressWorker<Playlist, Void> worker = new ProgressWorker<Playlist, Void>()
									{
										@Override
										protected Playlist doInBackground() throws Exception
										{
											Playlist list = new Playlist(tempFile, this);
											if (libraryFiles != null)
											{
												list.repair(libraryFiles, this);
											}
											return list;
										}

										@Override
										protected void done()
										{
											Playlist list;
											try
											{
												list = get();
												_playlist.addAll(dl.getRow(), list.getEntries());
											}
											catch (CancellationException ex)
											{
												return;
											}
											catch (Exception ex)
											{
												JOptionPane.showMessageDialog(PlaylistEditCtrl.this.getParentFrame(), ex, "Open Playlist Error", JOptionPane.ERROR_MESSAGE);
												return;
											}
										}
									};
									ProgressDialog pd = new ProgressDialog((GUIScreen) getParentFrame(), true, worker, "Loading...");
									int plistSize = _playlist.size();
									pd.setVisible(true);
									insertAt += _playlist.size() - plistSize;
								}
								else
								{
									// add it to the playlist!
									_playlist.add(insertAt, new File[] {tempFile}, null);
									insertAt++;
								}
							}
						}
						resizeAllColumns();
						return true;
					}
					catch (UnsupportedFlavorException ex)
					{
						Logger.getLogger(PlaylistEditCtrl.class.getName()).log(Level.SEVERE, null, ex);
					}
					catch (IOException ex)
					{
						Logger.getLogger(PlaylistEditCtrl.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
				return false;
			}

			private boolean HandleStringFlavor(TransferSupport info, final JTable.DropLocation dl)
			{
				Transferable t = info.getTransferable();
				DataFlavor[] flavors = t.getTransferDataFlavors();
				for (int i = 0; i < flavors.length; i++)
				{
					try
					{
						final String data;
						data = (String) t.getTransferData(flavors[i]);
						final String[] libraryFiles;
						if (GUIDriver.getInstance().getAppOptions().getAutoLocateEntriesOnPlaylistLoad())
						{
							libraryFiles = GUIDriver.getInstance().getMediaLibraryFileList();
						}
						else
						{
							libraryFiles = null;
						}
						ProgressWorker<Playlist, Void> worker = new ProgressWorker<Playlist, Void>()
						{
							@Override
							protected Playlist doInBackground() throws Exception
							{
								Playlist list = new Playlist(new File(data), this);
								if (libraryFiles != null)
								{
									list.repair(libraryFiles, this);
								}
								return list;
							}

							@Override
							protected void done()
							{
								Playlist list;
								try
								{
									list = get();
									_playlist.addAll(dl.getRow(), list.getEntries());
									if (_playlist.size() == list.size())
									{
										resizeAllColumns();
									}
								}
								catch (CancellationException ex)
								{
									return;
								}
								catch (Exception ex)
								{
									JOptionPane.showMessageDialog(PlaylistEditCtrl.this.getParentFrame(), ex, "Open Playlist Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
							}
						};
						ProgressDialog pd = new ProgressDialog((GUIScreen) getParentFrame(), true, worker, "Loading...");
						pd.setVisible(true);
						return true;
					}
					catch (UnsupportedFlavorException ex)
					{
						Logger.getLogger(PlaylistEditCtrl.class.getName()).log(Level.SEVERE, null, ex);
					}
					catch (IOException ex)
					{
						Logger.getLogger(PlaylistEditCtrl.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
				return false;
			}
;

			@Override
			public int getSourceActions(JComponent c)
			{
				return MOVE;
			}

			@Override
			protected Transferable createTransferable(JComponent c)
			{
				if (_isSortedByFileIx)
				{
					JTable table = (JTable) c;
					int[] rowIndexes = table.getSelectedRows();
					for (int i = 0; i < rowIndexes.length; i++)
					{
						rowIndexes[i] = _uiTable.convertRowIndexToModel(rowIndexes[i]);
					}
					try
					{
						return new PlaylistEntryList(_playlist.getSelectedEntries(rowIndexes));
					}
					catch (IOException ex)
					{
						Logger.getLogger(PlaylistEditCtrl.class.getName()).log(Level.SEVERE, null, ex);
						return null;
					}
				}
				else
				{
					return null;
				}
			}
		});

		_uiTable.setDropMode(DropMode.INSERT);
	}

	private class PlaylistTableModel extends AbstractTableModel
	{
		public int getRowCount()
		{
			if (_playlist != null)
			{
				return _playlist.size();
			}
			else
			{
				return 0;
			}
		}

		public int getColumnCount()
		{
			return 4;
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			PlaylistEntry entry = _playlist.get(rowIndex);
			switch (columnIndex)
			{
				case 0:
					return rowIndex + 1;
				case 1:
					if (entry.isURL())
					{
						return _imgUrl;
					}
					else if (entry.isFixed())
					{
						return _imgFixed;
					}
					else if (entry.isFound())
					{
						return _imgFound;
					}
					else
					{
						return _imgMissing;
					}
				case 2:
					return entry.getFileName();
				case 3:
					return entry.getPath();
				default:
					return null;
			}
		}

		@Override
		public String getColumnName(int column)
		{
			switch (column)
			{
				case 0:
					return "#";
				case 1:
					return "";
				case 2:
					return "File Name";
				case 3:
					return "Location";
				default:
					return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			switch (columnIndex)
			{
				case 0:
					return Integer.class;
				case 1:
					return ImageIcon.class;
				default:
					return Object.class;
			}
		}
	}

	private class IntRenderer extends DefaultTableCellRenderer
	{
		public IntRenderer()
		{
			super();
			setHorizontalAlignment(JLabel.RIGHT);
		}

		@Override
		protected void setValue(Object value)
		{
			setText((value == null) ? "" : _intFormatter.format(value));
		}
	}
}
