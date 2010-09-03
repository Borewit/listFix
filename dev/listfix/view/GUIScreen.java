/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2009 Jeremy Caron
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
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.tree.*;

import listfix.controller.GUIDriver;
import listfix.controller.tasks.AddMediaDirectoryTask;
import listfix.controller.tasks.UpdateMediaLibraryTask;

import listfix.io.BrowserLauncher;
import listfix.io.FileTreeNodeGenerator;
import listfix.io.FileWriter;
import listfix.io.PlaylistFileChooserFilter;
import listfix.io.PlaylistScanner;
import listfix.io.UNCFile;
import listfix.io.WinampHelper;

import listfix.model.AppOptions;
import listfix.model.BatchRepair;
import listfix.model.BatchRepairItem;
import listfix.model.Playlist;
import listfix.model.PlaylistEntry;
import listfix.model.PlaylistHistory;

import listfix.util.ArrayFunctions;
import listfix.util.OperatingSystem;

import listfix.view.dialogs.ProgressDialog;
import listfix.view.dialogs.BatchRepairDialog;
import listfix.view.dialogs.AppOptionsDialog;

import listfix.view.support.IPlaylistModifiedListener;
import listfix.view.support.ClosableTabCtrl;
import listfix.view.support.FontHelper;
import listfix.view.support.ICloseableTabManager;
import listfix.view.support.ProgressWorker;

public final class GUIScreen extends JFrame implements ICloseableTabManager
{
	private static final long serialVersionUID = 7691786927987534889L;
	private final JFileChooser jM3UChooser;
	private final JFileChooser jMediaDirChooser;
	private final JFileChooser jSaveFileChooser;
	private final listfix.view.support.ProgressPopup updateMediaLibraryProgressDialog;
	private GUIDriver guiDriver = null;

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
		updateMediaLibraryProgressDialog = new listfix.view.support.ProgressPopup(this, "Updating Media Library", true, 450, 40, true);
		this.setLookAndFeel(guiDriver.getAppOptions().getLookAndFeel());
		jM3UChooser.setDialogTitle("Choose a Playlist...");
		jM3UChooser.setAcceptAllFileFilterUsed(false);
		jM3UChooser.setFileFilter(new PlaylistFileChooserFilter());
		FontHelper.recursiveSetFont(jM3UChooser.getComponents());
		jMediaDirChooser.setDialogTitle("Specify a media directory...");
		jMediaDirChooser.setAcceptAllFileFilterUsed(false);
		jMediaDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		FontHelper.recursiveSetFont(jMediaDirChooser.getComponents());
		jSaveFileChooser.setDialogTitle("Save File:");
		jSaveFileChooser.setAcceptAllFileFilterUsed(false);
		jSaveFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jSaveFileChooser.setFileFilter(new PlaylistFileChooserFilter());
		FontHelper.recursiveSetFont(jSaveFileChooser.getComponents());
		splashScreen.setVisible(false);

        UIManager.put("OptionPane.font", new FontUIResource(new Font("Verdana", 0, 9)));
		UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("Verdana", 0, 9)));
		UIManager.put("OptionPane.buttonFont", new FontUIResource(new Font("Verdana", 0, 9)));

		if (guiDriver.getShowMediaDirWindow())
		{
			JOptionPane.showMessageDialog(this, "You need to add a media directory before you can find the new locations of your files.  See help for more information.", "Reminder", JOptionPane.INFORMATION_MESSAGE);
		}
		else
		{
			mediaLibraryList.setListData(guiDriver.getMediaDirs());
		}

		updateMediaDirButtons();
		updateRecentMenu();
		refreshStatusLabel(null);

		(new FileWriter()).writeIni(guiDriver.getMediaDirs(), guiDriver.getMediaLibraryDirectoryList(), guiDriver.getMediaLibraryFileList(), guiDriver.getAppOptions());

		((java.awt.CardLayout) _playlistPanel.getLayout()).show(_playlistPanel, "_gettingStartedPanel");

		initPlaylistListener();

		addOpenPlaylistTabButton(_uiTabs);

		if (!OperatingSystem.isWindows())
		{
			batchRepairWinampMenuItem.setVisible(false);
		}

		syncJMenuFonts();
	}

	public AppOptions getOptions()
	{
		return guiDriver.getAppOptions();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        playlistTreeRightClickMenu = new javax.swing.JPopupMenu();
        batchPlaylistRepairMenuItem = new javax.swing.JMenuItem();
        _statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        _splitPane = new javax.swing.JSplitPane();
        _leftSplitPane = new javax.swing.JSplitPane();
        mediaLibraryPanel = new javax.swing.JPanel();
        mediaLibraryButtonPanel = new javax.swing.JPanel();
        addMediaDirButton = new javax.swing.JButton();
        removeMediaDirButton = new javax.swing.JButton();
        refreshMediaDirsButton = new javax.swing.JButton();
        mediaLibraryScrollPane = new javax.swing.JScrollPane();
        mediaLibraryList = new javax.swing.JList(new String[] {"Please Add A Media Directory..."});
        playlistDirectoryPanel = new javax.swing.JPanel();
        treeScrollPane = new javax.swing.JScrollPane();
        playlistDirectoryTree = new javax.swing.JTree(FileTreeNodeGenerator.addNodes(null, new File(guiDriver.getAppOptions().getPlaylistsDirectory())));
        _playlistPanel = new javax.swing.JPanel();
        _uiTabs = new javax.swing.JTabbedPane();
        _gettingStartedPanel = new javax.swing.JPanel();
        _openIconButton = new javax.swing.JButton();
        _mainMenuBar = new javax.swing.JMenuBar();
        _fileMenu = new javax.swing.JMenu();
        loadMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        _miBatchRepair = new javax.swing.JMenuItem();
        batchRepairWinampMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        recentMenu = new javax.swing.JMenu();
        clearHistoryMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        appOptionsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        exit = new javax.swing.JMenuItem();
        _helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        helpMenuItem = new javax.swing.JMenuItem();
        updateCheckMenuItem = new javax.swing.JMenuItem();

        playlistTreeRightClickMenu.setPreferredSize(new java.awt.Dimension(160, 28));

        batchPlaylistRepairMenuItem.setFont(new java.awt.Font("Verdana", 0, 9));
        batchPlaylistRepairMenuItem.setText("Repair Playlist(s)...");
        batchPlaylistRepairMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                batchPlaylistRepairMenuItemActionPerformed(evt);
            }
        });
        playlistTreeRightClickMenu.add(batchPlaylistRepairMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("listFix( ) - v2.0.0");
        setMinimumSize(new java.awt.Dimension(600, 149));
        setName("mainFrame"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        _statusPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        _statusPanel.setLayout(new java.awt.BorderLayout());

        statusLabel.setFont(new java.awt.Font("Verdana", 0, 9)); // NOI18N
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

        mediaLibraryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Media Directories", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Verdana", 0, 9))); // NOI18N
        mediaLibraryPanel.setAlignmentX(0.0F);
        mediaLibraryPanel.setAlignmentY(0.0F);
        mediaLibraryPanel.setLayout(new java.awt.BorderLayout());

        mediaLibraryButtonPanel.setMinimumSize(new java.awt.Dimension(223, 31));

        addMediaDirButton.setFont(new java.awt.Font("Verdana", 0, 9)); // NOI18N
        addMediaDirButton.setText("Add");
        addMediaDirButton.setToolTipText("Where do you keep your music?");
        addMediaDirButton.setFocusable(false);
        addMediaDirButton.setMinimumSize(new java.awt.Dimension(53, 25));
        addMediaDirButton.setPreferredSize(null);
        addMediaDirButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMediaDirButtonActionPerformed(evt);
            }
        });
        mediaLibraryButtonPanel.add(addMediaDirButton);

        removeMediaDirButton.setFont(new java.awt.Font("Verdana", 0, 9)); // NOI18N
        removeMediaDirButton.setText("Remove");
        removeMediaDirButton.setToolTipText("Remove a directory from the search list");
        removeMediaDirButton.setFocusable(false);
        removeMediaDirButton.setMinimumSize(new java.awt.Dimension(73, 25));
        removeMediaDirButton.setPreferredSize(null);
        removeMediaDirButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMediaDirButtonActionPerformed(evt);
            }
        });
        mediaLibraryButtonPanel.add(removeMediaDirButton);

        refreshMediaDirsButton.setFont(new java.awt.Font("Verdana", 0, 9)); // NOI18N
        refreshMediaDirsButton.setText("Refresh");
        refreshMediaDirsButton.setToolTipText("The contents of your media library are cached, refresh to pickup changes");
        refreshMediaDirsButton.setFocusable(false);
        refreshMediaDirsButton.setMinimumSize(new java.awt.Dimension(71, 25));
        refreshMediaDirsButton.setPreferredSize(null);
        refreshMediaDirsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshMediaDirsButtonActionPerformed(evt);
            }
        });
        mediaLibraryButtonPanel.add(refreshMediaDirsButton);

        mediaLibraryPanel.add(mediaLibraryButtonPanel, java.awt.BorderLayout.SOUTH);

        mediaLibraryList.setFont(new java.awt.Font("Verdana", 0, 9));
        mediaLibraryList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        mediaLibraryList.setPreferredSize(null);
        mediaLibraryScrollPane.setViewportView(mediaLibraryList);

        mediaLibraryPanel.add(mediaLibraryScrollPane, java.awt.BorderLayout.CENTER);

        _leftSplitPane.setBottomComponent(mediaLibraryPanel);

        playlistDirectoryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Playlists Directory", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Verdana", 0, 9))); // NOI18N
        playlistDirectoryPanel.setAlignmentX(0.0F);
        playlistDirectoryPanel.setAlignmentY(0.0F);
        playlistDirectoryPanel.setLayout(new java.awt.BorderLayout());

        playlistDirectoryTree.setFont(new java.awt.Font("Verdana", 0, 9));
        playlistDirectoryTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playlistDirectoryTreeMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                playlistDirectoryTreeMousePressed(evt);
            }
        });
        treeScrollPane.setViewportView(playlistDirectoryTree);

        playlistDirectoryPanel.add(treeScrollPane, java.awt.BorderLayout.CENTER);

        _leftSplitPane.setTopComponent(playlistDirectoryPanel);

        _splitPane.setLeftComponent(_leftSplitPane);

        _playlistPanel.setBackground(java.awt.SystemColor.window);
        _playlistPanel.setLayout(new java.awt.CardLayout());

        _uiTabs.setFocusable(false);
        _uiTabs.setFont(new java.awt.Font("Verdana", 0, 9)); // NOI18N
        _uiTabs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                onTabStateChanged(evt);
            }
        });
        _playlistPanel.add(_uiTabs, "_uiTabs");

        _gettingStartedPanel.setBackground(new java.awt.Color(255, 255, 255));
        _gettingStartedPanel.setLayout(new java.awt.GridBagLayout());

        _openIconButton.setFont(new java.awt.Font("Verdana", 0, 12));
        _openIconButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open-big.png"))); // NOI18N
        _openIconButton.setText("Open A Playlist");
        _openIconButton.setToolTipText("Open A Playlist");
        _openIconButton.setAlignmentY(0.0F);
        _openIconButton.setBorder(null);
        _openIconButton.setFocusable(false);
        _openIconButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        _openIconButton.setIconTextGap(-2);
        _openIconButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        _openIconButton.setMaximumSize(new java.awt.Dimension(300, 25));
        _openIconButton.setMinimumSize(new java.awt.Dimension(256, 25));
        _openIconButton.setPreferredSize(new java.awt.Dimension(220, 180));
        _openIconButton.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        _openIconButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        _openIconButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _openIconButtonActionPerformed1(evt);
            }
        });
        _gettingStartedPanel.add(_openIconButton, new java.awt.GridBagConstraints());

        _playlistPanel.add(_gettingStartedPanel, "_gettingStartedPanel");

        _splitPane.setRightComponent(_playlistPanel);

        getContentPane().add(_splitPane, java.awt.BorderLayout.CENTER);

        _mainMenuBar.setBorder(null);
        _mainMenuBar.setFont(new java.awt.Font("Verdana", 0, 9));

        _fileMenu.setMnemonic('F');
        _fileMenu.setText("File");
        _fileMenu.setFont(new java.awt.Font("Verdana", 0, 9));

        loadMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        loadMenuItem.setFont(new java.awt.Font("Verdana", 0, 9));
        loadMenuItem.setMnemonic('L');
        loadMenuItem.setText("Open Playlist");
        loadMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openIconButtonActionPerformed(evt);
            }
        });
        _fileMenu.add(loadMenuItem);

        saveAsMenuItem.setFont(new java.awt.Font("Verdana", 0, 9));
        saveAsMenuItem.setMnemonic('V');
        saveAsMenuItem.setText("Save As");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        _fileMenu.add(saveAsMenuItem);

        jSeparator6.setForeground(new java.awt.Color(102, 102, 153));
        _fileMenu.add(jSeparator6);

        _miBatchRepair.setFont(loadMenuItem.getFont());
        _miBatchRepair.setText("Batch Repair...");
        _miBatchRepair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onMenuBatchRepairActionPerformed(evt);
            }
        });
        _fileMenu.add(_miBatchRepair);

        batchRepairWinampMenuItem.setFont(new java.awt.Font("Verdana", 0, 9));
        batchRepairWinampMenuItem.setText("Batch Repair Winamp Playlists...");
        batchRepairWinampMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                batchRepairWinampMenuItemActionPerformed(evt);
            }
        });
        _fileMenu.add(batchRepairWinampMenuItem);

        jSeparator3.setForeground(new java.awt.Color(102, 102, 153));
        _fileMenu.add(jSeparator3);

        recentMenu.setText("Recent Playlists");
        recentMenu.setToolTipText("Recently Opened Playlists");
        recentMenu.setFont(new java.awt.Font("Verdana", 0, 9));
        _fileMenu.add(recentMenu);

        clearHistoryMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        clearHistoryMenuItem.setFont(new java.awt.Font("Verdana", 0, 9));
        clearHistoryMenuItem.setMnemonic('H');
        clearHistoryMenuItem.setText("Clear Playlist History");
        clearHistoryMenuItem.setToolTipText("");
        clearHistoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearHistoryMenuItemActionPerformed(evt);
            }
        });
        _fileMenu.add(clearHistoryMenuItem);

        jSeparator1.setForeground(new java.awt.Color(102, 102, 153));
        _fileMenu.add(jSeparator1);

        appOptionsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.ALT_MASK));
        appOptionsMenuItem.setFont(new java.awt.Font("Verdana", 0, 9));
        appOptionsMenuItem.setMnemonic('H');
        appOptionsMenuItem.setText("Options...");
        appOptionsMenuItem.setToolTipText("");
        appOptionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appOptionsMenuItemActionPerformed(evt);
            }
        });
        _fileMenu.add(appOptionsMenuItem);

        jSeparator2.setForeground(new java.awt.Color(102, 102, 153));
        _fileMenu.add(jSeparator2);

        exit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        exit.setFont(new java.awt.Font("Verdana", 0, 9));
        exit.setMnemonic('x');
        exit.setText("Exit");
        exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitActionPerformed(evt);
            }
        });
        _fileMenu.add(exit);

        _mainMenuBar.add(_fileMenu);

        _helpMenu.setMnemonic('H');
        _helpMenu.setText("Help");
        _helpMenu.setFont(new java.awt.Font("Verdana", 0, 9));

        aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        aboutMenuItem.setFont(new java.awt.Font("Verdana", 0, 9));
        aboutMenuItem.setMnemonic('A');
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        _helpMenu.add(aboutMenuItem);

        helpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.ALT_MASK));
        helpMenuItem.setFont(new java.awt.Font("Verdana", 0, 9));
        helpMenuItem.setMnemonic('e');
        helpMenuItem.setText("Help");
        helpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuItemActionPerformed(evt);
            }
        });
        _helpMenu.add(helpMenuItem);

        updateCheckMenuItem.setFont(new java.awt.Font("Verdana", 0, 9));
        updateCheckMenuItem.setText("Check For Updates");
        updateCheckMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateCheckMenuItemActionPerformed(evt);
            }
        });
        _helpMenu.add(updateCheckMenuItem);

        _mainMenuBar.add(_helpMenu);

        setJMenuBar(_mainMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void playlistDirectoryTreeNodeDoubleClicked(TreePath selPath)
	{
		File toOpen = new File(FileTreeNodeGenerator.TreePathToFileSystemPath(selPath));
		if (!toOpen.isDirectory() && toOpen.exists())
		{
			this.openPlaylist(toOpen);
		}
	}

	private void clearHistoryMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_clearHistoryMenuItemActionPerformed
	{//GEN-HEADEREND:event_clearHistoryMenuItemActionPerformed
		guiDriver.clearM3UHistory();
		updateRecentMenu();
	}//GEN-LAST:event_clearHistoryMenuItemActionPerformed

	private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveAsMenuItemActionPerformed
	{//GEN-HEADEREND:event_saveAsMenuItemActionPerformed
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

				PlaylistEntry.BasePath = playlist.getParent();

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
				ProgressDialog pd = new ProgressDialog(this, true, worker, "Saving...");
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
	}//GEN-LAST:event_saveAsMenuItemActionPerformed

	private void openIconButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openIconButtonActionPerformed
	{//GEN-HEADEREND:event_openIconButtonActionPerformed
		if (_currentPlaylist != null)
		{
			jM3UChooser.setSelectedFile(_currentPlaylist.getFile());
		}
		int response = jM3UChooser.showOpenDialog(this);
		if (response == JFileChooser.APPROVE_OPTION)
		{
			File playlist = jM3UChooser.getSelectedFile();
			this.openPlaylist(playlist);
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

					((java.awt.CardLayout) _playlistPanel.getLayout()).show(_playlistPanel, "_uiTabs");
				}
			};
			ProgressDialog pd = new ProgressDialog(this, true, worker, "Loading...");
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

	private void syncJMenuFonts()
	{
		JMenuItem item = null;
		for (int i = 0; i < _fileMenu.getItemCount(); i++)
		{
			item = _fileMenu.getItem(i);
			try
			{
				syncJMenuItemFonts(item);
			}
			catch (Exception ex)
			{
			}
		}

		for (int i = 0; i < _helpMenu.getItemCount(); i++)
		{
			item = _helpMenu.getItem(i);
			try
			{
				syncJMenuItemFonts(item);
			}
			catch (Exception ex)
			{
			}
		}
	}

	private void syncJMenuItemFonts(JMenuItem item) throws SecurityException, IllegalArgumentException, IllegalAccessException
	{
		ButtonUI bui = item.getUI();
		if (bui instanceof BasicMenuItemUI)
		{
			Class<?> uiclass = bui.getClass();
			while (uiclass != null)
			{
				try
				{
					Field field = uiclass.getDeclaredField("acceleratorFont");
					if (field != null)
					{
						field.setAccessible(true);
						field.set(bui, new java.awt.Font("Verdana", 0, 9));

					}
				}
				catch (NoSuchFieldException ex)
				{
				}
				uiclass = uiclass.getSuperclass();
			}
		}
	}

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
				temp.setFont(new java.awt.Font("Verdana", 0, 9));
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
			setFont(new java.awt.Font("Verdana", 0, 9));
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
		BasicTabbedPaneUI ui;
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
				ProgressDialog pd = new ProgressDialog(this, true, worker, "Saving...");
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

	private void addMediaDirButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addMediaDirButtonActionPerformed
	{//GEN-HEADEREND:event_addMediaDirButtonActionPerformed
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
				String dir = mediaDir.getPath();
				if (guiDriver.getMediaDirs() != null && mediaDir != null)
				{
					// first let's see if this is a subdirectory of any of the media directories already in the list, and error out if so...
					if (ArrayFunctions.ContainsStringWithPrefix(guiDriver.getMediaDirs(), dir, !GUIDriver.fileSystemIsCaseSensitive))
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
				updateMediaLibraryProgressDialog.go();
				AddMediaDirectoryTask thisTask = new AddMediaDirectoryTask(dir, guiDriver);
				updateMediaLibraryProgressDialog.setBusyCursor(true);
				updateMediaLibraryProgressDialog.track(thisTask);
				updateMediaLibraryProgressDialog.setBusyCursor(false);
				updateMediaLibraryProgressDialog.setEnabled(false);
				mediaLibraryList.setListData(guiDriver.getMediaDirs());
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
	}//GEN-LAST:event_addMediaDirButtonActionPerformed

	private void removeMediaDirButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeMediaDirButtonActionPerformed
	{//GEN-HEADEREND:event_removeMediaDirButtonActionPerformed
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try
		{
			String selection = (String) mediaLibraryList.getSelectedValue();
			if (selection != null)
			{
				if (!selection.equals("Please Add A Media Directory..."))
				{
					guiDriver.removeMediaDir(selection);
					mediaLibraryList.setListData(guiDriver.getMediaDirs());
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
	}//GEN-LAST:event_removeMediaDirButtonActionPerformed

	private void removeMediaDirByIndex(java.awt.event.ActionEvent evt, int index)
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try
		{

			mediaLibraryList.setListData(guiDriver.removeMediaDir((String) mediaLibraryList.getModel().getElementAt(index)));
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

	private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt)
	{
		JOptionPane.showMessageDialog(this, "listFix( ) v2.0.0\n\nBrought To You By: \n          Jeremy Caron (firewyre at users dot sourceforge dot net) \n          Kennedy Akala (user22735 at users dot sourceforge dot net)", "About", JOptionPane.INFORMATION_MESSAGE);
	}

	private void exitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitActionPerformed
	{//GEN-HEADEREND:event_exitActionPerformed
		confirmCloseApp();
	}//GEN-LAST:event_exitActionPerformed

	/** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt)//GEN-FIRST:event_exitForm
	{//GEN-HEADEREND:event_exitForm
		confirmCloseApp();
	}//GEN-LAST:event_exitForm

	private void refreshMediaDirsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_refreshMediaDirsButtonActionPerformed
	{//GEN-HEADEREND:event_refreshMediaDirsButtonActionPerformed
		updateMediaLibraryProgressDialog.go();
		UpdateMediaLibraryTask thisTask = new UpdateMediaLibraryTask(guiDriver);
		updateMediaLibraryProgressDialog.setBusyCursor(true);
		updateMediaLibraryProgressDialog.track(thisTask);
		updateMediaLibraryProgressDialog.setBusyCursor(false);
	}//GEN-LAST:event_refreshMediaDirsButtonActionPerformed

	private void refreshMediaDirs()
	{
		updateMediaLibraryProgressDialog.go();
		UpdateMediaLibraryTask thisTask = new UpdateMediaLibraryTask(guiDriver);
		updateMediaLibraryProgressDialog.setBusyCursor(true);
		updateMediaLibraryProgressDialog.track(thisTask);
		updateMediaLibraryProgressDialog.setBusyCursor(false);
	}

	private void appOptionsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_appOptionsMenuItemActionPerformed
	{//GEN-HEADEREND:event_appOptionsMenuItemActionPerformed
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
				mediaLibraryList.setListData(guiDriver.getMediaDirs());
			}
			(new FileWriter()).writeIni(guiDriver.getMediaDirs(), guiDriver.getMediaLibraryDirectoryList(), guiDriver.getMediaLibraryFileList(), options);
			if (!oldPlaylistsDirectory.equals(options.getPlaylistsDirectory()))
			{
				playlistDirectoryTree.setModel(new DefaultTreeModel(FileTreeNodeGenerator.addNodes(null, new File(guiDriver.getAppOptions().getPlaylistsDirectory()))));
			}
			updateRecentMenu();
			this.setLookAndFeel(options.getLookAndFeel());
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}//GEN-LAST:event_appOptionsMenuItemActionPerformed

    private void playlistDirectoryTreeMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_playlistDirectoryTreeMouseClicked
    {//GEN-HEADEREND:event_playlistDirectoryTreeMouseClicked
		if ((evt.getModifiers() == MouseEvent.BUTTON2_MASK) || (evt.getModifiers() == MouseEvent.BUTTON3_MASK))
		{
			if (playlistDirectoryTree.getSelectionCount() > 0)
			{
				batchPlaylistRepairMenuItem.setEnabled(true);
			}
			else
			{
				batchPlaylistRepairMenuItem.setEnabled(false);
			}
			playlistTreeRightClickMenu.show(playlistDirectoryTree, (int) evt.getPoint().getX(), (int) evt.getPoint().getY());
		}
    }//GEN-LAST:event_playlistDirectoryTreeMouseClicked

    private void batchPlaylistRepairMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_batchPlaylistRepairMenuItemActionPerformed
    {//GEN-HEADEREND:event_batchPlaylistRepairMenuItemActionPerformed
		TreePath[] paths = playlistDirectoryTree.getSelectionPaths();
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
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);

		updatePlaylistDirectoryPanel();
	}//GEN-LAST:event_batchPlaylistRepairMenuItemActionPerformed

    private void playlistDirectoryTreeMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_playlistDirectoryTreeMousePressed
    {//GEN-HEADEREND:event_playlistDirectoryTreeMousePressed
		int selRow = playlistDirectoryTree.getRowForLocation(evt.getX(), evt.getY());
		TreePath selPath = playlistDirectoryTree.getPathForLocation(evt.getX(), evt.getY());
		if (selRow != -1)
		{
			if (evt.getClickCount() == 2)
			{
				playlistDirectoryTreeNodeDoubleClicked(selPath);
			}
		}
    }//GEN-LAST:event_playlistDirectoryTreeMousePressed

private void helpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMenuItemActionPerformed
        BrowserLauncher.launch("http://apps.sourceforge.net/mediawiki/listfix/index.php?title=Main_Page");//GEN-LAST:event_helpMenuItemActionPerformed
	}

private void updateCheckMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateCheckMenuItemActionPerformed
	BrowserLauncher.launch("http://sourceforge.net/projects/listfix/");
}//GEN-LAST:event_updateCheckMenuItemActionPerformed

private void batchRepairWinampMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_batchRepairWinampMenuItemActionPerformed

	final BatchRepair br = WinampHelper.getWinampBatchRepair(guiDriver.getMediaLibraryFileList());
	if (br == null || br.isEmpty())
	{
		JOptionPane.showMessageDialog(this, "Could not find any WinAmp playlists");
		return;
	}

	BatchRepairDialog dlg = new BatchRepairDialog(this, true, br);
	dlg.setLocationRelativeTo(this);
	dlg.setVisible(true);
}//GEN-LAST:event_batchRepairWinampMenuItemActionPerformed

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
	FontHelper.recursiveSetFont(dlg.getComponents());
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
		repairDlg.setLocationRelativeTo(this);
		repairDlg.setVisible(true);
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
		File playlist = jM3UChooser.getSelectedFile();
		this.openPlaylist(playlist);
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

	private void updateMediaDirButtons()
	{
		if (mediaLibraryList.getModel().getSize() == 0)
		{
			removeMediaDirButton.setEnabled(false);
			refreshMediaDirsButton.setEnabled(false);
		}
		else if (mediaLibraryList.getModel().getSize() != 0)
		{
			removeMediaDirButton.setEnabled(true);
			refreshMediaDirsButton.setEnabled(true);
		}
	}

	private void updatePlaylistDirectoryPanel()
	{
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		playlistDirectoryTree.setModel(new DefaultTreeModel(FileTreeNodeGenerator.addNodes(null, new File(guiDriver.getAppOptions().getPlaylistsDirectory()))));
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
			SwingUtilities.updateComponentTreeUI(this);
			SwingUtilities.updateComponentTreeUI(jM3UChooser);
			SwingUtilities.updateComponentTreeUI(jMediaDirChooser);
			SwingUtilities.updateComponentTreeUI(jSaveFileChooser);
			SwingUtilities.updateComponentTreeUI(updateMediaLibraryProgressDialog);
			SwingUtilities.updateComponentTreeUI(playlistTreeRightClickMenu);
			SwingUtilities.updateComponentTreeUI(_uiTabs);

			if (_tabPaneInsets != null)
			{
				setTabAreaInsets(_tabPaneInsets);
			}
		}
		catch (Exception e)
		{
		}
		finally
		{
			syncJMenuFonts();
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[])
	{
		GUIScreen mainWindow = new GUIScreen();
		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		java.awt.Dimension labelSize = mainWindow.getPreferredSize();
		mainWindow.setLocation(screenSize.width / 2 - (labelSize.width / 2), screenSize.height / 2 - (labelSize.height / 2));
		mainWindow.setVisible(true);
		if (mainWindow.getOptions().getAutoRefreshMediaLibraryOnStartup())
		{
			mainWindow.refreshMediaDirs();
		}
	}
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu _fileMenu;
    private javax.swing.JPanel _gettingStartedPanel;
    private javax.swing.JMenu _helpMenu;
    private javax.swing.JSplitPane _leftSplitPane;
    private javax.swing.JMenuBar _mainMenuBar;
    private javax.swing.JMenuItem _miBatchRepair;
    private javax.swing.JButton _openIconButton;
    private javax.swing.JPanel _playlistPanel;
    private javax.swing.JSplitPane _splitPane;
    private javax.swing.JPanel _statusPanel;
    private javax.swing.JTabbedPane _uiTabs;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton addMediaDirButton;
    private javax.swing.JMenuItem appOptionsMenuItem;
    private javax.swing.JMenuItem batchPlaylistRepairMenuItem;
    private javax.swing.JMenuItem batchRepairWinampMenuItem;
    private javax.swing.JMenuItem clearHistoryMenuItem;
    private javax.swing.JMenuItem exit;
    private javax.swing.JMenuItem helpMenuItem;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JMenuItem loadMenuItem;
    private javax.swing.JPanel mediaLibraryButtonPanel;
    private javax.swing.JList mediaLibraryList;
    private javax.swing.JPanel mediaLibraryPanel;
    private javax.swing.JScrollPane mediaLibraryScrollPane;
    private javax.swing.JPanel playlistDirectoryPanel;
    private javax.swing.JTree playlistDirectoryTree;
    private javax.swing.JPopupMenu playlistTreeRightClickMenu;
    private javax.swing.JMenu recentMenu;
    private javax.swing.JButton refreshMediaDirsButton;
    private javax.swing.JButton removeMediaDirButton;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JScrollPane treeScrollPane;
    private javax.swing.JMenuItem updateCheckMenuItem;
    // End of variables declaration//GEN-END:variables
	// </editor-fold>
}
