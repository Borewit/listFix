/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2010 Jeremy Caron
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.tree.*;

import listfix.controller.GUIDriver;
import listfix.controller.MediaLibraryOperator;

import listfix.io.BrowserLauncher;
import listfix.io.FileTreeNodeGenerator;
import listfix.io.FileWriter;
import listfix.io.PlaylistFileChooserFilter;
import listfix.io.PlaylistFileFilter;
import listfix.io.PlaylistScanner;
import listfix.io.UNCFile;
import listfix.io.WinampHelper;

import listfix.model.AppOptions;
import listfix.model.BatchRepair;
import listfix.model.BatchRepairItem;
import listfix.model.Playlist;
import listfix.model.PlaylistHistory;

import listfix.util.ArrayFunctions;
import listfix.util.FileTypeSearch;
import listfix.util.OperatingSystem;

import listfix.view.dialogs.ProgressDialog;
import listfix.view.dialogs.BatchRepairDialog;
import listfix.view.dialogs.AppOptionsDialog;

import listfix.view.support.IPlaylistModifiedListener;
import listfix.view.support.ClosableTabCtrl;
import listfix.view.support.ICloseableTabManager;
import listfix.view.support.ProgressWorker;

public final class GUIScreen extends JFrame implements ICloseableTabManager, DropTargetListener
{
	private static final long serialVersionUID = 7691786927987534889L;
	private final JFileChooser jM3UChooser;
	private final JFileChooser jMediaDirChooser;
	private final JFileChooser jSaveFileChooser;
	private GUIDriver guiDriver = null;
	private DropTarget dropTarget = null;

	/** Creates new form GUIScreen */
	public GUIScreen()
	{
		listfix.view.support.SplashScreen splashScreen = new listfix.view.support.SplashScreen("images/listfixSplashScreen.jpg");
		splashScreen.setStatusBar("Loading Media Library & Options...");
		guiDriver = GUIDriver.getInstance();
		splashScreen.setStatusBar("Initializing UI...");

		initComponents();

		jM3UChooser = new JFileChooser();
		jMediaDirChooser = new JFileChooser();
		jSaveFileChooser = new JFileChooser();

		setApplicationFont(guiDriver.getAppOptions().getAppFont());
		this.setLookAndFeel(guiDriver.getAppOptions().getLookAndFeel());
		
		jM3UChooser.setDialogTitle("Choose Playlists...");
		jM3UChooser.setAcceptAllFileFilterUsed(false);
		jM3UChooser.setFileFilter(new PlaylistFileChooserFilter());
		jM3UChooser.setMultiSelectionEnabled(true);

		jMediaDirChooser.setDialogTitle("Specify a media directory...");
		jMediaDirChooser.setAcceptAllFileFilterUsed(false);
		jMediaDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		jSaveFileChooser.setDialogTitle("Save File:");
		jSaveFileChooser.setAcceptAllFileFilterUsed(false);
		jSaveFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jSaveFileChooser.setFileFilter(new PlaylistFileChooserFilter());

		splashScreen.setVisible(false);

		if (guiDriver.getShowMediaDirWindow())
		{
			JOptionPane.showMessageDialog(this, "You need to add a media directory before you can find the new locations of your files.  See help for more information.", "Reminder", JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			_mediaLibraryList.setListData(guiDriver.getMediaDirs());
		}

		updateMediaDirButtons();
		updateRecentMenu();
		refreshStatusLabel(null);

		(new FileWriter()).writeIni(guiDriver.getMediaDirs(), guiDriver.getMediaLibraryDirectoryList(), guiDriver.getMediaLibraryFileList(), guiDriver.getAppOptions());

		((java.awt.CardLayout) _playlistPanel.getLayout()).show(_playlistPanel, "_gettingStartedPanel");

		initPlaylistListener();

		if (!OperatingSystem.isMac())
		{
			// The default L&F on macs doesn't support tab insets...
			addOpenPlaylistTabButton(_uiTabs);
		}

		if (!WinampHelper.isWinampInstalled())
		{
			_batchRepairWinampMenuItem.setVisible(false);
			_extractPlaylistsMenuItem.setVisible(false);
		}

		// drag-n-drop support for the playlist directory tree
		_playlistDirectoryTree.setTransferHandler(new TransferHandler()
		{
			@Override
			public boolean canImport(TransferHandler.TransferSupport info)
			{
				return false;
			}

			@Override
			public boolean importData(TransferHandler.TransferSupport info)
			{
				return false;
			}

			@Override
			public int getSourceActions(JComponent c)
			{
				return MOVE;
			}

			@Override
			protected Transferable createTransferable(JComponent c)
			{
				try
				{
					JTree table = (JTree) c;
					int selRow = _playlistDirectoryTree.getSelectionRows()[0];
					TreePath selPath = _playlistDirectoryTree.getPathForRow(selRow);
					return new StringSelection(FileTreeNodeGenerator.TreePathToFileSystemPath(selPath));
				}
				catch  (Exception exception)
				{
					return null;
				}

			}
		});

		dropTarget = new DropTarget(this, this);
		
		_playlistDirectoryTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent e)
			{
				boolean hasSelected = _playlistDirectoryTree.getSelectionCount() > 0;
				_openSelectedPlaylistsButton.setEnabled(hasSelected);
				_repairPlaylistsButton.setEnabled(hasSelected);
			}
		});
		
		// add popup menu to list, handle's right click actions.
		_playlistDirectoryTree.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				handleMouseEvent(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				handleMouseEvent(e);
			}

			private void handleMouseEvent(MouseEvent e)
			{				
				Point p = e.getPoint();
				if (e.isPopupTrigger())
				{
					int rowIx = _playlistDirectoryTree.getRowForLocation(p.x, p.y);
					boolean isOverItem = rowIx >= 0;
					if (isOverItem && (e.getModifiers() & ActionEvent.CTRL_MASK) > 0)
					{
						_playlistDirectoryTree.addSelectionRow(rowIx);
					}
					else if (isOverItem && !_playlistDirectoryTree.isRowSelected(rowIx))
					{
						_playlistDirectoryTree.setSelectionRow(rowIx);
					}

					if (_playlistDirectoryTree.getSelectionCount() > 0)
					{
						_miBatchPlaylistRepair.setEnabled(true);
						_miOpenSelectedPlaylists.setEnabled(true);
					}
					else
					{
						_miBatchPlaylistRepair.setEnabled(false);
						_miOpenSelectedPlaylists.setEnabled(false);
					}

					_miRefreshDirectoryTree.setEnabled(_playlistDirectoryTree.getRowCount() > 0);
					_playlistTreeRightClickMenu.show(e.getComponent(), p.x, p.y);
				}
				else
				{
					int selRow = _playlistDirectoryTree.getRowForLocation(p.x, p.y);
					TreePath selPath = _playlistDirectoryTree.getPathForLocation(p.x, p.y);
					if (selRow != -1)
					{
						if (e.getClickCount() == 2)
						{
							playlistDirectoryTreeNodeDoubleClicked(selPath);
						}
					}
					else
					{
						_playlistDirectoryTree.setSelectionRow(-1);
					}
				}
			}
		});
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde)
	{
		
	}

	@Override
	public void dragExit(DropTargetEvent dte)
	{
		
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde)
	{
		
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde)
	{
		
	}

	@Override
	public void drop(DropTargetDropEvent dtde)
	{
		try
		{
			// Ok, get the dropped object and try to figure out what it is
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavors = tr.getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++)
			{
				// Check for file lists specifically
				if (flavors[i].isFlavorJavaFileListType())
				{
					// Great!  Accept copy drops...
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

					// And add the list of file names to our text area
					java.util.List list = (java.util.List) tr.getTransferData(flavors[i]);
					for (int j = 0; j < list.size(); j++)
					{
						if (list.get(j) instanceof File && Playlist.isPlaylist((File)list.get(j)))
						{
							openPlaylist((File) list.get(j));
						}
					}

					// If we made it this far, everything worked.
					dtde.dropComplete(true);
					return;
				}
			}
			// Hmm, the user must not have dropped a file list
			dtde.rejectDrop();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			dtde.rejectDrop();
		}
	}

	public AppOptions getOptions()
	{
		return guiDriver.getAppOptions();
	}

	private void fireOptionsPopup()
	{
		String oldPlaylistsDirectory = guiDriver.getAppOptions().getPlaylistsDirectory();
		AppOptionsDialog optDialog = new AppOptionsDialog(this, "listFix() options", true, guiDriver.getAppOptions());
		AppOptions options = optDialog.showDialog();
		if (optDialog.getResultCode() == AppOptionsDialog.OK)
		{
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			guiDriver.setAppOptions(options);
			guiDriver.getHistory().setCapacity(options.getMaxPlaylistHistoryEntries());
			if (options.getAlwaysUseUNCPaths())
			{
				guiDriver.switchMediaLibraryToUNCPaths();
				_mediaLibraryList.setListData(guiDriver.getMediaDirs());
			}
			else
			{
				guiDriver.switchMediaLibraryToMappedDrives();
				_mediaLibraryList.setListData(guiDriver.getMediaDirs());
			}
			(new FileWriter()).writeIni(guiDriver.getMediaDirs(), guiDriver.getMediaLibraryDirectoryList(), guiDriver.getMediaLibraryFileList(), options);
			if (!oldPlaylistsDirectory.equals(options.getPlaylistsDirectory()))
			{
				_playlistDirectoryTree.setModel(new DefaultTreeModel(FileTreeNodeGenerator.addNodes(null, new File(guiDriver.getAppOptions().getPlaylistsDirectory()))));
			}
			updateRecentMenu();
			setApplicationFont(options.getAppFont());
			this.setLookAndFeel(options.getLookAndFeel());
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _playlistTreeRightClickMenu = new javax.swing.JPopupMenu();
        _miOpenSelectedPlaylists = new javax.swing.JMenuItem();
        _miBatchPlaylistRepair = new javax.swing.JMenuItem();
        _miRefreshDirectoryTree = new javax.swing.JMenuItem();
        _statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        _splitPane = new javax.swing.JSplitPane();
        _leftSplitPane = new javax.swing.JSplitPane();
        _mediaLibraryPanel = new javax.swing.JPanel();
        _mediaLibraryButtonPanel = new javax.swing.JPanel();
        _addMediaDirButton = new javax.swing.JButton();
        _removeMediaDirButton = new javax.swing.JButton();
        _refreshMediaDirsButton = new javax.swing.JButton();
        _mediaLibraryScrollPane = new javax.swing.JScrollPane();
        _mediaLibraryList = new javax.swing.JList(new String[] {"Please Add A Media Directory..."});
        _playlistDirectoryPanel = new javax.swing.JPanel();
        _treeScrollPane = new javax.swing.JScrollPane();
        _playlistDirectoryTree = new javax.swing.JTree(FileTreeNodeGenerator.addNodes(null, new File(guiDriver.getAppOptions().getPlaylistsDirectory())));
        jPanel1 = new javax.swing.JPanel();
        _setPlaylistsDirectoryButton = new javax.swing.JButton();
        _refreshPlaylistPanelButton = new javax.swing.JButton();
        _openSelectedPlaylistsButton = new javax.swing.JButton();
        _repairPlaylistsButton = new javax.swing.JButton();
        _playlistPanel = new javax.swing.JPanel();
        _gettingStartedPanel = new javax.swing.JPanel();
        _verticalPanel = new javax.swing.JPanel();
        _openIconButton = new javax.swing.JButton();
        _spacerPanel = new javax.swing.JPanel();
        _newIconButton = new javax.swing.JButton();
        _uiTabs = new javax.swing.JTabbedPane();
        _mainMenuBar = new javax.swing.JMenuBar();
        _fileMenu = new javax.swing.JMenu();
        _newPlaylistMenuItem = new javax.swing.JMenuItem();
        _loadMenuItem = new javax.swing.JMenuItem();
        _closeMenuItem = new javax.swing.JMenuItem();
        _closeAllMenuItem = new javax.swing.JMenuItem();
        _saveMenuItem = new javax.swing.JMenuItem();
        _saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        _miBatchRepair = new javax.swing.JMenuItem();
        _batchRepairWinampMenuItem = new javax.swing.JMenuItem();
        _extractPlaylistsMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        recentMenu = new javax.swing.JMenu();
        _clearHistoryMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        _appOptionsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        _exitMenuItem = new javax.swing.JMenuItem();
        _helpMenu = new javax.swing.JMenu();
        _helpMenuItem = new javax.swing.JMenuItem();
        _updateCheckMenuItem = new javax.swing.JMenuItem();
        _aboutMenuItem = new javax.swing.JMenuItem();

        _miOpenSelectedPlaylists.setText("Open");
        _miOpenSelectedPlaylists.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _miOpenSelectedPlaylistsActionPerformed(evt);
            }
        });
        _playlistTreeRightClickMenu.add(_miOpenSelectedPlaylists);

        _miBatchPlaylistRepair.setText("Repair Playlist(s)...");
        _miBatchPlaylistRepair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _miBatchPlaylistRepairActionPerformed(evt);
            }
        });
        _playlistTreeRightClickMenu.add(_miBatchPlaylistRepair);

        _miRefreshDirectoryTree.setText("Refresh");
        _miRefreshDirectoryTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _miRefreshDirectoryTreeActionPerformed(evt);
            }
        });
        _playlistTreeRightClickMenu.add(_miRefreshDirectoryTree);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("listFix( ) - v2.2.0");
        setMinimumSize(new java.awt.Dimension(600, 149));
        setName("mainFrame"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        _statusPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        _statusPanel.setLayout(new java.awt.BorderLayout());

        statusLabel.setForeground(new java.awt.Color(75, 75, 75));
        statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        statusLabel.setText("Untitled List     Number of entries in list: 0     Number of lost entries: 0     Number of URLs: 0");
        _statusPanel.add(statusLabel, java.awt.BorderLayout.WEST);

        getContentPane().add(_statusPanel, java.awt.BorderLayout.SOUTH);

        _splitPane.setDividerSize(7);
        _splitPane.setMaximumSize(null);
        _splitPane.setOneTouchExpandable(true);
        _splitPane.setPreferredSize(new java.awt.Dimension(785, 489));

        _leftSplitPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        _leftSplitPane.setDividerLocation(280);
        _leftSplitPane.setDividerSize(7);
        _leftSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        _leftSplitPane.setMaximumSize(null);
        _leftSplitPane.setOneTouchExpandable(true);
        _leftSplitPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                _leftSplitPaneResized(evt);
            }
        });

        _mediaLibraryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Media Directories", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP));
        _mediaLibraryPanel.setAlignmentX(0.0F);
        _mediaLibraryPanel.setAlignmentY(0.0F);
        _mediaLibraryPanel.setLayout(new java.awt.BorderLayout());

        _mediaLibraryButtonPanel.setMinimumSize(new java.awt.Dimension(223, 31));

        _addMediaDirButton.setText("Add");
        _addMediaDirButton.setToolTipText("Where do you keep your music?");
        _addMediaDirButton.setFocusable(false);
        _addMediaDirButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        _addMediaDirButton.setMinimumSize(new java.awt.Dimension(53, 25));
        _addMediaDirButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _addMediaDirButtonActionPerformed(evt);
            }
        });
        _mediaLibraryButtonPanel.add(_addMediaDirButton);

        _removeMediaDirButton.setText("Remove");
        _removeMediaDirButton.setToolTipText("Remove a directory from the search list");
        _removeMediaDirButton.setFocusable(false);
        _removeMediaDirButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        _removeMediaDirButton.setMinimumSize(new java.awt.Dimension(73, 25));
        _removeMediaDirButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _removeMediaDirButtonActionPerformed(evt);
            }
        });
        _mediaLibraryButtonPanel.add(_removeMediaDirButton);

        _refreshMediaDirsButton.setText("Refresh");
        _refreshMediaDirsButton.setToolTipText("The contents of your media library are cached, refresh to pickup changes");
        _refreshMediaDirsButton.setFocusable(false);
        _refreshMediaDirsButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        _refreshMediaDirsButton.setMinimumSize(new java.awt.Dimension(71, 25));
        _refreshMediaDirsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _refreshMediaDirsButtonActionPerformed(evt);
            }
        });
        _mediaLibraryButtonPanel.add(_refreshMediaDirsButton);

        _mediaLibraryPanel.add(_mediaLibraryButtonPanel, java.awt.BorderLayout.SOUTH);

        _mediaLibraryList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        _mediaLibraryList.setPreferredSize(null);
        _mediaLibraryScrollPane.setViewportView(_mediaLibraryList);

        _mediaLibraryPanel.add(_mediaLibraryScrollPane, java.awt.BorderLayout.CENTER);

        _leftSplitPane.setBottomComponent(_mediaLibraryPanel);

        _playlistDirectoryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Playlists Directory", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP));
        _playlistDirectoryPanel.setAlignmentX(0.0F);
        _playlistDirectoryPanel.setAlignmentY(0.0F);
        _playlistDirectoryPanel.setLayout(new java.awt.BorderLayout());

        _playlistDirectoryTree.setDragEnabled(true);
        _playlistDirectoryTree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                _playlistDirectoryTreeKeyReleased(evt);
            }
        });
        _treeScrollPane.setViewportView(_playlistDirectoryTree);

        _playlistDirectoryPanel.add(_treeScrollPane, java.awt.BorderLayout.CENTER);

        _setPlaylistsDirectoryButton.setText("Set");
        _setPlaylistsDirectoryButton.setToolTipText("Choose a folder (recursively searched for playlists to be shown here)");
        _setPlaylistsDirectoryButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        _setPlaylistsDirectoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _setPlaylistsDirectoryButtonActionPerformed(evt);
            }
        });
        jPanel1.add(_setPlaylistsDirectoryButton);

        _refreshPlaylistPanelButton.setText("Refresh");
        _refreshPlaylistPanelButton.setToolTipText("Refresh Playlists Tree");
        _refreshPlaylistPanelButton.setFocusable(false);
        _refreshPlaylistPanelButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        _refreshPlaylistPanelButton.setMinimumSize(new java.awt.Dimension(71, 25));
        _refreshPlaylistPanelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _refreshPlaylistPanelButtonActionPerformed(evt);
            }
        });
        jPanel1.add(_refreshPlaylistPanelButton);

        _openSelectedPlaylistsButton.setText("Open");
        _openSelectedPlaylistsButton.setToolTipText("Open Selected Playlist(s)");
        _openSelectedPlaylistsButton.setEnabled(false);
        _openSelectedPlaylistsButton.setFocusable(false);
        _openSelectedPlaylistsButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        _openSelectedPlaylistsButton.setMinimumSize(new java.awt.Dimension(71, 25));
        _openSelectedPlaylistsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _openSelectedPlaylistsButtonActionPerformed(evt);
            }
        });
        jPanel1.add(_openSelectedPlaylistsButton);

        _repairPlaylistsButton.setText("Repair");
        _repairPlaylistsButton.setToolTipText("Repair Selected Playlist(s)");
        _repairPlaylistsButton.setEnabled(false);
        _repairPlaylistsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _repairPlaylistsButtonActionPerformed(evt);
            }
        });
        jPanel1.add(_repairPlaylistsButton);

        _playlistDirectoryPanel.add(jPanel1, java.awt.BorderLayout.PAGE_END);

        _leftSplitPane.setTopComponent(_playlistDirectoryPanel);

        _splitPane.setLeftComponent(_leftSplitPane);

        _playlistPanel.setBackground(java.awt.SystemColor.window);
        _playlistPanel.setLayout(new java.awt.CardLayout());

        _gettingStartedPanel.setBackground(new java.awt.Color(255, 255, 255));
        _gettingStartedPanel.setLayout(new java.awt.GridBagLayout());

        _verticalPanel.setBackground(new java.awt.Color(255, 255, 255));
        _verticalPanel.setLayout(new javax.swing.BoxLayout(_verticalPanel, javax.swing.BoxLayout.Y_AXIS));

        _openIconButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open-big.png"))); // NOI18N
        _openIconButton.setText("Open A Playlist");
        _openIconButton.setToolTipText("Open A Playlist");
        _openIconButton.setAlignmentY(0.0F);
        _openIconButton.setFocusable(false);
        _openIconButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _openIconButton.setIconTextGap(-2);
        _openIconButton.setMaximumSize(new java.awt.Dimension(220, 180));
        _openIconButton.setMinimumSize(new java.awt.Dimension(220, 180));
        _openIconButton.setPreferredSize(new java.awt.Dimension(220, 180));
        _openIconButton.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        _openIconButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _openIconButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _openIconButtonActionPerformed1(evt);
            }
        });
        _verticalPanel.add(_openIconButton);

        _spacerPanel.setBackground(new java.awt.Color(255, 255, 255));
        _verticalPanel.add(_spacerPanel);

        _newIconButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/icon_new_file.png"))); // NOI18N
        _newIconButton.setText("New Playlist");
        _newIconButton.setToolTipText("New Playlist");
        _newIconButton.setAlignmentY(0.0F);
        _newIconButton.setFocusable(false);
        _newIconButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _newIconButton.setIconTextGap(3);
        _newIconButton.setMaximumSize(new java.awt.Dimension(220, 180));
        _newIconButton.setMinimumSize(new java.awt.Dimension(220, 180));
        _newIconButton.setPreferredSize(new java.awt.Dimension(220, 180));
        _newIconButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _newIconButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _newIconButtonActionPerformed(evt);
            }
        });
        _verticalPanel.add(_newIconButton);

        _gettingStartedPanel.add(_verticalPanel, new java.awt.GridBagConstraints());

        _playlistPanel.add(_gettingStartedPanel, "_gettingStartedPanel");

        _uiTabs.setFocusable(false);
        _uiTabs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                onTabStateChanged(evt);
            }
        });
        _playlistPanel.add(_uiTabs, "_uiTabs");

        _splitPane.setRightComponent(_playlistPanel);

        getContentPane().add(_splitPane, java.awt.BorderLayout.CENTER);

        _mainMenuBar.setBorder(null);

        _fileMenu.setMnemonic('F');
        _fileMenu.setText("File");

        _newPlaylistMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        _newPlaylistMenuItem.setMnemonic('L');
        _newPlaylistMenuItem.setText("New Playlist");
        _newPlaylistMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _newPlaylistMenuItemActionPerformed(evt);
            }
        });
        _fileMenu.add(_newPlaylistMenuItem);

        _loadMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        _loadMenuItem.setMnemonic('L');
        _loadMenuItem.setText("Open Playlist");
        _loadMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openIconButtonActionPerformed(evt);
            }
        });
        _fileMenu.add(_loadMenuItem);

        _closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        _closeMenuItem.setText("Close");
        _closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _closeMenuItemActionPerformed(evt);
            }
        });
        _fileMenu.add(_closeMenuItem);

        _closeAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        _closeAllMenuItem.setText("Close All");
        _closeAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _closeAllMenuItemActionPerformed(evt);
            }
        });
        _fileMenu.add(_closeAllMenuItem);

        _saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        _saveMenuItem.setMnemonic('S');
        _saveMenuItem.setText("Save");
        _saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _saveMenuItemActionPerformed(evt);
            }
        });
        _fileMenu.add(_saveMenuItem);

        _saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        _saveAsMenuItem.setMnemonic('V');
        _saveAsMenuItem.setText("Save As");
        _saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _saveAsMenuItemActionPerformed(evt);
            }
        });
        _fileMenu.add(_saveAsMenuItem);

        jSeparator6.setForeground(new java.awt.Color(102, 102, 153));
        _fileMenu.add(jSeparator6);

        _miBatchRepair.setText("Batch Repair...");
        _miBatchRepair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onMenuBatchRepairActionPerformed(evt);
            }
        });
        _fileMenu.add(_miBatchRepair);

        _batchRepairWinampMenuItem.setText("Batch Repair Winamp Playlists...");
        _batchRepairWinampMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _batchRepairWinampMenuItemActionPerformed(evt);
            }
        });
        _fileMenu.add(_batchRepairWinampMenuItem);

        _extractPlaylistsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        _extractPlaylistsMenuItem.setText("Extract Winamp Playlists");
        _extractPlaylistsMenuItem.setToolTipText("Extract Winamp Media Library Playlists");
        _extractPlaylistsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _extractPlaylistsMenuItemActionPerformed(evt);
            }
        });
        _fileMenu.add(_extractPlaylistsMenuItem);

        jSeparator3.setForeground(new java.awt.Color(102, 102, 153));
        _fileMenu.add(jSeparator3);

        recentMenu.setText("Recent Playlists");
        recentMenu.setToolTipText("Recently Opened Playlists");
        _fileMenu.add(recentMenu);

        _clearHistoryMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        _clearHistoryMenuItem.setMnemonic('H');
        _clearHistoryMenuItem.setText("Clear Playlist History");
        _clearHistoryMenuItem.setToolTipText("");
        _clearHistoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _clearHistoryMenuItemActionPerformed(evt);
            }
        });
        _fileMenu.add(_clearHistoryMenuItem);

        jSeparator1.setForeground(new java.awt.Color(102, 102, 153));
        _fileMenu.add(jSeparator1);

        _appOptionsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.ALT_MASK));
        _appOptionsMenuItem.setMnemonic('H');
        _appOptionsMenuItem.setText("Options...");
        _appOptionsMenuItem.setToolTipText("");
        _appOptionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _appOptionsMenuItemActionPerformed(evt);
            }
        });
        _fileMenu.add(_appOptionsMenuItem);

        jSeparator2.setForeground(new java.awt.Color(102, 102, 153));
        _fileMenu.add(jSeparator2);

        _exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        _exitMenuItem.setMnemonic('x');
        _exitMenuItem.setText("Exit");
        _exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _exitMenuItemActionPerformed(evt);
            }
        });
        _fileMenu.add(_exitMenuItem);

        _mainMenuBar.add(_fileMenu);

        _helpMenu.setMnemonic('H');
        _helpMenu.setText("Help");

        _helpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        _helpMenuItem.setMnemonic('e');
        _helpMenuItem.setText("Help");
        _helpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _helpMenuItemActionPerformed(evt);
            }
        });
        _helpMenu.add(_helpMenuItem);

        _updateCheckMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_MASK));
        _updateCheckMenuItem.setText("Check For Updates");
        _updateCheckMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _updateCheckMenuItemActionPerformed(evt);
            }
        });
        _helpMenu.add(_updateCheckMenuItem);

        _aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.ALT_MASK));
        _aboutMenuItem.setMnemonic('A');
        _aboutMenuItem.setText("About");
        _aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _aboutMenuItemActionPerformed(evt);
            }
        });
        _helpMenu.add(_aboutMenuItem);

        _mainMenuBar.add(_helpMenu);

        setJMenuBar(_mainMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void launchBatchPlaylistRepair()
	{
		TreePath[] paths = _playlistDirectoryTree.getSelectionPaths();
		List<File> files = new ArrayList<File>();
		for (TreePath path : paths)
		{
			File file = new File(FileTreeNodeGenerator.TreePathToFileSystemPath(path));
			if (file.isFile())
			{
				files.add(file);
			}
			else
			{
				files.addAll(PlaylistScanner.getAllPlaylists(file));
			}
		}
		if (files.isEmpty())
		{
			return;
		}
		BatchRepair br = new BatchRepair(guiDriver.getMediaLibraryFileList(), files.get(0));
		br.setDescription("Batch Repair");
		for (File file : files)
		{
			br.add(new BatchRepairItem(file));
		}
		BatchRepairDialog dlg = new BatchRepairDialog(this, true, br);
		if (!dlg.getUserCancelled())
		{
			dlg.setLocationRelativeTo(this);
			dlg.setVisible(true);
			updatePlaylistDirectoryPanel();
		}
	}

	private void openTreeSelectedPlaylists()
	{
		int[] selRows = _playlistDirectoryTree.getSelectionRows();
		if (selRows != null && selRows.length > 0)
		{
			for (int i : selRows)
			{
				TreePath selPath = _playlistDirectoryTree.getPathForRow(i);
				File toOpen = new File(FileTreeNodeGenerator.TreePathToFileSystemPath(selPath));
				if (toOpen.isDirectory())
				{
					// not yet supported, need a method to generate a list of all playlists under a folder...
					List<File> files = (new FileTypeSearch()).findFiles(toOpen, new PlaylistFileFilter());
					for (File f : files)
					{
						openPlaylist(f);
					}
				}
				else
				{
					openPlaylist(toOpen);
				}
			}
		}
	}

	private void playlistDirectoryTreeNodeDoubleClicked(TreePath selPath)
	{
		File toOpen = new File(FileTreeNodeGenerator.TreePathToFileSystemPath(selPath));
		if (!toOpen.isDirectory() && toOpen.exists())
		{
			this.openPlaylist(toOpen);
		}
	}

	private void _clearHistoryMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__clearHistoryMenuItemActionPerformed
	{//GEN-HEADEREND:event__clearHistoryMenuItemActionPerformed
		guiDriver.clearM3UHistory();
		updateRecentMenu();
	}//GEN-LAST:event__clearHistoryMenuItemActionPerformed

	private void _saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__saveAsMenuItemActionPerformed
	{//GEN-HEADEREND:event__saveAsMenuItemActionPerformed
		if (_currentPlaylist == null)
		{
			return;
		}

		jSaveFileChooser.setSelectedFile(_currentPlaylist.getFile());
		int rc = jSaveFileChooser.showSaveDialog(this);
		if (rc == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				File playlist = jSaveFileChooser.getSelectedFile();

				// prompt for confirmation if the file already exists...
				if (playlist.exists())
				{
					int result = JOptionPane.showConfirmDialog(this, "You picked a file that already exists, should I really overwrite it?", "File Exists:", JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.NO_OPTION)
					{
						return;
					}
				}

				// make sure file has correct extension
				String normalizedName = playlist.getName().trim().toLowerCase();
				if (!Playlist.isPlaylist(playlist)
					|| (!normalizedName.endsWith(".m3u") && !normalizedName.endsWith(".m3u8") && !normalizedName.endsWith(".pls")))
				{
					if (_currentPlaylist.isUtfFormat())
					{
						playlist = new File(playlist.getPath() + ".m3u8");
					}
					else
					{
						playlist = new File(playlist.getPath() + ".m3u");
					}
				}

				final File finalPlaylistFile = playlist;
				String oldPath = _currentPlaylist.getFile().getCanonicalPath();
				ProgressWorker worker = new ProgressWorker<Void, Void>()
				{
					@Override
					protected Void doInBackground() throws IOException
					{
						boolean saveRelative = GUIDriver.getInstance().getAppOptions().getSavePlaylistsWithRelativePaths();
						_currentPlaylist.saveAs(finalPlaylistFile, saveRelative, this);
						return null;
					}
				};
				ProgressDialog pd = new ProgressDialog(this, true, worker, "Saving...", false);
				pd.setVisible(true);

				worker.get();
				int tabIx = getPlaylistTabIx(_currentPlaylist);
				String newPath = finalPlaylistFile.getCanonicalPath();
				_uiTabs.setToolTipTextAt(tabIx, newPath);
				_pathToEditorMap.put(newPath, _pathToEditorMap.get(oldPath));
				_pathToEditorMap.remove(oldPath);
				updatePlaylistDirectoryPanel();

				// update playlist history
				guiDriver.getHistory().add(newPath);
				(new FileWriter()).writeMruPlaylists(guiDriver.getHistory());
				updateRecentMenu();
			}
			catch (CancellationException exception)
			{
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
	}//GEN-LAST:event__saveAsMenuItemActionPerformed

	private void openIconButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openIconButtonActionPerformed
	{//GEN-HEADEREND:event_openIconButtonActionPerformed
		if (_currentPlaylist != null)
		{
			jM3UChooser.setSelectedFile(_currentPlaylist.getFile());
		}
		int response = jM3UChooser.showOpenDialog(this);
		if (response == JFileChooser.APPROVE_OPTION)
		{
			File[] playlists = jM3UChooser.getSelectedFiles();
			for (File file : playlists)
				this.openPlaylist(file);
		}
	}//GEN-LAST:event_openIconButtonActionPerformed

	private void openPlaylist(final File file)
	{
		// do nothing if the file is already open.
		for (Playlist list : _playlistToEditorMap.keySet())
		{
			if (list.getFile().equals(file))
			{
				_uiTabs.setSelectedComponent(_playlistToEditorMap.get(list));
				return;
			}
		}

		final String path;
		try
		{
			if (file.exists())
			{
				path = file.getCanonicalPath();
			}
			else
			{
				JOptionPane.showMessageDialog(this, "File Not Found.", "Open Playlist Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(this, ex, "Open Playlist Error", JOptionPane.ERROR_MESSAGE);
			return;
		}


		final String[] libraryFiles;
		if (guiDriver.getAppOptions().getAutoLocateEntriesOnPlaylistLoad())
		{
			libraryFiles = GUIDriver.getInstance().getMediaLibraryFileList();
		}
		else
		{
			libraryFiles = null;
		}

		int tabIx = getPlaylistTabIx(path);
		if (tabIx == -1)
		{
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
						JOptionPane.showMessageDialog(GUIScreen.this, ex, "Open Playlist Error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					PlaylistEditCtrl editor = new PlaylistEditCtrl();
					editor.setPlaylist(list);
					String title = list.getFilename();
					_uiTabs.addTab(title, null, editor, path);
					int ix = _uiTabs.getTabCount() - 1;
					_uiTabs.setSelectedIndex(ix);
					_pathToEditorMap.put(path, editor);
					_playlistToEditorMap.put(list, editor);

					// add custom tab controls
					_uiTabs.setTabComponentAt(ix, new ClosableTabCtrl(GUIScreen.this, _uiTabs, title));

					// update playlist history
					PlaylistHistory history = guiDriver.getHistory();
					history.add(path);
					(new FileWriter()).writeMruPlaylists(history);

					updateRecentMenu();

					// update title and status bar if list was modified during loading
					if (list.isModified())
					{
						onPlaylistModified(list);
					}

					((java.awt.CardLayout) _playlistPanel.getLayout()).show(_playlistPanel, "_uiTabs");
				}
			};
			ProgressDialog pd = new ProgressDialog(this, true, worker, "Loading '" + (file.getName().length() > 70 ? file.getName().substring(0,70) : file.getName()) + "'...", false);
			pd.setVisible(true);
		}
		else
		{
			_uiTabs.setSelectedIndex(tabIx);
		}

	}
	Map<String, PlaylistEditCtrl> _pathToEditorMap = new HashMap<String, PlaylistEditCtrl>();
	Map<Playlist, PlaylistEditCtrl> _playlistToEditorMap = new HashMap<Playlist, PlaylistEditCtrl>();

	private void addOpenPlaylistTabButton(JTabbedPane tabCtrl)
	{
		Insets oldInsets = getTabAreaInsets();
		if (oldInsets == null)
		{
			return;
		}

		ImageIcon img = new ImageIcon(getClass().getResource("/images/open-small.gif"));
		int width = img.getIconWidth() + 4;
		int height = img.getIconHeight() + 4;

		// these settings work for putting the button on the left
		//_btnOpenTab.setForcedBounds(oldInsets.left, oldInsets.top, width, height);
		//Insets newInsets = new Insets(oldInsets.top, _btnOpenTab.getWidth() + 4 + oldInsets.left, oldInsets.bottom, oldInsets.right);

		int rightPad = width + 2 + oldInsets.right;
		final Rectangle bounds = new Rectangle(rightPad, oldInsets.top, width, height);

		// reserve space for open button
		Insets newInsets = new Insets(oldInsets.top, oldInsets.left, oldInsets.bottom, rightPad);
		setTabAreaInsets(newInsets);
		_tabPaneInsets = newInsets;

		// add open button
		final TButton button = new TButton(img);
		button.setToolTipText("Open Playlist");
		button.setForcedBounds(tabCtrl.getWidth() - bounds.x, bounds.y, bounds.width, bounds.height);
		tabCtrl.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				Component comp = e.getComponent();
				button.setForcedBounds(comp.getWidth() - bounds.x, bounds.y, bounds.width, bounds.height);
			}
		});
		tabCtrl.add(button);
	}
	private Insets _tabPaneInsets;

	private void setTabAreaInsets(Insets tabAreaInsets)
	{
		TabbedPaneUI ui = _uiTabs.getUI();
		if (ui instanceof BasicTabbedPaneUI)
		{
			try
			{
				Class<?> uiClass = ui.getClass();
				while (uiClass != null)
				{
					try
					{
						Field field = uiClass.getDeclaredField("tabAreaInsets");
						if (field != null)
						{
							field.setAccessible(true);
							field.set(ui, tabAreaInsets);
							_uiTabs.invalidate();
							return;
						}
					}
					catch (NoSuchFieldException ex)
					{
					}
					uiClass = uiClass.getSuperclass();
				}
			}
			catch (Throwable t)
			{
			}
		}

		throw new UnsupportedOperationException("LAF does not support tab area inserts");
	}

	private Insets getTabAreaInsets()
	{
		TabbedPaneUI ui = _uiTabs.getUI();
		if (ui instanceof BasicTabbedPaneUI)
		{
			try
			{
				Class<?> uiClass = ui.getClass();
				while (uiClass != null)
				{
					try
					{
						Field field = uiClass.getDeclaredField("tabAreaInsets");
						if (field != null)
						{
							field.setAccessible(true);
							return (Insets) field.get(ui);
						}
					}
					catch (NoSuchFieldException nsfe)
					{
					}
					uiClass = uiClass.getSuperclass();
				}
			}
			catch (Throwable t)
			{
			}
		}
		return UIManager.getInsets("TabbedPane.tabAreaInsets");
	}

	static
	{
		Color highlight = UIManager.getColor("TabbedPane.highlight");
		_normalBorder = new LineBorder(highlight);
		_pressedBorder = new LineBorder(Color.black);
	}
	private static Border _normalBorder;
	private static Border _pressedBorder;

	public void updateCurrentTab(Playlist list)
	{
		PlaylistEditCtrl oldEditor = (PlaylistEditCtrl) _uiTabs.getSelectedComponent();
		int spot = _uiTabs.getSelectedIndex();

		((ClosableTabCtrl) _uiTabs.getTabComponentAt(spot)).setText(list.getFilename());
		oldEditor.setPlaylist(list, true);

		// update playlist history
		PlaylistHistory history = guiDriver.getHistory();
		try
		{
			history.add(list.getFile().getCanonicalPath());
		}
		catch (IOException ex)
		{
			Logger.getLogger(GUIScreen.class.getName()).log(Level.SEVERE, null, ex);
		}
		(new FileWriter()).writeMruPlaylists(history);

		updateRecentMenu();
	}

	public void updateRecentMenu()
	{
		recentMenu.removeAll();
		String[] files = guiDriver.getRecentM3Us();
		if (files.length == 0)
		{
			JMenuItem temp = new JMenuItem("Empty");
			temp.setEnabled(false);
			recentMenu.add(temp);
		}
		else
		{
			for (int i = 0; i < files.length; i++)
			{
				JMenuItem temp = new JMenuItem(files[i]);
				temp.addActionListener(new java.awt.event.ActionListener()
				{
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt)
					{
						recentPlaylistActionPerformed(evt);
					}
				});
				recentMenu.add(temp);
			}
		}
	}

	private void updateAllComponentTreeUIs()
	{
		SwingUtilities.updateComponentTreeUI(this);
		SwingUtilities.updateComponentTreeUI(jM3UChooser);
		SwingUtilities.updateComponentTreeUI(jMediaDirChooser);
		SwingUtilities.updateComponentTreeUI(jSaveFileChooser);
		SwingUtilities.updateComponentTreeUI(_playlistTreeRightClickMenu);
		SwingUtilities.updateComponentTreeUI(_uiTabs);
	}

	class TButton extends JButton implements UIResource
	{
		public TButton(Icon icon)
		{
			super(icon);

			setContentAreaFilled(false);
			setFocusable(false);
			setBorder(_normalBorder);
			setBorderPainted(false);

			createMouseListener();
			createOpenActionListener();
		}

		public TButton(String text, Icon icon)
		{
			super(text, icon);

			setFocusable(false);
			setToolTipText(text);
			setAlignmentY(0.0F);
			setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
			setMargin(new java.awt.Insets(4, 10, 4, 10));

			createOpenActionListener();
		}

		private void createMouseListener()
		{
			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseEntered(MouseEvent e)
				{
					setBorderPainted(true);
				}

				@Override
				public void mouseExited(MouseEvent e)
				{
					setBorderPainted(false);
				}

				@Override
				public void mousePressed(MouseEvent e)
				{
					setBorder(_pressedBorder);
				}

				@Override
				public void mouseReleased(MouseEvent e)
				{
					setBorder(_normalBorder);
				}
			});
		}

		private void createOpenActionListener()
		{
			addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(java.awt.event.ActionEvent evt)
				{
					openIconButtonActionPerformed(evt);
				}
			});
		}

		public void setForcedBounds(int x, int y, int width, int height)
		{
			_allowBoundsSetting = true;
			setBounds(x, y, width, height);
			_allowBoundsSetting = false;
		}

		@Override
		public void setBounds(int x, int y, int width, int height)
		{
			if (_allowBoundsSetting)
			{
				super.setBounds(x, y, width, height);
			}
		}
		private boolean _allowBoundsSetting = true;
	}

	private int getPlaylistTabIx(String path)
	{
		PlaylistEditCtrl ctrl = _pathToEditorMap.get(path);
		if (ctrl != null)
		{
			return _uiTabs.indexOfComponent(ctrl);
		}
		else
		{
			return -1;
		}
	}

	private int getPlaylistTabIx(Playlist list)
	{
		PlaylistEditCtrl ctrl = _playlistToEditorMap.get(list);
		if (ctrl != null)
		{
			return _uiTabs.indexOfComponent(ctrl);
		}
		else
		{
			return -1;
		}
	}

	private void onSelectedTabChanged()
	{
		int tabIx = _uiTabs.getSelectedIndex();
		Playlist list = getPlaylistFromTab(tabIx);
		if (list == _currentPlaylist)
		{
			return;
		}

		// remove listeners from current playlist
		if (_currentPlaylist != null)
		{
			_currentPlaylist.removeModifiedListener(_playlistListener);
		}

		_currentPlaylist = list;
		if (_currentPlaylist == null)
		{
			// clear status label
			statusLabel.setText("No list loaded");
		}
		else
		{
			// set status label
			refreshStatusLabel(_currentPlaylist);

			// add listeners to current playlist
			_currentPlaylist.addModifiedListener(_playlistListener);

		}
	}
	Playlist _currentPlaylist;

	private Playlist getPlaylistFromTab(int tabIx)
	{
		if (tabIx < 0)
		{
			return null;
		}
		Component comp = _uiTabs.getComponentAt(tabIx);
		return comp != null ? ((PlaylistEditCtrl) _uiTabs.getComponentAt(tabIx)).getPlaylist() : null;
	}

	private void initPlaylistListener()
	{
		_playlistListener = new IPlaylistModifiedListener()
		{
			@Override
			public void playlistModified(Playlist list)
			{
				onPlaylistModified(list);
			}
		};
	}
	IPlaylistModifiedListener _playlistListener;

	private void onPlaylistModified(Playlist list)
	{
		refreshStatusLabel(list);
		int tabIx = getPlaylistTabIx(list);
		if (tabIx != -1)
		{
			String title;
			if (list.isModified())
			{
				title = list.getFilename() + "*";
			}
			else
			{
				title = list.getFilename();
			}
			_uiTabs.setTitleAt(tabIx, title);
			((ClosableTabCtrl) _uiTabs.getTabComponentAt(tabIx)).setText(title);
		}
	}

	private void refreshStatusLabel(Playlist list)
	{
		if (list != null)
		{
			String fmt = "Currently Open: %s%s     Number of entries in list: %d     Number of lost entries: %d     Number of URLs: %d";
			String txt = String.format(fmt, list.getFilename(), list.isModified() ? "*" : "", list.size(), list.getMissingCount(), list.getUrlCount());
			statusLabel.setText(txt);
		}
		else
		{
			statusLabel.setText("No list loaded");
		}
	}

	@Override
	public boolean tryCloseAllTabs()
	{
		boolean result = true;
		while (_uiTabs.getTabCount() > 0 && result)
		{
			_uiTabs.setSelectedIndex(0);
			result = result && tryCloseTab((ClosableTabCtrl)_uiTabs.getTabComponentAt(0));
		}
		return result;
	}

	@Override
	public void closeAllOtherTabs(int tabIx)
	{
		if (_uiTabs.getTabCount() > 1)
		{
			Component comp = _uiTabs.getComponentAt(tabIx);
			boolean success = true;
			while (success && !_uiTabs.getComponentAt(0).equals(comp))
			{
				_uiTabs.setSelectedIndex(0);
				success = success && tryCloseTab((ClosableTabCtrl)_uiTabs.getTabComponentAt(0));
			}
			while (_uiTabs.getTabCount() > 1 && success)
			{
				_uiTabs.setSelectedIndex(1);
				success = success && tryCloseTab((ClosableTabCtrl)_uiTabs.getTabComponentAt(1));
			}
		}
	}

	@Override
	public boolean tryCloseTab(ClosableTabCtrl ctrl)
	{
		int tabIx = _uiTabs.indexOfTabComponent(ctrl);
		if (tabIx == -1)
		{
			return false;
		}
		final Playlist list = getPlaylistFromTab(tabIx);
		if (list.isModified())
		{
			Object[] options =
			{
				"Save", "Don't Save", "Cancel"
			};
			int rc = JOptionPane.showOptionDialog(this, "This playlist has been modified. Do you want to save the changes?", "Confirm Close",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
			if (rc == JOptionPane.YES_OPTION)
			{
				// TODO: not tested
				ProgressWorker<Boolean, Void> worker = new ProgressWorker<Boolean, Void>()
				{
					@Override
					protected Boolean doInBackground() throws IOException
					{
						boolean saveRelative = GUIDriver.getInstance().getAppOptions().getSavePlaylistsWithRelativePaths();
						list.save(saveRelative, this);
						return true;
					}
				};
				ProgressDialog pd = new ProgressDialog(this, true, worker, "Saving...", false);
				pd.setVisible(true);

				try
				{
					boolean savedOk = worker.get();
					if (savedOk)
					{
						closeTab(list, tabIx);
						return true;
					}
				}
				catch (InterruptedException ex)
				{
				}
				catch (ExecutionException ex)
				{
					JOptionPane.showMessageDialog(GUIScreen.this, ex.getCause(), "Save Error", JOptionPane.ERROR_MESSAGE);
				}

				return false;
			}
			else if (rc == JOptionPane.NO_OPTION)
			{
				closeTab(list, tabIx);
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			closeTab(list, tabIx);
			return true;
		}
	}

	private void closeTab(Playlist list, int tabIx)
	{
		_uiTabs.remove(tabIx);
		_playlistToEditorMap.remove(list);

		try
		{
			String path = list.getFile().getCanonicalPath();
			_pathToEditorMap.remove(path);
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(this, ex, "Close Playlist Error (ignoring)", JOptionPane.ERROR_MESSAGE);
		}

		if (_uiTabs.getTabCount() <= 0)
		{
			((java.awt.CardLayout) _playlistPanel.getLayout()).show(_playlistPanel, "_gettingStartedPanel");
		}
	}

	private void confirmCloseApp()
	{
		Set<Playlist> lists = _playlistToEditorMap.keySet();
		for (Playlist list : lists)
		{
			if (list.isModified())
			{

				Object[] options =
				{
					"Discard Changes and Exit", "Cancel"
				};
				int rc = JOptionPane.showOptionDialog(this, "You have unsaved changes. Do you really want to discard these changes and exit?\n", "Confirm Close",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
				if (rc == JOptionPane.NO_OPTION)
				{
					return;
				}
				else
				{
					break;
				}
			}
		}

		System.exit(0);
	}

	private void _addMediaDirButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__addMediaDirButtonActionPerformed
	{//GEN-HEADEREND:event__addMediaDirButtonActionPerformed
		int response = jMediaDirChooser.showOpenDialog(this);
		if (response == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				UNCFile mediaDir = new UNCFile(jMediaDirChooser.getSelectedFile());
				if (guiDriver.getAppOptions().getAlwaysUseUNCPaths())
				{
					if (mediaDir.onNetworkDrive())
					{
						mediaDir = new UNCFile(mediaDir.getUNCPath());
					}
				}
				final String dir = mediaDir.getPath();
				if (guiDriver.getMediaDirs() != null && mediaDir != null)
				{
					// first let's see if this is a subdirectory of any of the media directories already in the list, and error out if so...
					if (ArrayFunctions.ContainsStringPrefixingAnotherString(guiDriver.getMediaDirs(), dir, !GUIDriver.fileSystemIsCaseSensitive))
					{
						JOptionPane.showMessageDialog(this, "The directory you attempted to add is a subdirectory of one already in your media library, no change was made.", "Reminder", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					else
					{
						// Now check if any of the media directories is a subdirectory of the one we're adding and remove the media directory if so.                        
						String[] dirsToCheck = guiDriver.getMediaDirs();
						for (int i = 0; i < dirsToCheck.length; i++)
						{
							int matchCount = 0;
							if (dirsToCheck[i].startsWith(dir))
							{
								if (matchCount == 0)
								{
									JOptionPane.showMessageDialog(this, "One or more of your existing media directories is a subdirectory of the directory you just added.  These directories will be removed from your list automatically.", "Reminder", JOptionPane.INFORMATION_MESSAGE);
								}
								removeMediaDirByIndex(evt, i);
								matchCount++;
							}
						}
					}
				}

				ProgressWorker<Void, Void> worker = new ProgressWorker<Void, Void>()
				{
					@Override
					protected Void doInBackground()
					{
						MediaLibraryOperator operator = new MediaLibraryOperator(this);
						operator.addDirectory(dir);
						return null;
					}
				};
				ProgressDialog pd = new ProgressDialog(this, true, worker, "Updating Media Library...", true);
				pd.setVisible(true);

				try
				{
					worker.get();
					_mediaLibraryList.setListData(guiDriver.getMediaDirs());
				}
				catch (InterruptedException ex)
				{
				}
				catch (ExecutionException ex)
				{
				}
				catch (CancellationException ex)
				{
				}
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(this, "An error has occured, media directory could not be added.");
				e.printStackTrace();
			}
		}
		else
		{
			jMediaDirChooser.cancelSelection();
		}
		updateMediaDirButtons();
	}//GEN-LAST:event__addMediaDirButtonActionPerformed

	private void _removeMediaDirButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__removeMediaDirButtonActionPerformed
	{//GEN-HEADEREND:event__removeMediaDirButtonActionPerformed
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try
		{
			String selection = (String) _mediaLibraryList.getSelectedValue();
			if (selection != null)
			{
				if (!selection.equals("Please Add A Media Directory..."))
				{
					guiDriver.removeMediaDir(selection);
					_mediaLibraryList.setListData(guiDriver.getMediaDirs());
				}
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		catch (Exception e)
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			JOptionPane.showMessageDialog(this, "An error has occured, files in the media directory you removed may not have been completely removed from the library.  Please refresh the library.");
			e.printStackTrace();
		}
		updateMediaDirButtons();
	}//GEN-LAST:event__removeMediaDirButtonActionPerformed

	private void removeMediaDirByIndex(java.awt.event.ActionEvent evt, int index)
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try
		{

			_mediaLibraryList.setListData(guiDriver.removeMediaDir((String) _mediaLibraryList.getModel().getElementAt(index)));
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		catch (Exception e)
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			JOptionPane.showMessageDialog(this, "An error has occured, files in the media directory you removed may not have been completely removed from the library.  Please refresh the library.");
			e.printStackTrace();
		}
		updateMediaDirButtons();
	}

	private void _aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt)
	{
		JOptionPane.showMessageDialog(this, "listFix( ) v2.2.0\n\nBrought To You By: \n          Jeremy Caron (firewyre at users dot sourceforge dot net) \n          Kennedy Akala (kennedyakala at users dot sourceforge dot net)", "About", JOptionPane.INFORMATION_MESSAGE);
	}

	private void _exitMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__exitMenuItemActionPerformed
	{//GEN-HEADEREND:event__exitMenuItemActionPerformed
		confirmCloseApp();
	}//GEN-LAST:event__exitMenuItemActionPerformed

	/** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt)//GEN-FIRST:event_exitForm
	{//GEN-HEADEREND:event_exitForm
		confirmCloseApp();
	}//GEN-LAST:event_exitForm

	private void _refreshMediaDirsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__refreshMediaDirsButtonActionPerformed
	{//GEN-HEADEREND:event__refreshMediaDirsButtonActionPerformed
		refreshMediaDirs();
	}//GEN-LAST:event__refreshMediaDirsButtonActionPerformed

	private void refreshMediaDirs()
	{
		ProgressWorker<Void, Void> worker = new ProgressWorker<Void, Void>()
		{
			@Override
			protected Void doInBackground()
			{
				MediaLibraryOperator operator = new MediaLibraryOperator(this);
				operator.refresh();
				return null;
			}
		};
		ProgressDialog pd = new ProgressDialog(this, true, worker, "Updating Media Library...", true);
		pd.setVisible(true);

		try
		{
			worker.get();
			_mediaLibraryList.setListData(guiDriver.getMediaDirs());
		}
		catch (InterruptedException ex)
		{
		}
		catch (ExecutionException ex)
		{
		}
		catch (CancellationException ex)
		{
		}
	}

	private void _appOptionsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__appOptionsMenuItemActionPerformed
	{//GEN-HEADEREND:event__appOptionsMenuItemActionPerformed
		fireOptionsPopup();
	}//GEN-LAST:event__appOptionsMenuItemActionPerformed

    private void _miBatchPlaylistRepairActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miBatchPlaylistRepairActionPerformed
    {//GEN-HEADEREND:event__miBatchPlaylistRepairActionPerformed
		launchBatchPlaylistRepair();
	}//GEN-LAST:event__miBatchPlaylistRepairActionPerformed

private void _helpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__helpMenuItemActionPerformed
        BrowserLauncher.launch("http://apps.sourceforge.net/mediawiki/listfix/index.php?title=Main_Page");//GEN-LAST:event__helpMenuItemActionPerformed
	}

private void _updateCheckMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__updateCheckMenuItemActionPerformed
	BrowserLauncher.launch("http://sourceforge.net/projects/listfix/");
}//GEN-LAST:event__updateCheckMenuItemActionPerformed

private void _batchRepairWinampMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__batchRepairWinampMenuItemActionPerformed

	final BatchRepair br = WinampHelper.getWinampBatchRepair(guiDriver.getMediaLibraryFileList());
	if (br == null || br.isEmpty())
	{
		JOptionPane.showMessageDialog(this, "Could not find any WinAmp playlists");
		return;
	}

	BatchRepairDialog dlg = new BatchRepairDialog(this, true, br);
	if (!dlg.getUserCancelled())
	{
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);
	}
}//GEN-LAST:event__batchRepairWinampMenuItemActionPerformed

private void onTabStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_onTabStateChanged
{//GEN-HEADEREND:event_onTabStateChanged
	onSelectedTabChanged();
}//GEN-LAST:event_onTabStateChanged

private void onMenuBatchRepairActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onMenuBatchRepairActionPerformed
{//GEN-HEADEREND:event_onMenuBatchRepairActionPerformed
	JFileChooser dlg = new JFileChooser();
	dlg.setDialogTitle("Select Playlists and/or Directories");
	dlg.setAcceptAllFileFilterUsed(false);
	dlg.addChoosableFileFilter(new PlaylistFileChooserFilter());
	dlg.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	dlg.setMultiSelectionEnabled(true);
	if (dlg.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
	{
		// build complete list of playlists
		List<File> files = new ArrayList<File>();
		for (File file : dlg.getSelectedFiles())
		{
			if (file.isFile())
			{
				files.add(file);
			}
			else
			{
				files.addAll(PlaylistScanner.getAllPlaylists(file));
			}
		}
		if (files.isEmpty())
		{
			return;
		}

		File rootDir = dlg.getCurrentDirectory();
		BatchRepair br = new BatchRepair(guiDriver.getMediaLibraryFileList(), rootDir);
		br.setDescription("Batch Repair Selected Playlists");
		for (File file : files)
		{
			br.add(new BatchRepairItem(file));
		}

		BatchRepairDialog repairDlg = new BatchRepairDialog(this, true, br);
		if (!repairDlg.getUserCancelled())
		{
			repairDlg.setLocationRelativeTo(this);
			repairDlg.setVisible(true);

			updatePlaylistDirectoryPanel();
		}
	}
}//GEN-LAST:event_onMenuBatchRepairActionPerformed

private void _openIconButtonActionPerformed1(java.awt.event.ActionEvent evt)//GEN-FIRST:event__openIconButtonActionPerformed1
{//GEN-HEADEREND:event__openIconButtonActionPerformed1
	if (_currentPlaylist != null)
	{
		jM3UChooser.setSelectedFile(_currentPlaylist.getFile());
	}
	int response = jM3UChooser.showOpenDialog(this);
	if (response == JFileChooser.APPROVE_OPTION)
	{
		File[] playlists = jM3UChooser.getSelectedFiles();
		for (File file : playlists)
			this.openPlaylist(file);
	}
	else
	{
		jM3UChooser.cancelSelection();
	}
}//GEN-LAST:event__openIconButtonActionPerformed1

private void _leftSplitPaneResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event__leftSplitPaneResized
{//GEN-HEADEREND:event__leftSplitPaneResized
	_leftSplitPane.setDividerLocation(.60);
}//GEN-LAST:event__leftSplitPaneResized

private void _saveMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__saveMenuItemActionPerformed
{//GEN-HEADEREND:event__saveMenuItemActionPerformed

	if (_currentPlaylist == null)
	{
		return;
	}

	if (_currentPlaylist.isNew())
	{
		_saveAsMenuItemActionPerformed(evt);
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
					_currentPlaylist.save(saveRelative, this);
					return null;
				}
			};
			ProgressDialog pd = new ProgressDialog(this, true, worker, "Saving...", false);
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
}//GEN-LAST:event__saveMenuItemActionPerformed

private void _newIconButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__newIconButtonActionPerformed
{//GEN-HEADEREND:event__newIconButtonActionPerformed
	try
	{
		_currentPlaylist = new Playlist();
		// _currentPlaylist.addModifiedListener(this);
		String path = _currentPlaylist.getFile().getCanonicalPath();
		PlaylistEditCtrl editor = new PlaylistEditCtrl();
		editor.setPlaylist(_currentPlaylist);
		String title = _currentPlaylist.getFilename();
		_uiTabs.addTab(title, null, editor, path);
		int ix = _uiTabs.getTabCount() - 1;
		_uiTabs.setSelectedIndex(ix);
		_pathToEditorMap.put(path, editor);
		_playlistToEditorMap.put(_currentPlaylist, editor);

		// add custom tab controls
		_uiTabs.setTabComponentAt(ix, new ClosableTabCtrl(GUIScreen.this, _uiTabs, title));
		refreshStatusLabel(_currentPlaylist);
		_currentPlaylist.addModifiedListener(_playlistListener);

		((java.awt.CardLayout) _playlistPanel.getLayout()).show(_playlistPanel, "_uiTabs");
	}
	catch (IOException ex)
	{
		Logger.getLogger(GUIScreen.class.getName()).log(Level.SEVERE, null, ex);
		JOptionPane.showMessageDialog(this, "Sorry, there was an error creating a new playlist.  Please try again, or file a bug report.");
	}

}//GEN-LAST:event__newIconButtonActionPerformed

private void _newPlaylistMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__newPlaylistMenuItemActionPerformed
{//GEN-HEADEREND:event__newPlaylistMenuItemActionPerformed
	_newIconButtonActionPerformed(evt);
}//GEN-LAST:event__newPlaylistMenuItemActionPerformed

private void _extractPlaylistsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__extractPlaylistsMenuItemActionPerformed
{//GEN-HEADEREND:event__extractPlaylistsMenuItemActionPerformed

	final JFileChooser dlg = new JFileChooser();
	dlg.setDialogTitle("Extract to...");
	dlg.setAcceptAllFileFilterUsed(true);
	dlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	dlg.setMultiSelectionEnabled(false);
	int response = dlg.showOpenDialog(this);

	if (response == JFileChooser.APPROVE_OPTION)
	{
		ProgressWorker<Void, Void> worker = new ProgressWorker<Void, Void>()
		{
			@Override
			protected Void doInBackground() throws Exception
			{
				try
				{
					WinampHelper.extractPlaylists(dlg.getSelectedFile(), this);
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(GUIScreen.this, "Sorry, there was a problem extracting your playlists.  The error was: " + ex.getMessage(), "Extraction Error", JOptionPane.ERROR_MESSAGE);
				}
				finally
				{
					return null;
				}
			}
		};
		ProgressDialog pd = new ProgressDialog(this, true, worker, "Extracting...", false);
		pd.setVisible(true);
	}
}//GEN-LAST:event__extractPlaylistsMenuItemActionPerformed

private void _closeMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__closeMenuItemActionPerformed
{//GEN-HEADEREND:event__closeMenuItemActionPerformed
	if (_uiTabs.getComponents().length > 0)
	{
		this.tryCloseTab((ClosableTabCtrl) _uiTabs.getTabComponentAt(_uiTabs.getSelectedIndex()));
	}
}//GEN-LAST:event__closeMenuItemActionPerformed

private void _refreshPlaylistPanelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__refreshPlaylistPanelButtonActionPerformed
{//GEN-HEADEREND:event__refreshPlaylistPanelButtonActionPerformed
	updatePlaylistDirectoryPanel();
}//GEN-LAST:event__refreshPlaylistPanelButtonActionPerformed

private void _playlistDirectoryTreeKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event__playlistDirectoryTreeKeyReleased
{//GEN-HEADEREND:event__playlistDirectoryTreeKeyReleased
	if (evt.getKeyCode() == KeyEvent.VK_ENTER)
	{
		_openSelectedPlaylistsButtonActionPerformed(null);
	}
}//GEN-LAST:event__playlistDirectoryTreeKeyReleased

private void _openSelectedPlaylistsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__openSelectedPlaylistsButtonActionPerformed
{//GEN-HEADEREND:event__openSelectedPlaylistsButtonActionPerformed
	openTreeSelectedPlaylists();
}//GEN-LAST:event__openSelectedPlaylistsButtonActionPerformed

private void _miOpenSelectedPlaylistsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miOpenSelectedPlaylistsActionPerformed
{//GEN-HEADEREND:event__miOpenSelectedPlaylistsActionPerformed
	openTreeSelectedPlaylists();
}//GEN-LAST:event__miOpenSelectedPlaylistsActionPerformed

private void _setPlaylistsDirectoryButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__setPlaylistsDirectoryButtonActionPerformed
{//GEN-HEADEREND:event__setPlaylistsDirectoryButtonActionPerformed
	fireOptionsPopup();
}//GEN-LAST:event__setPlaylistsDirectoryButtonActionPerformed

private void _repairPlaylistsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__repairPlaylistsButtonActionPerformed
{//GEN-HEADEREND:event__repairPlaylistsButtonActionPerformed
	launchBatchPlaylistRepair();
}//GEN-LAST:event__repairPlaylistsButtonActionPerformed

private void _closeAllMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__closeAllMenuItemActionPerformed
{//GEN-HEADEREND:event__closeAllMenuItemActionPerformed
	tryCloseAllTabs();
}//GEN-LAST:event__closeAllMenuItemActionPerformed

private void _miRefreshDirectoryTreeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miRefreshDirectoryTreeActionPerformed
{//GEN-HEADEREND:event__miRefreshDirectoryTreeActionPerformed
	updatePlaylistDirectoryPanel();
}//GEN-LAST:event__miRefreshDirectoryTreeActionPerformed

	public void setApplicationFont(Font font)
	{
		Enumeration enumer = UIManager.getDefaults().keys();
		while (enumer.hasMoreElements())
		{
			Object key = enumer.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof Font)
			{
				UIManager.put(key, new javax.swing.plaf.FontUIResource(font));
			}
		}
		updateAllComponentTreeUIs();
	}

	private void updateMediaDirButtons()
	{
		if (_mediaLibraryList.getModel().getSize() == 0)
		{
			_removeMediaDirButton.setEnabled(false);
			_refreshMediaDirsButton.setEnabled(false);
		}
		else if (_mediaLibraryList.getModel().getSize() != 0)
		{
			_removeMediaDirButton.setEnabled(true);
			_refreshMediaDirsButton.setEnabled(true);
		}
	}

	private void updatePlaylistDirectoryPanel()
	{
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		_playlistDirectoryTree.setModel(new DefaultTreeModel(FileTreeNodeGenerator.addNodes(null, new File(guiDriver.getAppOptions().getPlaylistsDirectory()))));
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private void recentPlaylistActionPerformed(java.awt.event.ActionEvent evt)
	{
		JMenuItem temp = (JMenuItem) evt.getSource();
		File playlist = new File(temp.getText());
		openPlaylist(playlist);
	}

	private void setLookAndFeel(String className)
	{
		try
		{
			UIManager.setLookAndFeel(className);
			updateAllComponentTreeUIs();
			try
			{
				if (_tabPaneInsets != null)
				{
					setTabAreaInsets(_tabPaneInsets);
				}
			}
			catch (Exception e)
			{
				// Eat the error and get on with life...
			}
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[])
	{
		GUIScreen mainWindow = new GUIScreen();
		DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
		java.awt.Dimension labelSize = mainWindow.getPreferredSize();
		mainWindow.setLocation(dm.getWidth() / 2 - (labelSize.width / 2), dm.getHeight() / 2 - (labelSize.height / 2));
		mainWindow.setVisible(true);

		if (mainWindow.getOptions().getAutoRefreshMediaLibraryOnStartup())
		{
			mainWindow.refreshMediaDirs();
		}

		for (String arg : args)
		{
			try
			{
				mainWindow.openPlaylist(new File(arg));
			}
			catch (Exception e)
			{

			}
		}
	}
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem _aboutMenuItem;
    private javax.swing.JButton _addMediaDirButton;
    private javax.swing.JMenuItem _appOptionsMenuItem;
    private javax.swing.JMenuItem _batchRepairWinampMenuItem;
    private javax.swing.JMenuItem _clearHistoryMenuItem;
    private javax.swing.JMenuItem _closeAllMenuItem;
    private javax.swing.JMenuItem _closeMenuItem;
    private javax.swing.JMenuItem _exitMenuItem;
    private javax.swing.JMenuItem _extractPlaylistsMenuItem;
    private javax.swing.JMenu _fileMenu;
    private javax.swing.JPanel _gettingStartedPanel;
    private javax.swing.JMenu _helpMenu;
    private javax.swing.JMenuItem _helpMenuItem;
    private javax.swing.JSplitPane _leftSplitPane;
    private javax.swing.JMenuItem _loadMenuItem;
    private javax.swing.JMenuBar _mainMenuBar;
    private javax.swing.JPanel _mediaLibraryButtonPanel;
    private javax.swing.JList _mediaLibraryList;
    private javax.swing.JPanel _mediaLibraryPanel;
    private javax.swing.JScrollPane _mediaLibraryScrollPane;
    private javax.swing.JMenuItem _miBatchPlaylistRepair;
    private javax.swing.JMenuItem _miBatchRepair;
    private javax.swing.JMenuItem _miOpenSelectedPlaylists;
    private javax.swing.JMenuItem _miRefreshDirectoryTree;
    private javax.swing.JButton _newIconButton;
    private javax.swing.JMenuItem _newPlaylistMenuItem;
    private javax.swing.JButton _openIconButton;
    private javax.swing.JButton _openSelectedPlaylistsButton;
    private javax.swing.JPanel _playlistDirectoryPanel;
    private javax.swing.JTree _playlistDirectoryTree;
    private javax.swing.JPanel _playlistPanel;
    private javax.swing.JPopupMenu _playlistTreeRightClickMenu;
    private javax.swing.JButton _refreshMediaDirsButton;
    private javax.swing.JButton _refreshPlaylistPanelButton;
    private javax.swing.JButton _removeMediaDirButton;
    private javax.swing.JButton _repairPlaylistsButton;
    private javax.swing.JMenuItem _saveAsMenuItem;
    private javax.swing.JMenuItem _saveMenuItem;
    private javax.swing.JButton _setPlaylistsDirectoryButton;
    private javax.swing.JPanel _spacerPanel;
    private javax.swing.JSplitPane _splitPane;
    private javax.swing.JPanel _statusPanel;
    private javax.swing.JScrollPane _treeScrollPane;
    private javax.swing.JTabbedPane _uiTabs;
    private javax.swing.JMenuItem _updateCheckMenuItem;
    private javax.swing.JPanel _verticalPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JMenu recentMenu;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
	// </editor-fold>
}
