/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2014 Jeremy Caron
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

import com.jcabi.manifests.Manifests;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DarkStar;
import com.jgoodies.looks.plastic.theme.LightGray;
import com.jgoodies.looks.plastic.theme.SkyBlue;

import listfix.config.ApplicationOptionsConfiguration;
import listfix.config.IAppOptions;
import listfix.config.IMediaLibrary;
import listfix.config.MediaLibraryConfiguration;
import listfix.controller.GUIDriver;
import listfix.controller.MediaLibraryOperator;
import listfix.exceptions.MediaDirNotFoundException;
import listfix.io.*;
import listfix.io.filters.ExtensionFilter;
import listfix.io.filters.PlaylistFileFilter;
import listfix.json.JsonAppOptions;
import listfix.model.BatchRepair;
import listfix.model.BatchRepairItem;
import listfix.model.PlaylistHistory;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistFactory;
import listfix.swing.IDocumentChangeListener;
import listfix.util.ArrayFunctions;
import listfix.util.ExStack;
import listfix.util.FileTypeSearch;
import listfix.view.controls.JTransparentTextArea;
import listfix.view.controls.PlaylistEditCtrl;
import listfix.view.dialogs.*;
import listfix.view.support.IPlaylistModifiedListener;
import listfix.view.support.ImageIcons;
import listfix.view.support.ProgressWorker;
import listfix.view.support.WindowSaver;
import listfix.swing.JDocumentComponent;
import listfix.swing.JDocumentTabbedPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author jcaron
 */
public final class GUIScreen extends JFrame implements DropTargetListener
{
  private static final long _serialVersionUID = 7691786927987534889L;

  private final JFileChooser _jOpenPlaylistFileChooser = new JFileChooser();
  private final JFileChooser _jSaveFileChooser = new JFileChooser();
  private final FolderChooser _jMediaDirChooser = new FolderChooser();
  private final List<Playlist> _openPlaylists = new ArrayList<>();
  private final Image applicationIcon = new javax.swing.ImageIcon(Objects.requireNonNull(getClass().getResource("/images/icon.png"))).getImage();
  private final listfix.view.support.SplashScreen splashScreen = new listfix.view.support.SplashScreen("images/listfixSplashScreen.png");

  private GUIDriver _guiDriver = null;
  private Playlist _currentPlaylist;
  private IPlaylistModifiedListener _playlistListener;

  private static final Logger _logger = LogManager.getLogger(GUIScreen.class);

  private static final String applicationVersion = Manifests.read("Implementation-Version");

  private static final int keyEventRenameFile = KeyEvent.VK_F2;

  /**
   * The components should only be enabled when 1 or more playlists are loaded
   */
  private Component[] componentsRequireActivePlaylist;

  /**
   * Creates new form GUIScreen
   */
  public GUIScreen()
  {
    splashScreen.setIconImage(applicationIcon);

    preInitComponents();

    // Netbeans-generated form init code
    initComponents();

    postInitComponents();
  }

  /**
   * Show the splash screen with initial text, load the media library & option files into memory, then prepare to init the UI
   */
  private void preInitComponents()
  {
    splashScreen.setVisible(true);
    splashScreen.setStatusBar("Loading Media Library & Options...");
    _guiDriver = GUIDriver.getInstance();
    splashScreen.setStatusBar("Initializing UI...");
  }

  private IAppOptions getApplicationConfig()
  {
    return this._guiDriver.getApplicationConfiguration().getConfig();
  }

  /**
   * All UI components have been instantiated at this point.
   */
  private void postInitComponents()
  {
    // Set the user-selected font and look & feel
    final IAppOptions appConfig = this.getApplicationConfig();
    setApplicationFont(appConfig.getAppFont());
    this.setLookAndFeel(appConfig.getLookAndFeel());

    configureFileAndFolderChoosers();

    // Warn the user if no media directories have been defined, and set the
    if (this._guiDriver.getShowMediaDirWindow())
    {
      JOptionPane.showMessageDialog(
        this,
        new JTransparentTextArea("You need to add a media directory before you can find the new locations of your files.  See help for more information."),
        "Reminder",
        JOptionPane.INFORMATION_MESSAGE);
    }
    else
    {
      _lstMediaLibraryDirs.setListData(_guiDriver.getMediaLibrary().getDirectories().toArray());
    }

    updateMediaDirButtons();
    updateRecentMenu();
    refreshStatusLabel(null);

    ((java.awt.CardLayout) _playlistPanel.getLayout()).show(_playlistPanel, "_gettingStartedPanel");

    initPlaylistListener();

    if (!WinampHelper.isWinampInstalled())
    {
      _batchRepairWinampMenuItem.setVisible(false);
      _extractPlaylistsMenuItem.setVisible(false);
    }

    // drag-n-drop support for the playlist directory tree
    _playlistDirectoryTree.setTransferHandler(createPlaylistTreeTransferHandler());

    // A constructor with side-effects, required to support opening playlists that are dragged in...
    // Java... what voodoo/nonsense is this?
    new DropTarget(this, this);

    _playlistDirectoryTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
    {
      @Override
      public void valueChanged(TreeSelectionEvent e)
      {
        boolean hasSelected = _playlistDirectoryTree.getSelectionCount() > 0;
        _btnOpenSelected.setEnabled(hasSelected);
        _btnQuickRepair.setEnabled(hasSelected);
        _btnDeepRepair.setEnabled(hasSelected);
      }
    });

    // addAt popup menu to playlist tree on right-click
    _playlistDirectoryTree.addMouseListener(createPlaylistTreeMouseListener());

    addPlaylistPanelModelListener();

//    _documentPane.setGroupsAllowed(false);
//    _documentPane.setFloatingAllowed(false);
//    _documentPane.setTabbedPaneCustomizer(createTabCustomizer());
//    _documentPane.setTabColorProvider(createTabColorProvider());

    // Load the position the window was in when it was last closed.
    WindowSaver.getInstance().loadSettings(this);

    // Set the position of the divider in the left split pane.
    _leftSplitPane.setDividerLocation(.7);

    updateMenuItemStatuses();

    // JCaron - 2012.05.03 - Global listener for Ctrl-Tab and Ctrl-Shift-Tab
    configureGlobalKeyboardListener();

    // Stop showing the loading screen
    splashScreen.setVisible(false);
  }

  private void addPlaylistPanelModelListener()
  {
    _playlistDirectoryTree.getModel().addTreeModelListener(new TreeModelListener()
    {
      @Override
      public void treeNodesChanged(TreeModelEvent e)
      {
        recheckStatusOfOpenPlaylists();
      }

      @Override
      public void treeNodesInserted(TreeModelEvent e)
      {
        recheckStatusOfOpenPlaylists();
      }

      @Override
      public void treeNodesRemoved(TreeModelEvent e)
      {
        recheckStatusOfOpenPlaylists();
      }

      @Override
      public void treeStructureChanged(TreeModelEvent e)
      {
        recheckStatusOfOpenPlaylists();
      }
    });
  }

  private void recheckStatusOfOpenPlaylists()
  {
    for (Playlist playlist : _openPlaylists)
    {
      playlist.updateModifiedStatus();
    }
  }

  private void configureGlobalKeyboardListener()
  {
    KeyEventPostProcessor pp = new KeyEventPostProcessor()
    {
      @Override
      public boolean postProcessKeyEvent(KeyEvent e)
      {
        // Advance the selected tab on Ctrl-Tab (make sure Shift isn't pressed)
        if (ctrlTabWasPressed(e) && _documentPane.getDocumentCount() > 1)
        {
          _documentPane.nextDocument();
        }

        // Regress the selected tab on Ctrl-Shift-Tab
        if (ctrlShiftTabWasPressed(e) && _documentPane.getDocumentCount() > 1)
        {
          _documentPane.prevDocument();
        }

        return true;
      }

      private boolean ctrlShiftTabWasPressed(KeyEvent e)
      {
        return (e.getKeyCode() == KeyEvent.VK_TAB) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0) && _documentPane.getDocumentCount() > 0 && e.getID() == KeyEvent.KEY_PRESSED;
      }

      private boolean ctrlTabWasPressed(KeyEvent e)
      {
        return (e.getKeyCode() == KeyEvent.VK_TAB) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && ((e.getModifiers() & KeyEvent.SHIFT_MASK) == 0) && _documentPane.getDocumentCount() > 0 && e.getID() == KeyEvent.KEY_PRESSED;
      }
    };

    DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(pp);
  }

  private MouseAdapter createPlaylistTreeMouseListener()
  {
    return new MouseAdapter()
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
          else
          {
            if (isOverItem && !_playlistDirectoryTree.isRowSelected(rowIx))
            {
              _playlistDirectoryTree.setSelectionRow(rowIx);
            }
          }

          if (_playlistDirectoryTree.getSelectionCount() > 0)
          {
            _miExactMatchesSearch.setEnabled(true);
            _miClosestMatchesSearch.setEnabled(true);
            _miOpenSelectedPlaylists.setEnabled(true);
            _miDeletePlaylist.setEnabled(true);
            if (_playlistDirectoryTree.getSelectionCount() == 1)
            {
              _miRenameSelectedItem.setEnabled(true);
            }
          }
          else
          {
            _miExactMatchesSearch.setEnabled(false);
            _miClosestMatchesSearch.setEnabled(false);
            _miOpenSelectedPlaylists.setEnabled(false);
            _miDeletePlaylist.setEnabled(false);
            _miRenameSelectedItem.setEnabled(false);
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
    };
  }

  private TransferHandler createPlaylistTreeTransferHandler()
  {
    return new TransferHandler()
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
          ArrayList<String> paths = new ArrayList<>();

          for (int selRow : _playlistDirectoryTree.getSelectionRows())
          {
            TreePath selPath = _playlistDirectoryTree.getPathForRow(selRow);
            paths.add(FileTreeNodeGenerator.TreePathToFileSystemPath(selPath));
          }

          String serializedPaths = StringArrayListSerializer.serialize(paths);
          return new StringSelection(serializedPaths);
        }
        catch (IOException ex)
        {
          _logger.warn(ex);
          return null;
        }

      }
    };
  }

  private void configureFileAndFolderChoosers()
  {
    _jOpenPlaylistFileChooser.setDialogTitle("Choose Playlists...");
    _jOpenPlaylistFileChooser.setAcceptAllFileFilterUsed(false);
    _jOpenPlaylistFileChooser.setFileFilter(new PlaylistFileFilter());
    _jOpenPlaylistFileChooser.setMultiSelectionEnabled(true);

    _jMediaDirChooser.setDialogTitle("Specify a media directory...");
    _jMediaDirChooser.setAcceptAllFileFilterUsed(false);
    _jMediaDirChooser.setMinimumSize(new Dimension(400, 500));
    _jMediaDirChooser.setPreferredSize(new Dimension(400, 500));

    _jSaveFileChooser.setDialogTitle("Save File:");
    _jSaveFileChooser.setAcceptAllFileFilterUsed(false);
    _jSaveFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    _jSaveFileChooser.addChoosableFileFilter(new ExtensionFilter("m3u", "M3U Playlist (*.m3u)"));
    _jSaveFileChooser.addChoosableFileFilter(new ExtensionFilter("m3u8", "M3U8 Playlist (*.m3u8)"));
    _jSaveFileChooser.addChoosableFileFilter(new ExtensionFilter("pls", "PLS Playlist (*.pls)"));
    _jSaveFileChooser.addChoosableFileFilter(new ExtensionFilter("wpl", "WPL Playlist (*.wpl)"));
    _jSaveFileChooser.addChoosableFileFilter(new ExtensionFilter("xspf", "XSPF Playlist (*.xspf)"));
    _jSaveFileChooser.addChoosableFileFilter(new ExtensionFilter("xml", "iTunes Playlist (*.xml)"));
  }

  /**
   * @param dtde
   */
  @Override
  public void dragEnter(DropTargetDragEvent dtde)
  {

  }

  /**
   * @param dte
   */
  @Override
  public void dragExit(DropTargetEvent dte)
  {

  }

  /**
   * @param dtde
   */
  @Override
  public void dragOver(DropTargetDragEvent dtde)
  {

  }

  /**
   * @param dtde
   */
  @Override
  public void dropActionChanged(DropTargetDragEvent dtde)
  {

  }

  /**
   * @param dtde
   */
  @Override
  public void drop(DropTargetDropEvent dtde)
  {
    try
    {
      // Ok, get the dropped object and try to figure out what it is
      Transferable tr = dtde.getTransferable();
      DataFlavor[] flavors = tr.getTransferDataFlavors();
      for (DataFlavor flavor : flavors)
      {
        // Check for file lists specifically
        if (flavor.isFlavorJavaFileListType() || flavor.isFlavorTextType())
        {
          // Accept the drop...
          dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
          Object data = tr.getTransferData(flavor);
          if (data instanceof List)
          {
            // Process the drop, in this case, of file or folder paths from the OS.
            List list = (List) data;
            File tempFile;
            for (Object list1 : list)
            {
              if (list1 instanceof File)
              {
                tempFile = (File) list1;
                if (Playlist.isPlaylist(tempFile, this.getOptions()))
                {
                  openPlaylist(tempFile);
                }
                else if (tempFile.isDirectory())
                {
                  List<File> playlists = PlaylistScanner.getAllPlaylists(tempFile);
                  for (File f : playlists)
                  {
                    openPlaylist(f);
                  }
                }
              }
            }
          }
          else if (data instanceof InputStreamReader)
          {
            try (InputStreamReader list = (InputStreamReader) data; BufferedReader temp = new BufferedReader(list))
            {
              String filePath = temp.readLine();
              while (filePath != null && !filePath.isEmpty())
              {
                openPlaylist(new File(new URI(filePath)));
                filePath = temp.readLine();
              }
            }
          }
          else if (data instanceof String)
          {
            // Magically delicious string coming from the playlist panel
            String input = (String) data;
            List<String> paths = StringArrayListSerializer.deserialize(input);

            // Turn this into a list of files, and reuse the processing code above
            File tempFile;
            for (String path : paths)
            {
              tempFile = new File(path);
              if (Playlist.isPlaylist(tempFile, this.getOptions()))
              {
                openPlaylist(tempFile);
              }
              else if (tempFile.isDirectory())
              {
                List<File> playlists = PlaylistScanner.getAllPlaylists(tempFile);
                for (File f : playlists)
                {
                  openPlaylist(f);
                }
              }
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
    catch (UnsupportedFlavorException | IOException | ClassNotFoundException | URISyntaxException e)
    {
      _logger.warn(e);
      dtde.rejectDrop();
    }
  }

  /**
   * @return
   */
  public IAppOptions getOptions()
  {
    return this._guiDriver.getApplicationConfiguration().getConfig();
  }

  public IMediaLibrary getMediaLibrary()
  {
    return this._guiDriver.getMediaLibrary();
  }

  private void fireOptionsPopup()
  {
    final ApplicationOptionsConfiguration applicationConfiguration = this._guiDriver.getApplicationConfiguration();
    final String oldPlaylistsDirectory = applicationConfiguration.getConfig().getPlaylistsDirectory();
    AppOptionsDialog optDialog = new AppOptionsDialog(this, "listFix() options", true, (JsonAppOptions) applicationConfiguration.getConfig());
    JsonAppOptions options = optDialog.showDialog();
    if (optDialog.getResultCode() == AppOptionsDialog.OK)
    {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      _guiDriver.setAppOptions(options);
      _guiDriver.getHistory().setCapacity(options.getMaxPlaylistHistoryEntries());
      final MediaLibraryConfiguration mediaLibraryConfiguration = _guiDriver.getMediaLibrarConfiguration();
      if (options.getAlwaysUseUNCPaths())
      {
        mediaLibraryConfiguration.switchMediaLibraryToUNCPaths();
        _lstMediaLibraryDirs.setListData(_guiDriver.getMediaLibrary().getDirectories().toArray());
      }
      else
      {
        _guiDriver.switchMediaLibraryToMappedDrives();
        _lstMediaLibraryDirs.setListData(_guiDriver.getMediaLibrary().getDirectories().toArray());
      }
      try
      {
        applicationConfiguration.write();
      }
      catch (IOException e)
      {
        _logger.error("Writing application configuration", e);
      }
      if (!oldPlaylistsDirectory.equals(options.getPlaylistsDirectory()))
      {
        _playlistDirectoryTree.setModel(new DefaultTreeModel(FileTreeNodeGenerator.addNodes(null, new File(applicationConfiguration.getConfig().getPlaylistsDirectory()))));
      }
      updateRecentMenu();
      setApplicationFont(options.getAppFont());
      setLookAndFeel(options.getLookAndFeel());
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    _playlistTreeRightClickMenu = new javax.swing.JPopupMenu();
    _miRefreshDirectoryTree = new javax.swing.JMenuItem();
    _miOpenSelectedPlaylists = new javax.swing.JMenuItem();
    _miOpenSelectedPlaylistLocation = new javax.swing.JMenuItem();
    _miExactMatchesSearch = new javax.swing.JMenuItem();
    _miClosestMatchesSearch = new javax.swing.JMenuItem();
    _miDeletePlaylist = new javax.swing.JMenuItem();
    _miRenameSelectedItem = new javax.swing.JMenuItem();
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
    _lstMediaLibraryDirs = new javax.swing.JList(new String[]{"Please Add A Media Directory..."});
    _playlistDirectoryPanel = new javax.swing.JPanel();
    _treeScrollPane = new javax.swing.JScrollPane();
    _playlistDirectoryTree = new javax.swing.JTree(FileTreeNodeGenerator.addNodes(null, new File(getApplicationConfig().getPlaylistsDirectory())));
    _playlistsDirectoryButtonPanel = new javax.swing.JPanel();
    _btnSetPlaylistsDir = new javax.swing.JButton();
    _btnRefresh = new javax.swing.JButton();
    _btnOpenSelected = new javax.swing.JButton();
    _btnQuickRepair = new javax.swing.JButton();
    _btnDeepRepair = new javax.swing.JButton();
    _playlistPanel = new javax.swing.JPanel();
    _gettingStartedPanel = new javax.swing.JPanel();
    _verticalPanel = new javax.swing.JPanel();
    _openIconButton = new javax.swing.JButton();
    _spacerPanel = new javax.swing.JPanel();
    _newIconButton = new javax.swing.JButton();
    _docTabPanel = new javax.swing.JPanel();
    _documentPane = new JDocumentTabbedPane();
    _mainMenuBar = new javax.swing.JMenuBar();
    _fileMenu = new JMenu();
    _newPlaylistMenuItem = new javax.swing.JMenuItem();
    _loadMenuItem = new javax.swing.JMenuItem();
    _openPlaylistLocationMenuItem = new javax.swing.JMenuItem();
    _closeMenuItem = new javax.swing.JMenuItem();
    _closeAllMenuItem = new javax.swing.JMenuItem();
    jSeparator1 = new javax.swing.JPopupMenu.Separator();
    _saveMenuItem = new javax.swing.JMenuItem();
    _saveAsMenuItem = new javax.swing.JMenuItem();
    _saveAllMenuItem = new javax.swing.JMenuItem();
    jSeparator2 = new javax.swing.JPopupMenu.Separator();
    _miReload = new javax.swing.JMenuItem();
    _miReloadAll = new javax.swing.JMenuItem();
    jSeparator3 = new javax.swing.JPopupMenu.Separator();
    recentMenu = new javax.swing.JMenu();
    _clearHistoryMenuItem = new javax.swing.JMenuItem();
    jSeparator4 = new javax.swing.JPopupMenu.Separator();
    _appOptionsMenuItem = new javax.swing.JMenuItem();
    jSeparator5 = new javax.swing.JPopupMenu.Separator();
    _exitMenuItem = new javax.swing.JMenuItem();
    _repairMenu = new JMenu();
    _miBatchRepair = new javax.swing.JMenuItem();
    _miExactMatchRepairOpenPlaylists = new javax.swing.JMenuItem();
    _miClosestMatchRepairOpenPlaylists = new javax.swing.JMenuItem();
    _batchRepairWinampMenuItem = new javax.swing.JMenuItem();
    _extractPlaylistsMenuItem = new javax.swing.JMenuItem();
    _helpMenu = new JMenu();
    _helpMenuItem = new javax.swing.JMenuItem();
    _updateCheckMenuItem = new javax.swing.JMenuItem();
    _aboutMenuItem = new javax.swing.JMenuItem();

    _miRefreshDirectoryTree.setText("Refresh");
    _miRefreshDirectoryTree.addActionListener(evt -> _miRefreshDirectoryTreeActionPerformed(evt));
    _playlistTreeRightClickMenu.add(_miRefreshDirectoryTree);

    _miOpenSelectedPlaylists.setText("Open");
    _miOpenSelectedPlaylists.addActionListener(evt -> _miOpenSelectedPlaylistsActionPerformed(evt));
    _playlistTreeRightClickMenu.add(_miOpenSelectedPlaylists);

    _miOpenSelectedPlaylistLocation.setText("Open Playlist Location");
    _miOpenSelectedPlaylistLocation.addActionListener(evt -> this.openPlaylistFoldersFromPlaylistTree());
    _playlistTreeRightClickMenu.add(_miOpenSelectedPlaylistLocation);

    _miExactMatchesSearch.setText("Find Exact Matches");
    _miExactMatchesSearch.addActionListener(evt -> _miExactMatchesSearchActionPerformed(evt));
    _playlistTreeRightClickMenu.add(_miExactMatchesSearch);

    _miClosestMatchesSearch.setText("Find Closest Matches");
    _miClosestMatchesSearch.addActionListener(evt -> _miClosestMatchesSearchActionPerformed(evt));
    _playlistTreeRightClickMenu.add(_miClosestMatchesSearch);

    _miDeletePlaylist.setMnemonic('D');
    _miDeletePlaylist.setText("Delete Selected");
    _miDeletePlaylist.setToolTipText("Delete Selected Folders & Playlists");
    _miDeletePlaylist.addActionListener(evt -> _miDeletePlaylistActionPerformed(evt));
    _playlistTreeRightClickMenu.add(_miDeletePlaylist);

    _miRenameSelectedItem.setMnemonic('R');
    _miRenameSelectedItem.setAccelerator(KeyStroke.getKeyStroke(keyEventRenameFile, 0));
    _miRenameSelectedItem.setText("Rename");
    _miRenameSelectedItem.setToolTipText("Rename selected file or folder");
    _miRenameSelectedItem.addActionListener(evt -> _miRenameSelectedItemActionPerformed(evt));
    _playlistTreeRightClickMenu.add(_miRenameSelectedItem);

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    setTitle(String.format("listFix() version %s", applicationVersion));
    setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    setIconImage(this.applicationIcon);
    setMinimumSize(new java.awt.Dimension(600, 149));
    setName("mainFrame"); // NOI18N
    addComponentListener(new java.awt.event.ComponentAdapter()
    {
      public void componentResized(java.awt.event.ComponentEvent evt)
      {
        formComponentResized(evt);
      }
    });
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        exitForm(evt);
      }
    });

    _statusPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    _statusPanel.setLayout(new java.awt.BorderLayout());

    statusLabel.setForeground(new java.awt.Color(75, 75, 75));
    statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    statusLabel.setText("Untitled List     Number of entries in list: 0     Number of lost entries: 0     Number of URLs: 0     Number of open playlists: 0");
    _statusPanel.add(statusLabel, java.awt.BorderLayout.WEST);

    getContentPane().add(_statusPanel, java.awt.BorderLayout.SOUTH);

    _splitPane.setDividerSize(7);
    _splitPane.setContinuousLayout(true);
    _splitPane.setMaximumSize(null);
    _splitPane.setOneTouchExpandable(true);
    _splitPane.setPreferredSize(new java.awt.Dimension(785, 489));

    _leftSplitPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    _leftSplitPane.setDividerLocation(280);
    _leftSplitPane.setDividerSize(7);
    _leftSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
    _leftSplitPane.setContinuousLayout(true);
    _leftSplitPane.setMaximumSize(null);
    _leftSplitPane.setOneTouchExpandable(true);

    _mediaLibraryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Media Directories", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP));
    _mediaLibraryPanel.setAlignmentX(0.0F);
    _mediaLibraryPanel.setAlignmentY(0.0F);
    _mediaLibraryPanel.setLayout(new java.awt.BorderLayout());

    _mediaLibraryButtonPanel.setMaximumSize(null);

    _addMediaDirButton.setText("Add");
    _addMediaDirButton.setToolTipText("Where do you keep your music?");
    _addMediaDirButton.setFocusable(false);
    _addMediaDirButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
    _addMediaDirButton.setMinimumSize(new java.awt.Dimension(53, 25));
    _addMediaDirButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        _addMediaDirButtonActionPerformed(evt);
      }
    });
    _mediaLibraryButtonPanel.add(_addMediaDirButton);

    _removeMediaDirButton.setText("Remove");
    _removeMediaDirButton.setToolTipText("Remove a directory from the search list");
    _removeMediaDirButton.setFocusable(false);
    _removeMediaDirButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
    _removeMediaDirButton.setMinimumSize(new java.awt.Dimension(73, 25));
    _removeMediaDirButton.addActionListener(evt -> _removeMediaDirButtonActionPerformed(evt));
    _mediaLibraryButtonPanel.add(_removeMediaDirButton);

    _refreshMediaDirsButton.setText("Refresh");
    _refreshMediaDirsButton.setToolTipText("The contents of your media library are cached; refresh to pickup changes");
    _refreshMediaDirsButton.setFocusable(false);
    _refreshMediaDirsButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
    _refreshMediaDirsButton.setMinimumSize(new java.awt.Dimension(71, 25));
    _refreshMediaDirsButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        _refreshMediaDirsButtonActionPerformed(evt);
      }
    });
    _mediaLibraryButtonPanel.add(_refreshMediaDirsButton);

    _mediaLibraryPanel.add(_mediaLibraryButtonPanel, java.awt.BorderLayout.SOUTH);

    _mediaLibraryScrollPane.setMaximumSize(null);
    _mediaLibraryScrollPane.setMinimumSize(null);

    _lstMediaLibraryDirs.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    _lstMediaLibraryDirs.setMaximumSize(null);
    _lstMediaLibraryDirs.setMinimumSize(null);
    _lstMediaLibraryDirs.setPreferredSize(null);
    _mediaLibraryScrollPane.setViewportView(_lstMediaLibraryDirs);

    _mediaLibraryPanel.add(_mediaLibraryScrollPane, java.awt.BorderLayout.CENTER);

    _leftSplitPane.setBottomComponent(_mediaLibraryPanel);

    _playlistDirectoryPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Playlists Directory", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP));
    _playlistDirectoryPanel.setAlignmentX(0.0F);
    _playlistDirectoryPanel.setAlignmentY(0.0F);
    _playlistDirectoryPanel.setLayout(new java.awt.BorderLayout());

    _treeScrollPane.setMaximumSize(null);
    _treeScrollPane.setMinimumSize(null);

    _playlistDirectoryTree.setDragEnabled(true);
    _playlistDirectoryTree.setMaximumSize(null);
    _playlistDirectoryTree.setMinimumSize(null);
    _playlistDirectoryTree.setPreferredSize(null);
    _playlistDirectoryTree.addKeyListener(new KeyAdapter()
    {
      public void keyPressed(java.awt.event.KeyEvent evt)
      {
        _playlistDirectoryTreeKeyPressed(evt);
      }
    });
    _treeScrollPane.setViewportView(_playlistDirectoryTree);

    _playlistDirectoryPanel.add(_treeScrollPane, java.awt.BorderLayout.CENTER);

    _playlistsDirectoryButtonPanel.setMaximumSize(null);
    _playlistsDirectoryButtonPanel.setMinimumSize(new java.awt.Dimension(300, 35));
    _playlistsDirectoryButtonPanel.setName(""); // NOI18N

    _btnSetPlaylistsDir.setText("Set");
    _btnSetPlaylistsDir.setToolTipText("Opens the options screen where you can set your playlists directory ");
    _btnSetPlaylistsDir.setMargin(new java.awt.Insets(2, 8, 2, 8));
    _btnSetPlaylistsDir.addActionListener(evt -> _btnSetPlaylistsDirActionPerformed(evt));
    _playlistsDirectoryButtonPanel.add(_btnSetPlaylistsDir);

    _btnRefresh.setText("Refresh");
    _btnRefresh.setToolTipText("Refresh Playlists Tree");
    _btnRefresh.setFocusable(false);
    _btnRefresh.setMargin(new java.awt.Insets(2, 8, 2, 8));
    _btnRefresh.setMinimumSize(new java.awt.Dimension(71, 25));
    _btnRefresh.addActionListener(evt -> _btnRefreshActionPerformed(evt));
    _playlistsDirectoryButtonPanel.add(_btnRefresh);

    _btnOpenSelected.setText("Open");
    _btnOpenSelected.setToolTipText("Open Selected Playlist(s)");
    _btnOpenSelected.setEnabled(false);
    _btnOpenSelected.setFocusable(false);
    _btnOpenSelected.setMargin(new java.awt.Insets(2, 8, 2, 8));
    _btnOpenSelected.setMinimumSize(new java.awt.Dimension(71, 25));
    _btnOpenSelected.addActionListener(evt -> _btnOpenSelectedActionPerformed(evt));
    _playlistsDirectoryButtonPanel.add(_btnOpenSelected);

    _btnQuickRepair.setText("Quick");
    _btnQuickRepair.setToolTipText("Quick Batch Repair");
    _btnQuickRepair.setEnabled(false);
    _btnQuickRepair.setMargin(new java.awt.Insets(2, 8, 2, 8));
    _btnQuickRepair.addActionListener(evt -> _btnQuickRepairActionPerformed(evt));
    _playlistsDirectoryButtonPanel.add(_btnQuickRepair);

    _btnDeepRepair.setText("Deep");
    _btnDeepRepair.setToolTipText("Deep Batch Repair");
    _btnDeepRepair.setEnabled(false);
    _btnDeepRepair.setMargin(new java.awt.Insets(2, 8, 2, 8));
    _btnDeepRepair.addActionListener(evt -> _btnDeepRepairActionPerformed(evt));
    _playlistsDirectoryButtonPanel.add(_btnDeepRepair);

    _playlistDirectoryPanel.add(_playlistsDirectoryButtonPanel, java.awt.BorderLayout.PAGE_END);

    _leftSplitPane.setTopComponent(_playlistDirectoryPanel);

    _splitPane.setLeftComponent(_leftSplitPane);

    _playlistPanel.setBackground(java.awt.SystemColor.window);
    _playlistPanel.setLayout(new java.awt.CardLayout());

    _gettingStartedPanel.setBackground(new java.awt.Color(255, 255, 255));
    _gettingStartedPanel.setLayout(new java.awt.GridBagLayout());

    _verticalPanel.setBackground(new java.awt.Color(255, 255, 255));
    _verticalPanel.setLayout(new javax.swing.BoxLayout(_verticalPanel, javax.swing.BoxLayout.Y_AXIS));

    _openIconButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open-big.png"))); // NOI18N
    _openIconButton.setText("Open a Playlist");
    _openIconButton.setToolTipText("Open a Playlist");
    _openIconButton.setAlignmentY(0.0F);
    _openIconButton.setFocusable(false);
    _openIconButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    _openIconButton.setIconTextGap(-2);
    _openIconButton.setMaximumSize(new java.awt.Dimension(220, 180));
    _openIconButton.setMinimumSize(new java.awt.Dimension(220, 180));
    _openIconButton.setPreferredSize(new java.awt.Dimension(220, 180));
    _openIconButton.setVerticalAlignment(javax.swing.SwingConstants.TOP);
    _openIconButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    _openIconButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
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
    _newIconButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        _newIconButtonActionPerformed(evt);
      }
    });
    _verticalPanel.add(_newIconButton);

    _gettingStartedPanel.add(_verticalPanel, new java.awt.GridBagConstraints());

    _playlistPanel.add(_gettingStartedPanel, "_gettingStartedPanel");

    _docTabPanel.setLayout(new java.awt.BorderLayout());
    _docTabPanel.add(_documentPane, java.awt.BorderLayout.CENTER);

    _playlistPanel.add(_docTabPanel, "_docTabPanel");

    _splitPane.setRightComponent(_playlistPanel);

    getContentPane().add(_splitPane, java.awt.BorderLayout.CENTER);

    _mainMenuBar.setBorder(null);

    _fileMenu.setMnemonic('F');
    _fileMenu.setText("File");

    _newPlaylistMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _newPlaylistMenuItem.setMnemonic('N');
    _newPlaylistMenuItem.setText("New Playlist");
    _newPlaylistMenuItem.setToolTipText("Creates a New Playlist");
    _newPlaylistMenuItem.addActionListener(evt -> _newPlaylistMenuItemActionPerformed(evt));
    _fileMenu.add(_newPlaylistMenuItem);

    _loadMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _loadMenuItem.setMnemonic('O');
    _loadMenuItem.setText("Open Playlist");
    _loadMenuItem.setToolTipText("Opens a Playlist");
    _loadMenuItem.addActionListener(evt -> openIconButtonActionPerformed(evt));
    _fileMenu.add(_loadMenuItem);

    _openPlaylistLocationMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _openPlaylistLocationMenuItem.setMnemonic('E');
    _openPlaylistLocationMenuItem.setText("Open Playlist Location");
    _openPlaylistLocationMenuItem.setToolTipText("Opens playlist folder in Explorer");
    _openPlaylistLocationMenuItem.addActionListener(evt -> openPlaylistLocation(this._currentPlaylist));
    _fileMenu.add(_openPlaylistLocationMenuItem);


    _closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _closeMenuItem.setMnemonic('C');
    _closeMenuItem.setText("Close");
    _closeMenuItem.setToolTipText("Closes The Current Playlist");
    _closeMenuItem.addActionListener(evt -> _closeMenuItemActionPerformed(evt));
    _fileMenu.add(_closeMenuItem);

    _closeAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _closeAllMenuItem.setMnemonic('A');
    _closeAllMenuItem.setText("Close All");
    _closeAllMenuItem.setToolTipText("Closes All Open Playlists");
    _closeAllMenuItem.addActionListener(evt -> _closeAllMenuItemActionPerformed(evt));
    _fileMenu.add(_closeAllMenuItem);
    _fileMenu.add(jSeparator1);

    _saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _saveMenuItem.setMnemonic('S');
    _saveMenuItem.setText("Save");
    _saveMenuItem.addActionListener(evt -> _saveMenuItemActionPerformed(evt));
    _fileMenu.add(_saveMenuItem);

    _saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _saveAsMenuItem.setMnemonic('V');
    _saveAsMenuItem.setText("Save As");
    _saveAsMenuItem.addActionListener(evt -> _saveAsMenuItemActionPerformed(evt));
    _fileMenu.add(_saveAsMenuItem);

    _saveAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _saveAllMenuItem.setMnemonic('S');
    _saveAllMenuItem.setText("Save All");
    _saveAllMenuItem.setToolTipText("Save All Open Playlists");
    _saveAllMenuItem.addActionListener(evt -> _saveAllMenuItemActionPerformed(evt));
    _fileMenu.add(_saveAllMenuItem);
    _fileMenu.add(jSeparator2);

    _miReload.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _miReload.setMnemonic('R');
    _miReload.setText("Reload");
    _miReload.setToolTipText("Reloads The Currently Open Playlist");
    _miReload.addActionListener(evt -> _miReloadActionPerformed(evt));
    _fileMenu.add(_miReload);

    _miReloadAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _miReloadAll.setMnemonic('l');
    _miReloadAll.setText("Reload All");
    _miReloadAll.setToolTipText("Reloads All Currently Open Playlists");
    _miReloadAll.addActionListener(evt -> _miReloadAllActionPerformed(evt));
    _fileMenu.add(_miReloadAll);
    _fileMenu.add(jSeparator3);

    recentMenu.setText("Recent Playlists");
    recentMenu.setToolTipText("Recently Opened Playlists");
    _fileMenu.add(recentMenu);

    _clearHistoryMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _clearHistoryMenuItem.setMnemonic('H');
    _clearHistoryMenuItem.setText("Clear Playlist History");
    _clearHistoryMenuItem.setToolTipText("Clears the recently opened playlist history");
    _clearHistoryMenuItem.addActionListener(evt -> _clearHistoryMenuItemActionPerformed(evt));
    _fileMenu.add(_clearHistoryMenuItem);
    _fileMenu.add(jSeparator4);

    _appOptionsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.ALT_DOWN_MASK));
    _appOptionsMenuItem.setMnemonic('p');
    _appOptionsMenuItem.setText("Options...");
    _appOptionsMenuItem.setToolTipText("Opens the Options Screen");
    _appOptionsMenuItem.addActionListener(evt -> _appOptionsMenuItemActionPerformed(evt));
    _fileMenu.add(_appOptionsMenuItem);
    _fileMenu.add(jSeparator5);

    _exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_DOWN_MASK));
    _exitMenuItem.setMnemonic('x');
    _exitMenuItem.setText("Exit");
    _exitMenuItem.addActionListener(evt -> _exitMenuItemActionPerformed(evt));
    _fileMenu.add(_exitMenuItem);

    _mainMenuBar.add(_fileMenu);

    _repairMenu.setText("Repair");

    _miBatchRepair.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _miBatchRepair.setMnemonic('E');
    _miBatchRepair.setText("Exact Matches Repair...");
    _miBatchRepair.setToolTipText("Runs an \"Exact Matches Repair\" on lists chosen from the file system");
    _miBatchRepair.addActionListener(evt -> onMenuBatchRepairActionPerformed(evt));
    _repairMenu.add(_miBatchRepair);

    _miExactMatchRepairOpenPlaylists.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _miExactMatchRepairOpenPlaylists.setText("Quick Repair Currently Open Playlists");
    _miExactMatchRepairOpenPlaylists.setToolTipText("Runs an \"Exact Matches Repair\" on all open playlists");
    _miExactMatchRepairOpenPlaylists.addActionListener(evt -> _miExactMatchRepairOpenPlaylistsonMenuBatchRepairActionPerformed(evt));
    _repairMenu.add(_miExactMatchRepairOpenPlaylists);

    _miClosestMatchRepairOpenPlaylists.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _miClosestMatchRepairOpenPlaylists.setText("Deep Repair Currently Open Playlists");
    _miClosestMatchRepairOpenPlaylists.setToolTipText("Runs an \"Closest Matches Repair\" on all open playlists");
    _miClosestMatchRepairOpenPlaylists.addActionListener(evt -> _miClosestMatchRepairOpenPlaylistsonMenuBatchRepairActionPerformed(evt));
    _repairMenu.add(_miClosestMatchRepairOpenPlaylists);

    _batchRepairWinampMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.ALT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _batchRepairWinampMenuItem.setMnemonic('B');
    _batchRepairWinampMenuItem.setText("Batch Repair Winamp Media Library Playlists...");
    _batchRepairWinampMenuItem.addActionListener(evt -> _batchRepairWinampMenuItemActionPerformed(evt));
    _repairMenu.add(_batchRepairWinampMenuItem);

    _extractPlaylistsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _extractPlaylistsMenuItem.setMnemonic('W');
    _extractPlaylistsMenuItem.setText("Extract Winamp Media Library Playlists");
    _extractPlaylistsMenuItem.setToolTipText("Extract Winamp Media Library Playlists");
    _extractPlaylistsMenuItem.addActionListener(evt -> _extractPlaylistsMenuItemActionPerformed(evt));
    _repairMenu.add(_extractPlaylistsMenuItem);

    _mainMenuBar.add(_repairMenu);

    _helpMenu.setMnemonic('H');
    _helpMenu.setText("Help");

    _helpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
    _helpMenuItem.setMnemonic('H');
    _helpMenuItem.setText("Help");
    _helpMenuItem.setToolTipText("Open listFix() documentation");
    _helpMenuItem.addActionListener(evt -> _helpMenuItemActionPerformed(evt));
    _helpMenu.add(_helpMenuItem);

    _updateCheckMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    _updateCheckMenuItem.setMnemonic('C');
    _updateCheckMenuItem.setText("Check For Updates");
    _updateCheckMenuItem.setToolTipText("Opens the listFix() download site");
    _updateCheckMenuItem.addActionListener(evt -> _updateCheckMenuItemActionPerformed(evt));
    _helpMenu.add(_updateCheckMenuItem);

    _aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.ALT_DOWN_MASK));
    _aboutMenuItem.setMnemonic('A');
    _aboutMenuItem.setText("About");
    _aboutMenuItem.setToolTipText("Version info and such...");
    _aboutMenuItem.addActionListener(evt -> _aboutMenuItemActionPerformed(evt));
    _helpMenu.add(_aboutMenuItem);

    _mainMenuBar.add(_helpMenu);

    _documentPane.addDocumentChangeListener(new IDocumentChangeListener()
    {
      @Override
      public boolean tryClosingDocument(JDocumentComponent document)
      {
        return GUIScreen.this.tryCloseTab(document);
      }

      @Override
      public void documentOpened(JDocumentComponent document)
      {
        updateMenuItemStatuses();
      }

      @Override
      public void documentClosed(JDocumentComponent document)
      {
        final Playlist playlist = getPlaylistFromDocumentComponent(document);
        cleanupOnTabClose(playlist);
        if (_documentPane.getDocumentCount() == 0)
        {
          ((java.awt.CardLayout) _playlistPanel.getLayout()).show(_playlistPanel, "_gettingStartedPanel");
          currentTabChanged();
          updateMenuItemStatuses();
        }
      }

      @Override
      public void documentActivated(JDocumentComponent doc)
      {
        GUIScreen.this.currentTabChanged(doc);
      }

    });

    setJMenuBar(_mainMenuBar);

    this.componentsRequireActivePlaylist = new Component[]{
      _openPlaylistLocationMenuItem,
      _saveAllMenuItem,
      _saveAsMenuItem,
      _saveMenuItem,

      _closeAllMenuItem,
      _closeMenuItem,

      _miReload,
      _miReloadAll,

      _miExactMatchRepairOpenPlaylists,
      _miClosestMatchRepairOpenPlaylists,
    };

    splashScreen.setIconImage(applicationIcon);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  /**
   * Checked
   */
  private void runClosestMatchesSearchOnSelectedLists()
  {
    _logger.debug("Run Closest Matches Search...");
    List<File> files = this.getRecursiveSelectedFilesFromTreePlaylists();
    if (files.isEmpty())
    {
      return;
    }
    BatchRepair br = new BatchRepair(_guiDriver.getMediaLibrary(), files.get(0));
    br.setDescription("Closest Matches Search");
    for (File file : files)
    {
      br.add(new BatchRepairItem(file, this.getOptions()));
    }
    MultiListBatchClosestMatchResultsDialog dlg = new MultiListBatchClosestMatchResultsDialog(this, true, br, this.getOptions());
    if (!dlg.getUserCancelled())
    {
      if (br.isEmpty())
      {
        JOptionPane.showMessageDialog(this, new JTransparentTextArea("There was nothing to fix in the list(s) that were processed."));
      }
      else
      {
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        updatePlaylistDirectoryPanel();
      }
    }
  }

  private void runExactMatchesSearchOnSelectedPlaylists()
  {
    _logger.debug("Run Exact Matches Search...");
    List<File> files = this.getRecursiveSelectedFilesFromTreePlaylists();
    if (files.isEmpty())
    {
      _logger.info("Abort search, no files selected");
      return;
    }
    BatchRepair br = new BatchRepair(_guiDriver.getMediaLibrary(), files.get(0));
    br.setDescription("Exact Matches Search");
    for (File file : files)
    {
      br.add(new BatchRepairItem(file, this.getOptions()));
    }
    BatchExactMatchesResultsDialog dlg = new BatchExactMatchesResultsDialog(this, true, br, this.getOptions());
    if (!dlg.getUserCancelled())
    {
      if (br.isEmpty())
      {
        JOptionPane.showMessageDialog(this, new JTransparentTextArea("There was nothing to fix in the list(s) that were processed."));
      }
      else
      {
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        updatePlaylistDirectoryPanel();
      }
    }
  }

  private void openTreeSelectedPlaylists()
  {
    this.getSelectedFilesFromTreePlaylists().forEach(toOpen -> {
      if (toOpen.isDirectory())
      {
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
    });
  }

  private List<File> getRecursiveSelectedFilesFromTreePlaylists() {
    List<File> files = new ArrayList<>();
    for (File file : this.getSelectedFilesFromTreePlaylists())
    {
      if (file.isFile())
      {
        files.add(file);
      }
      else
      {
        // We're dealing w/ a folder, get all the lists it contains.
        files.addAll(PlaylistScanner.getAllPlaylists(file));
      }
    }
    return files;
  }

  private List<File> getSelectedFilesFromTreePlaylists()
  {
    TreePath[] paths = _playlistDirectoryTree.getSelectionPaths();
    if (paths == null)
    {
      return Collections.emptyList();
    }
    return Arrays.stream(paths).map(selPath -> new File(FileTreeNodeGenerator.TreePathToFileSystemPath(selPath))).collect(Collectors.toList());
  }

  private void deleteTreeSelectedPlaylists()
  {
    int[] selRows = _playlistDirectoryTree.getSelectionRows();
    if (selRows != null && selRows.length > 0)
    {
      if (JOptionPane.showConfirmDialog(this, new JTransparentTextArea("Are you sure you want to delete the selected files and folders?"), "Delete Selected Files & Folders?", JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION)
      {
        File toOpen;
        List<TreePath> selPaths = new ArrayList<>();
        for (int i : selRows)
        {
          selPaths.add(_playlistDirectoryTree.getPathForRow(i));
        }
        for (TreePath selPath : selPaths)
        {
          toOpen = new File(FileTreeNodeGenerator.TreePathToFileSystemPath(selPath));
          if (toOpen.isDirectory())
          {
            FileUtils.deleteDirectory(toOpen);
            DefaultTreeModel treeModel = (DefaultTreeModel) _playlistDirectoryTree.getModel();
            treeModel.removeNodeFromParent((MutableTreeNode) selPath.getLastPathComponent());
          }
          else
          {
            if (toOpen.canWrite())
            {
              toOpen.delete();
              _playlistDirectoryTree.makeVisible(selPath);
              DefaultTreeModel treeModel = (DefaultTreeModel) _playlistDirectoryTree.getModel();
              treeModel.removeNodeFromParent((MutableTreeNode) selPath.getLastPathComponent());
            }
          }
        }
      }
    }
  }

  private void renameTreeSelectedNode()
  {
    int[] selRows = _playlistDirectoryTree.getSelectionRows();
    DefaultMutableTreeNode curNode;
    DefaultTreeModel treeModel = (DefaultTreeModel) _playlistDirectoryTree.getModel();
    TreeNodeFile nodeFile;
    if (selRows != null && selRows.length > 0)
    {
      List<TreePath> selPaths = new ArrayList<>();
      for (int i : selRows)
      {
        selPaths.add(_playlistDirectoryTree.getPathForRow(i));
      }
      for (TreePath selPath : selPaths)
      {
        curNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();
        nodeFile = (TreeNodeFile) curNode.getUserObject();
        String str = curNode.toString();
        String reply = JOptionPane.showInputDialog(this, new JTransparentTextArea("Rename " + str), "." + FileUtils.getFileExtension(nodeFile.getName()));
        if (reply != null && !"".equals(reply))
        {
          TreeNodeFile destFile = new TreeNodeFile(nodeFile.getParent() + Constants.FS + reply);
          nodeFile.renameTo(destFile);
          curNode.setUserObject(destFile);
          treeModel.nodeChanged(curNode);
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
    try
    {
      _guiDriver.clearM3UHistory();
    }
    catch (IOException e)
    {
      _logger.error("Error clear M3U history", e);
    }
    updateRecentMenu();
  }//GEN-LAST:event__clearHistoryMenuItemActionPerformed

  private void _saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__saveAsMenuItemActionPerformed
  {//GEN-HEADEREND:event__saveAsMenuItemActionPerformed
    if (_currentPlaylist == null)
    {
      return;
    }

    handleSaveAs(_currentPlaylist);
  }//GEN-LAST:event__saveAsMenuItemActionPerformed

  private void openIconButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openIconButtonActionPerformed
  {//GEN-HEADEREND:event_openIconButtonActionPerformed
    if (_currentPlaylist != null)
    {
      _jOpenPlaylistFileChooser.setSelectedFile(_currentPlaylist.getFile());
    }
    int response = _jOpenPlaylistFileChooser.showOpenDialog(this);
    if (response == JFileChooser.APPROVE_OPTION)
    {
      File[] playlists = _jOpenPlaylistFileChooser.getSelectedFiles();
      for (File file : playlists)
      {
        this.openPlaylist(file);
      }
    }
  }//GEN-LAST:event_openIconButtonActionPerformed(java.awt.event.ActionEvent evt)

  private void openPlaylistFoldersFromPlaylistTree()
  {
    this.getSelectedFilesFromTreePlaylists().stream()
      .map(file -> file.isDirectory() ? file : file.getParentFile())
      .filter(Objects :: nonNull)
      .distinct()
      .forEach(this :: openFolderInExplorerPerformed);
  }

  private void openPlaylistLocation(Playlist playList)
  {
    this.openFolderInExplorerPerformed(playList.getFile().getParentFile());
  }

  private void openFolderInExplorerPerformed(File folder)
  {
    if (folder != null)
    {
      try
      {
        Desktop.getDesktop().open(folder);
      }
      catch (IOException e)
      {
        _logger.warn("Failed to open playlist folder location", e);
        throw new RuntimeException(e);
      }
    }
  }

  public void openPlaylist(final File file)
  {
    // do nothing if the file is already open.
    for (Playlist list : _openPlaylists)
    {
      if (list.getFile().equals(file))
      {
        String path = list.getFile().getPath();
        _documentPane.setActiveDocument(path);
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
        JOptionPane.showMessageDialog(this, new JTransparentTextArea("File Not Found."), "Open Playlist Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    catch (IOException | HeadlessException ex)
    {
      _logger.error("Open playlist error", ex);
      JOptionPane.showMessageDialog(this, new JTransparentTextArea(ExStack.textFormatErrorForUser("There was a problem opening the file you selected.", ex.getCause())),
        "Open Playlist Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    JDocumentComponent tempComp = _documentPane.getDocument(path);
    if (tempComp == null)
    {
      PlaylistType type = Playlist.determinePlaylistTypeFromExtension(file, this.getOptions());

      ProgressWorker<Playlist, String> worker = new ProgressWorker<>()
      {
        @Override
        protected Playlist doInBackground() throws Exception
        {
          this.setMessage("Please wait while your playlist is opened and analyzed.");
          Playlist list = PlaylistFactory.getPlaylist(file, this, GUIScreen.this.getOptions());
          if (getApplicationConfig().getAutoLocateEntriesOnPlaylistLoad())
          {
            list.repair(GUIScreen.this.getMediaLibrary(), this);
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
          catch (InterruptedException | ExecutionException ex)
          {
            _logger.error("Open playlist error", ex);

            JOptionPane.showMessageDialog(GUIScreen.this, new JTransparentTextArea(ExStack.textFormatErrorForUser("There was a problem opening the file you selected, are you sure it was a playlist?", ex.getCause())),
              "Open Playlist Error", JOptionPane.ERROR_MESSAGE);
            return;
          }

          openNewTabForPlaylist(list);

          // update playlist history
          PlaylistHistory history = _guiDriver.getHistory();
          history.add(path);
          try
          {
            history.write();
          }
          catch (IOException e)
          {
            _logger.error("Error", e);
          }

          updateRecentMenu();
        }
      };

      boolean textOnly = false;
      if (type == PlaylistType.ITUNES || type == PlaylistType.XSPF)
      {
        // Can't show a progress dialog for these as we have no way to track them at present.
        textOnly = true;
      }
      ProgressDialog pd = new ProgressDialog(this, true, worker, "Loading '" + (file.getName().length() > 70 ? file.getName().substring(0, 70) : file.getName()) + "'...", textOnly, true);
      pd.setVisible(true);
    }
    else
    {
      _documentPane.setActiveDocument(path);
    }
  }

  public void openNewTabForPlaylist(Playlist list)
  {
    PlaylistEditCtrl editor = new PlaylistEditCtrl(this);
    editor.setPlaylist(list);

    // Add the tab to the tabbed pane
    String title = list.getFilename();
    Path path = list.getFile().toPath().normalize();

    final JDocumentComponent tempComp = createDocumentComponentForEditor(editor, path, title);
    tempComp.setTooltip(list.getFile().getPath());

    _documentPane.openDocument(tempComp);
    _documentPane.setActiveDocument(tempComp.getName());

    // Tie the DocumentComponent and the Playlist in the editor together via listeners, so the former can update when the latter is modified
    list.addModifiedListener(
      new IPlaylistModifiedListener()
      {
        @Override
        public void playlistModified(Playlist list)
        {
          updateTabTitleForPlaylist(list, tempComp);
          tempComp.setIcon(getIconForPlaylist(list));
          tempComp.setTooltip(list.getFile().getPath());
        }
      }
    );

    // Update the list of open playlists
    _openPlaylists.add(list);

    // update title and status bar if list was modified during loading (due to fix on load option)
    if (list.isModified())
    {
      refreshStatusLabel(list);
      updateTabTitleForPlaylist(list, tempComp);
    }

    if (_documentPane.getDocumentCount() == 1)
    {
      ((java.awt.CardLayout) _playlistPanel.getLayout()).show(_playlistPanel, "_docTabPanel");
    }
  }

  private JDocumentComponent createDocumentComponentForEditor(PlaylistEditCtrl editor, Path path, String title)
  {
    ImageIcon icon;
    icon = getIconForPlaylist(editor.getPlaylist());
    final JDocumentComponent tempComp = new JDocumentComponent(editor, path, title, icon);

    return tempComp;
  }

  private ImageIcon getIconForPlaylist(Playlist list)
  {
    ImageIcon icon;
    int missing = list.getMissingCount();
    if (missing > 0)
    {
      icon = ImageIcons.IMG_MISSING;
    }
    else
    {
      if (list.getFixedCount() > 0 || list.isModified())
      {
        icon = ImageIcons.IMG_FIXED;
      }
      else
      {
        icon = ImageIcons.IMG_FOUND;
      }
    }
    return icon;
  }

  /**
   * @param list
   */
  public void updateCurrentTab(Playlist list)
  {
    PlaylistEditCtrl oldEditor = _documentPane.getActiveDocument().getComponent();

    _documentPane.getActiveDocument().setTitle(list.getFilename());
    oldEditor.setPlaylist(list, true);

    // update playlist history
    PlaylistHistory history = _guiDriver.getHistory();
    try
    {
      history.add(list.getFile().getCanonicalPath());
      history.write();
    }
    catch (IOException ex)
    {
      _logger.warn(ex);
    }

    updateRecentMenu();
  }

  /**
   *
   */
  public void updateRecentMenu()
  {
    recentMenu.removeAll();
    String[] files = _guiDriver.getRecentM3Us();
    if (files.length == 0)
    {
      JMenuItem temp = new JMenuItem("Empty");
      temp.setEnabled(false);
      recentMenu.add(temp);
    }
    else
    {
      for (String file : files)
      {
        JMenuItem temp = new JMenuItem(file);
        temp.addActionListener(this :: recentPlaylistActionPerformed);
        recentMenu.add(temp);
      }
    }
  }

  private void updateAllComponentTreeUIs()
  {
    SwingUtilities.updateComponentTreeUI(this);
    SwingUtilities.updateComponentTreeUI(_jOpenPlaylistFileChooser);
    SwingUtilities.updateComponentTreeUI(_jMediaDirChooser);
    SwingUtilities.updateComponentTreeUI(_jSaveFileChooser);
    SwingUtilities.updateComponentTreeUI(_playlistTreeRightClickMenu);
    SwingUtilities.updateComponentTreeUI(_documentPane);
  }

  private Playlist getPlaylistFromDocumentComponent(JDocumentComponent ctrl)
  {
    return ((PlaylistEditCtrl) ctrl.getComponent()).getPlaylist();
  }

  private void handlePlaylistSave(final Playlist list) throws HeadlessException
  {
    if (list.isNew())
    {
      handleSaveAs(list);
    }
    else
    {
      try
      {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ProgressWorker<Void, String> worker = new ProgressWorker<>()
        {
          @Override
          protected Void doInBackground() throws Exception
          {
            boolean saveRelative = GUIDriver.getInstance().getAppOptions().getSavePlaylistsWithRelativePaths();
            list.save(saveRelative, this);
            return null;
          }
        };
        ProgressDialog pd = new ProgressDialog(this, true, worker, "Saving...", list.getType() == PlaylistType.ITUNES || list.getType() == PlaylistType.XSPF, false);
        pd.setMessage("Please wait while your playlist is saved to disk.");
        pd.setVisible(true);
        worker.get();
      }
      catch (InterruptedException | ExecutionException ex)
      {
        _logger.error("Error saving your playlist", ex);
        JOptionPane.showMessageDialog(this, new JTransparentTextArea("Sorry, there was an error saving your playlist.  Please try again, or file a bug report."));
      }
      finally
      {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    }
  }

  private FileFilter getFileFilterForPlaylist(Playlist list)
  {
    switch (list.getType())
    {
      case ITUNES:
        return new ExtensionFilter("xml", "iTunes Playlist (*.xml)");
      case M3U:
        if (list.isUtfFormat() || list.getFile().getPath().endsWith("8"))
        {
          return new ExtensionFilter("m3u8", "M3U8 Playlist (*.m3u8)");
        }
        else
        {
          return new ExtensionFilter("m3u", "M3U Playlist (*.m3u)");
        }
      case PLS:
        return new ExtensionFilter("pls", "PLS Playlist (*.pls)");
      case WPL:
        return new ExtensionFilter("wpl", "WPL Playlist (*.wpl)");
      case XSPF:
        return new ExtensionFilter("xspf", "XSPF Playlist (*.xspf)");
      case UNKNOWN:
        return new ExtensionFilter("m3u8", "M3U8 Playlist (*.m3u8)");
    }
    return new ExtensionFilter("m3u8", "M3U8 Playlist (*.m3u8)");
  }

  private boolean handleSaveAs(Playlist list)
  {
    _jSaveFileChooser.setSelectedFile(list.getFile());
    _jSaveFileChooser.setFileFilter(getFileFilterForPlaylist(list));
    int rc = _jSaveFileChooser.showSaveDialog(this);
    if (rc == JFileChooser.APPROVE_OPTION)
    {
      try
      {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        File playlist = _jSaveFileChooser.getSelectedFile();

        // prompt for confirmation if the file already exists...
        if (playlist.exists())
        {
          int result = JOptionPane.showConfirmDialog(this, new JTransparentTextArea("You picked a file that already exists, should I really overwrite it?"), "File Exists Warning", JOptionPane.YES_NO_OPTION);
          if (result == JOptionPane.NO_OPTION)
          {
            return false;
          }
        }

        String extension = ((ExtensionFilter) _jSaveFileChooser.getFileFilter()).getExtension();

        if (list.getType() != PlaylistType.ITUNES && extension.equals("xml"))
        {
          JOptionPane.showMessageDialog(this, new JTransparentTextArea("listFix() can only save iTunes playlists as iTunes XML files.  Please save to M3U or M3U8, which can be imported directly into iTunes."));
          return false;
        }

        // make sure file has correct extension
        String normalizedName = playlist.getName().trim().toLowerCase();
        if (!Playlist.isPlaylist(playlist, this.getOptions()) || (!normalizedName.endsWith(extension)))
        {
          if (extension.equals("m3u") && list.isUtfFormat())
          {
            playlist = new File(playlist.getPath() + ".m3u8");
          }
          else
          {
            playlist = new File(playlist.getPath() + "." + extension);
          }
        }

        final File finalPlaylistFile = playlist;
        final Playlist finalList = list;
        ProgressWorker<Void, String> worker = new ProgressWorker<>()
        {
          @Override
          protected Void doInBackground() throws Exception
          {
            finalList.saveAs(finalPlaylistFile, this);
            return null;
          }
        };

        ProgressDialog pd = new ProgressDialog(this, true, worker, "Saving...", list.getType() == PlaylistType.ITUNES || list.getType() == PlaylistType.XSPF, false);
        pd.setMessage("Please wait while your playlist is saved to disk.");
        pd.setVisible(true);

        worker.get();

        updatePlaylistDirectoryPanel();

        // update playlist history
        PlaylistHistory history = _guiDriver.getHistory();
        history.add(list.getFile().getPath());
        try
        {
          history.write();
        }
        catch (IOException e)
        {

        }
        updateRecentMenu();

        String path = list.getFile().getPath();
        try
        {
          path = list.getFile().getCanonicalPath();
        }
        catch (IOException e)
        {

        }

        _documentPane.renameDocument(_documentPane.getActiveDocument().getName(), path);

        return true;
      }
      catch (CancellationException e)
      {
        return false;
      }
      catch (HeadlessException | InterruptedException | ExecutionException e)
      {
        _logger.error("Error saving your playlist", e);
        JOptionPane.showMessageDialog(this, new JTransparentTextArea("Sorry, there was an error saving your playlist.  Please try again, or file a bug report."));
        return false;
      }
      finally
      {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    }
    return false;
  }

  private void updateMenuItemStatuses()
  {
    boolean enable = _documentPane.getDocumentCount() > 0;
    Arrays.stream(componentsRequireActivePlaylist).forEach(c -> c.setEnabled(enable));
  }

  private void currentTabChanged()
  {
    this.currentTabChanged(_documentPane.getActiveDocument());
  }

  private void currentTabChanged(JDocumentComponent documentComponent)
  {
    Playlist list = documentComponent != null ? ((PlaylistEditCtrl) documentComponent.getComponent()).getPlaylist() : null;
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

    refreshStatusLabel(_currentPlaylist);
    if (_currentPlaylist != null)
    {
      _currentPlaylist.addModifiedListener(_playlistListener);
    }
  }

  // Setup the listener for changes to the current playlist.  Essentially turns around and calls onPlaylistModified().
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

  private void onPlaylistModified(Playlist list)
  {
    refreshStatusLabel(list);
  }

  private void refreshStatusLabel(Playlist list)
  {
    if (list != null)
    {
      String fmt = "Currently Open: %s%s     Number of entries in list: %d     Number of lost entries: %d     Number of URLs: %d     Number of open playlists: %d";
      String txt = String.format(fmt, list.getFilename(), list.isModified() ? "*" : "", list.size(), list.getMissingCount(), list.getUrlCount(), _documentPane.getDocumentCount());
      statusLabel.setText(txt);
    }
    else
    {
      statusLabel.setText("No list(s) loaded");
    }
  }

  private void updateTabTitleForPlaylist(Playlist list, JDocumentComponent comp)
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
    comp.setTitle(title);
  }

  /**
   * @return
   */
  public boolean tryCloseAllTabs()
  {
    boolean result = true;
    while (_documentPane.getDocumentCount() > 0 && result)
    {
      result = result && tryCloseTab(_documentPane.getActiveDocument());
    }
    return result;
  }

  /**
   *
   */
  public void runExactMatchOnAllTabs()
  {
    JDocumentComponent firstComp = _documentPane.getActiveDocument();
    JDocumentComponent comp = _documentPane.getActiveDocument();
    do
    {
      PlaylistEditCtrl ctrl = (PlaylistEditCtrl) comp.getComponent();
      if (ctrl != null)
      {
        _documentPane.setActiveDocument(comp.getName());
        ctrl.locateMissingFiles();
      }
      _documentPane.nextDocument();
      comp = _documentPane.getActiveDocument();
    }
    while (!firstComp.equals(comp));
  }

  /**
   *
   */
  public void runClosestMatchOnAllTabs()
  {
    JDocumentComponent firstComp = _documentPane.getActiveDocument();
    JDocumentComponent comp = _documentPane.getActiveDocument();
    do
    {
      PlaylistEditCtrl ctrl = (PlaylistEditCtrl) comp.getComponent();
      if (ctrl != null)
      {
        _documentPane.setActiveDocument(comp.getName());
        ctrl.locateMissingFiles();
        ctrl.bulkFindClosestMatches();
      }
      _documentPane.nextDocument();
      comp = _documentPane.getActiveDocument();
    }
    while (!firstComp.equals(comp));
  }

  private void reloadAllTabs()
  {
    JDocumentComponent firstComp = _documentPane.getActiveDocument();
    JDocumentComponent comp = _documentPane.getActiveDocument();
    do
    {
      PlaylistEditCtrl ctrl = (PlaylistEditCtrl) comp.getComponent();
      if (ctrl != null)
      {
        _documentPane.setActiveDocument(comp.getName());
        ctrl.reloadPlaylist();
      }
      _documentPane.nextDocument();
      comp = _documentPane.getActiveDocument();
    }
    while (!firstComp.equals(comp));
  }

  /**
   * @param ctrl
   * @return
   */
  public boolean tryCloseTab(JDocumentComponent ctrl)
  {
    final Playlist playlist = getPlaylistFromDocumentComponent(ctrl);
    if (playlist.isModified())
    {
      Object[] options =
        {
          "Save", "Save As", "Don't Save", "Cancel"
        };
      int rc = JOptionPane.showOptionDialog(this, new JTransparentTextArea("The playlist \"" + playlist.getFilename() + "\" has been modified. Do you want to save the changes?"), "Confirm Close",
        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);

      if (rc == 0)
      {
        ProgressWorker<Boolean, String> worker = new ProgressWorker<>()
        {
          @Override
          protected Boolean doInBackground() throws Exception
          {
            boolean saveRelative = GUIDriver.getInstance().getAppOptions().getSavePlaylistsWithRelativePaths();
            playlist.save(saveRelative, this);
            return true;
          }
        };
        ProgressDialog pd = new ProgressDialog(this, true, worker, "Saving...", false, false);
        pd.setVisible(true);

        try
        {
          boolean savedOk = worker.get();
          if (savedOk)
          {
            cleanupOnTabClose(playlist);
            return true;
          }
        }
        catch (InterruptedException ex)
        {
        }
        catch (ExecutionException ex)
        {
          _logger.error("Save Error", ex);
          JOptionPane.showMessageDialog(GUIScreen.this, ex.getCause(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }

        return false;
      }
      else if (rc == 1)
      {
        return handleSaveAs(playlist);
      }
      else if (rc == 2)
      {
        cleanupOnTabClose(playlist);
        return true;
      }
      else
      {
        return false;
      }
    }
    else
    {
      return true;
    }
  }

  private void cleanupOnTabClose(Playlist list)
  {
    _openPlaylists.remove(list);
  }

  private void confirmCloseApp()
  {
    for (Playlist list : _openPlaylists)
    {
      if (list.isModified())
      {
        Object[] options =
          {
            "Discard Changes and Exit", "Cancel"
          };
        int rc = JOptionPane.showOptionDialog(this, new JTransparentTextArea("You have unsaved changes. Do you really want to discard these changes and exit?"), "Confirm Close",
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

    WindowSaver.getInstance().saveSettings();
    System.exit(0);
  }

  private void _addMediaDirButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__addMediaDirButtonActionPerformed
  {//GEN-HEADEREND:event__addMediaDirButtonActionPerformed
    int response = _jMediaDirChooser.showOpenDialog(this);
    if (response == JFileChooser.APPROVE_OPTION)
    {
      try
      {
        UNCFile mediaDir = new UNCFile(_jMediaDirChooser.getSelectedFile());
        if (getApplicationConfig().getAlwaysUseUNCPaths())
        {
          if (mediaDir.onNetworkDrive())
          {
            mediaDir = new UNCFile(mediaDir.getUNCPath());
          }
        }
        final String dir = mediaDir.getPath();

        // first let's see if this is a subdirectory of any of the media directories already in the list, and error out if so...
        if (ArrayFunctions.containsStringPrefixingAnotherString(_guiDriver.getMediaLibrary().getDirectories(), dir, !GUIDriver.FILE_SYSTEM_IS_CASE_SENSITIVE))
        {
          JOptionPane.showMessageDialog(this, new JTransparentTextArea("The directory you attempted to add is a subdirectory of one already in your media library, no change was made."),
            "Reminder", JOptionPane.INFORMATION_MESSAGE);
          return;
        }
        else
        {
          // Now check if any of the media directories is a subdirectory of the one we're adding and remove the media directory if so.
          int matchCount = 0;
          for (String dirToCheck : _guiDriver.getMediaLibrary().getDirectories())
          {
            if (dirToCheck.startsWith(dir))
            {
              // Only showing the message the first time we find this condition...
              if (matchCount == 0)
              {
                JOptionPane.showMessageDialog(this,
                  new JTransparentTextArea("One or more of your existing media directories is a subdirectory of the directory you just added.  These directories will be removed from your list automatically."),
                  "Reminder", JOptionPane.INFORMATION_MESSAGE);
              }
              removeMediaDir(dirToCheck);
              matchCount++;
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
        ProgressDialog pd = new ProgressDialog(this, true, worker, "Updating Media Library...", true, true);
        pd.setVisible(true);

        try
        {
          worker.get();
          _lstMediaLibraryDirs.setListData(_guiDriver.getMediaLibrary().getDirectories().toArray());
        }
        catch (InterruptedException | CancellationException ex)
        {
        }
        catch (ExecutionException ex)
        {
          _logger.error(ex);
        }
      }
      catch (HeadlessException e)
      {
        JOptionPane.showMessageDialog(this, new JTransparentTextArea("An error has occurred, media directory could not be added."));
        _logger.error("Error adding media directory", e);
      }
    }
    else
    {
      _jMediaDirChooser.cancelSelection();
    }
    updateMediaDirButtons();
  }//GEN-LAST:event__addMediaDirButtonActionPerformed

  private void _removeMediaDirButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__removeMediaDirButtonActionPerformed
  {//GEN-HEADEREND:event__removeMediaDirButtonActionPerformed
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    try
    {
      String selection = (String) _lstMediaLibraryDirs.getSelectedValue();
      if (selection != null)
      {
        if (!selection.equals("Please Add A Media Directory..."))
        {
          _guiDriver.getMediaLibrarConfiguration().removeMediaDir(selection);
          _lstMediaLibraryDirs.setListData(_guiDriver.getMediaLibrary().getDirectories().toArray());
        }
      }
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    catch (MediaDirNotFoundException e)
    {
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(this, new JTransparentTextArea("An error has occured, files in the media directory you removed may not have been completely removed from the library.  Please refresh the library."));
      _logger.warn(e);
    }
    updateMediaDirButtons();
  }//GEN-LAST:event__removeMediaDirButtonActionPerformed

  private void removeMediaDir(String mediaDirectory)
  {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    try
    {
      _guiDriver.getMediaLibrarConfiguration().removeMediaDir(mediaDirectory);
      _lstMediaLibraryDirs.setListData(_guiDriver.getMediaLibrary().getDirectories().toArray());
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    catch (MediaDirNotFoundException e)
    {
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      JOptionPane.showMessageDialog(this, new JTransparentTextArea("An error has occured, files in the media directory you removed may not have been completely removed from the library.  Please refresh the library."));
      _logger.warn(e);
    }
    updateMediaDirButtons();
  }

  private void _aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt)
  {
    JOptionPane.showMessageDialog(this, "listFix( ) v" + applicationVersion + "\n\nBrought To You By: " +
        "\n          Borewit" +
        "\n          Jeremy Caron (firewyre) " +
        "\n          Kennedy Akala (kennedyakala)" +
        "\n          John Peterson (johnpeterson)" +
        "\n\nProject home: https://github.com/Borewit/listFix",
      "About", JOptionPane.INFORMATION_MESSAGE);
  }

  private void _exitMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__exitMenuItemActionPerformed
  {//GEN-HEADEREND:event__exitMenuItemActionPerformed
    confirmCloseApp();
  }//GEN-LAST:event__exitMenuItemActionPerformed

  /**
   * Exit the Application
   */
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
    ProgressDialog pd = new ProgressDialog(this, true, worker, "Updating Media Library...", true, true);
    pd.setVisible(true);

    try
    {
      worker.get();
      _lstMediaLibraryDirs.setListData(_guiDriver.getMediaLibrary().getDirectories().toArray());
    }
    catch (InterruptedException | CancellationException ex)
    {
    }
    catch (ExecutionException ex)
    {
      _logger.error("Error refresh media directories", ex);
    }
  }

  private void _appOptionsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__appOptionsMenuItemActionPerformed
  {//GEN-HEADEREND:event__appOptionsMenuItemActionPerformed
    fireOptionsPopup();
  }//GEN-LAST:event__appOptionsMenuItemActionPerformed

  private void _miExactMatchesSearchActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miExactMatchesSearchActionPerformed
  {//GEN-HEADEREND:event__miExactMatchesSearchActionPerformed
    runExactMatchesSearchOnSelectedPlaylists();
  }//GEN-LAST:event__miExactMatchesSearchActionPerformed

  private void _helpMenuItemActionPerformed(java.awt.event.ActionEvent evt)
  {//GEN-FIRST:event__helpMenuItemActionPerformed
    BrowserLauncher.launch("https://github.com/Borewit/listFix");
  }//GEN-LAST:event__helpMenuItemActionPerformed

  private void _updateCheckMenuItemActionPerformed(java.awt.event.ActionEvent evt)
  {//GEN-FIRST:event__updateCheckMenuItemActionPerformed
    BrowserLauncher.launch("https://github.com/Borewit/listFix");
  }//GEN-LAST:event__updateCheckMenuItemActionPerformed

  private void _batchRepairWinampMenuItemActionPerformed(java.awt.event.ActionEvent evt)
  {//GEN-FIRST:event__batchRepairWinampMenuItemActionPerformed
    final BatchRepair br = WinampHelper.getWinampBatchRepair(_guiDriver.getMediaLibrary(), this.getOptions());
    if (br == null || br.isEmpty())
    {
      JOptionPane.showMessageDialog(this, new JTransparentTextArea("Could not find any WinAmp Media Library playlists"));
      return;
    }

    BatchExactMatchesResultsDialog dlg = new BatchExactMatchesResultsDialog(this, true, br, this.getOptions());
    if (!dlg.getUserCancelled())
    {
      if (br.isEmpty())
      {
        JOptionPane.showMessageDialog(this, new JTransparentTextArea("There was nothing to fix in the list(s) that were processed."));
      }
      else
      {
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
      }
    }
  }//GEN-LAST:event__batchRepairWinampMenuItemActionPerformed

  private void onMenuBatchRepairActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onMenuBatchRepairActionPerformed
  {//GEN-HEADEREND:event_onMenuBatchRepairActionPerformed
    JFileChooser dlg = new JFileChooser();
    dlg.setDialogTitle("Select Playlists and/or Directories");
    dlg.setAcceptAllFileFilterUsed(false);
    dlg.addChoosableFileFilter(new PlaylistFileFilter());
    dlg.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    dlg.setMultiSelectionEnabled(true);
    if (dlg.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
    {
      // build complete list of playlists
      List<File> files = new ArrayList<>();
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
      BatchRepair br = new BatchRepair(_guiDriver.getMediaLibrary(), rootDir);
      br.setDescription("Exact Matches Search");
      for (File file : files)
      {
        br.add(new BatchRepairItem(file, this.getOptions()));
      }

      BatchExactMatchesResultsDialog repairDlg = new BatchExactMatchesResultsDialog(this, true, br, this.getOptions());
      if (!repairDlg.getUserCancelled())
      {
        if (br.isEmpty())
        {
          JOptionPane.showMessageDialog(this, new JTransparentTextArea("There was nothing to fix in the list(s) that were processed."));
        }
        else
        {
          repairDlg.setLocationRelativeTo(this);
          repairDlg.setVisible(true);
          updatePlaylistDirectoryPanel();
        }
      }
    }
  }//GEN-LAST:event_onMenuBatchRepairActionPerformed

  private void _openIconButtonActionPerformed1(java.awt.event.ActionEvent evt)//GEN-FIRST:event__openIconButtonActionPerformed1
  {//GEN-HEADEREND:event__openIconButtonActionPerformed1
    if (_currentPlaylist != null)
    {
      _jOpenPlaylistFileChooser.setSelectedFile(_currentPlaylist.getFile());
    }
    int response = _jOpenPlaylistFileChooser.showOpenDialog(this);
    if (response == JFileChooser.APPROVE_OPTION)
    {
      File[] playlists = _jOpenPlaylistFileChooser.getSelectedFiles();
      for (File file : playlists)
      {
        this.openPlaylist(file);
      }
    }
    else
    {
      _jOpenPlaylistFileChooser.cancelSelection();
    }
  }//GEN-LAST:event__openIconButtonActionPerformed1

  private void _saveMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__saveMenuItemActionPerformed
  {//GEN-HEADEREND:event__saveMenuItemActionPerformed
    if (_currentPlaylist == null)
    {
      return;
    }

    handlePlaylistSave(_currentPlaylist);
  }//GEN-LAST:event__saveMenuItemActionPerformed

  private void _newIconButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__newIconButtonActionPerformed
  {//GEN-HEADEREND:event__newIconButtonActionPerformed
    try
    {
      _currentPlaylist = new Playlist(this.getOptions());
      Path path = _currentPlaylist.getFile().toPath();
      PlaylistEditCtrl editor = new PlaylistEditCtrl(this);
      editor.setPlaylist(_currentPlaylist);
      String title = _currentPlaylist.getFilename();
      final JDocumentComponent tempComp = createDocumentComponentForEditor(editor, path, title);
      _documentPane.openDocument(tempComp);
      _documentPane.setActiveDocument(tempComp.getName());

      _openPlaylists.add(_currentPlaylist);

      refreshStatusLabel(_currentPlaylist);
      _currentPlaylist.addModifiedListener(_playlistListener);

      _currentPlaylist.addModifiedListener(new IPlaylistModifiedListener()
      {
        @Override
        public void playlistModified(Playlist list)
        {
          updateTabTitleForPlaylist(list, tempComp);
          tempComp.setIcon(getIconForPlaylist(list));
          tempComp.setTooltip(list.getFile().getPath());
        }
      });

      if (_documentPane.getDocumentCount() == 1)
      {
        ((java.awt.CardLayout) _playlistPanel.getLayout()).show(_playlistPanel, "_docTabPanel");
      }
    }
    catch (IOException ex)
    {
      _logger.error("Error creating a new playlist", ex);
      JOptionPane.showMessageDialog(this,
        new JTransparentTextArea(ExStack.textFormatErrorForUser("Sorry, there was an error creating a new playlist.  Please try again, or file a bug report.", ex.getCause())),
        "New Playlist Error",
        JOptionPane.ERROR_MESSAGE);
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
          catch (JAXBException | IOException ex)
          {
            JOptionPane.showMessageDialog(GUIScreen.this, new JTransparentTextArea("Sorry, there was a problem extracting your playlists.  The error was: " + ex.getMessage()), "Extraction Error", JOptionPane.ERROR_MESSAGE);
            _logger.warn(ex);
          }
          finally
          {
            return null;
          }
        }
      };
      ProgressDialog pd = new ProgressDialog(this, true, worker, "Extracting...", false, true);
      pd.setVisible(true);
    }
  }//GEN-LAST:event__extractPlaylistsMenuItemActionPerformed

  private void _closeMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__closeMenuItemActionPerformed
  {//GEN-HEADEREND:event__closeMenuItemActionPerformed
    if (_documentPane.getDocumentCount() > 0)
    {
      _documentPane.closeActiveDocument();
    }
  }//GEN-LAST:event__closeMenuItemActionPerformed

  private void _btnRefreshActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__btnRefreshActionPerformed
  {//GEN-HEADEREND:event__btnRefreshActionPerformed
    updatePlaylistDirectoryPanel();
  }//GEN-LAST:event__btnRefreshActionPerformed

  private void _btnOpenSelectedActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__btnOpenSelectedActionPerformed
  {//GEN-HEADEREND:event__btnOpenSelectedActionPerformed
    openTreeSelectedPlaylists();
  }//GEN-LAST:event__btnOpenSelectedActionPerformed

  private void _miOpenSelectedPlaylistsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miOpenSelectedPlaylistsActionPerformed
  {//GEN-HEADEREND:event__miOpenSelectedPlaylistsActionPerformed
    openTreeSelectedPlaylists();
  }//GEN-LAST:event__miOpenSelectedPlaylistsActionPerformed

  private void _btnSetPlaylistsDirActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__btnSetPlaylistsDirActionPerformed
  {//GEN-HEADEREND:event__btnSetPlaylistsDirActionPerformed
    fireOptionsPopup();
  }//GEN-LAST:event__btnSetPlaylistsDirActionPerformed

  private void _btnQuickRepairActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__btnQuickRepairActionPerformed
  {//GEN-HEADEREND:event__btnQuickRepairActionPerformed
    runExactMatchesSearchOnSelectedPlaylists();
  }//GEN-LAST:event__btnQuickRepairActionPerformed

  private void _closeAllMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__closeAllMenuItemActionPerformed
  {//GEN-HEADEREND:event__closeAllMenuItemActionPerformed
    _documentPane.closeAll();
  }//GEN-LAST:event__closeAllMenuItemActionPerformed

  private void _miRefreshDirectoryTreeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miRefreshDirectoryTreeActionPerformed
  {//GEN-HEADEREND:event__miRefreshDirectoryTreeActionPerformed
    updatePlaylistDirectoryPanel();
  }//GEN-LAST:event__miRefreshDirectoryTreeActionPerformed

  private void _btnDeepRepairActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__btnDeepRepairActionPerformed
  {//GEN-HEADEREND:event__btnDeepRepairActionPerformed
    runClosestMatchesSearchOnSelectedLists();
  }//GEN-LAST:event__btnDeepRepairActionPerformed

  private void _miClosestMatchesSearchActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miClosestMatchesSearchActionPerformed
  {//GEN-HEADEREND:event__miClosestMatchesSearchActionPerformed
    runClosestMatchesSearchOnSelectedLists();
  }//GEN-LAST:event__miClosestMatchesSearchActionPerformed

  private void _miReloadActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miReloadActionPerformed
  {//GEN-HEADEREND:event__miReloadActionPerformed
    if (_documentPane.getActiveDocument() != null)
    {
      ((PlaylistEditCtrl) _documentPane.getActiveDocument().getComponent()).reloadPlaylist();
    }
  }//GEN-LAST:event__miReloadActionPerformed

  private void formComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentResized
  {//GEN-HEADEREND:event_formComponentResized
    // Set the position of the divider in the left split pane.
    _leftSplitPane.setDividerLocation(.7);
  }//GEN-LAST:event_formComponentResized

  private void _miDeletePlaylistActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miDeletePlaylistActionPerformed
  {//GEN-HEADEREND:event__miDeletePlaylistActionPerformed
    deleteTreeSelectedPlaylists();
  }//GEN-LAST:event__miDeletePlaylistActionPerformed

  private void _miRenameSelectedItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miRenameSelectedItemActionPerformed
  {//GEN-HEADEREND:event__miRenameSelectedItemActionPerformed
    renameTreeSelectedNode();
  }//GEN-LAST:event__miRenameSelectedItemActionPerformed

  private void _playlistDirectoryTreeKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event__playlistDirectoryTreeKeyPressed
  {//GEN-HEADEREND:event__playlistDirectoryTreeKeyPressed
    switch(evt.getKeyCode()) {
      case KeyEvent.VK_ENTER:
        this._btnOpenSelectedActionPerformed(null);
        break;
      case keyEventRenameFile:
        this.renameTreeSelectedNode();
        break;

    }
  }//GEN-LAST:event__playlistDirectoryTreeKeyPressed

  private void _miExactMatchRepairOpenPlaylistsonMenuBatchRepairActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miExactMatchRepairOpenPlaylistsonMenuBatchRepairActionPerformed
  {//GEN-HEADEREND:event__miExactMatchRepairOpenPlaylistsonMenuBatchRepairActionPerformed
    runExactMatchOnAllTabs();
  }//GEN-LAST:event__miExactMatchRepairOpenPlaylistsonMenuBatchRepairActionPerformed

  private void _saveAllMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__saveAllMenuItemActionPerformed
  {//GEN-HEADEREND:event__saveAllMenuItemActionPerformed
    for (int i = 0; i < _documentPane.getDocumentCount(); i++)
    {
      Playlist list = getPlaylistFromDocumentComponent(_documentPane.getDocumentAt(i));
      handlePlaylistSave(list);
    }
  }//GEN-LAST:event__saveAllMenuItemActionPerformed

  private void _miClosestMatchRepairOpenPlaylistsonMenuBatchRepairActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miClosestMatchRepairOpenPlaylistsonMenuBatchRepairActionPerformed
  {//GEN-HEADEREND:event__miClosestMatchRepairOpenPlaylistsonMenuBatchRepairActionPerformed
    runClosestMatchOnAllTabs();
  }//GEN-LAST:event__miClosestMatchRepairOpenPlaylistsonMenuBatchRepairActionPerformed

  private void _miReloadAllActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miReloadAllActionPerformed
  {//GEN-HEADEREND:event__miReloadAllActionPerformed
    reloadAllTabs();
  }//GEN-LAST:event__miReloadAllActionPerformed

  /**
   * @param font
   */
  public void setApplicationFont(Font font)
  {
    Enumeration enumer = UIManager.getDefaults().keys();
    while (enumer.hasMoreElements())
    {
      Object key = enumer.nextElement();
      Object value = UIManager.get(key);
      if (value instanceof Font || value instanceof FontUIResource)
      {
        UIManager.put(key, new javax.swing.plaf.FontUIResource(font));
      }
    }

    UIManager.put("OptionPane.buttonFont", new javax.swing.plaf.FontUIResource(font));
    updateAllComponentTreeUIs();
  }

  /**
   * @param font
   */
  public static void InitApplicationFont(Font font)
  {
    Enumeration enumer = UIManager.getDefaults().keys();
    while (enumer.hasMoreElements())
    {
      Object key = enumer.nextElement();
      Object value = UIManager.get(key);
      if (value instanceof Font || value instanceof FontUIResource)
      {
        UIManager.put(key, new javax.swing.plaf.FontUIResource(font));
      }
    }

    UIManager.put("OptionPane.messsageFont", font);
    UIManager.put("OptionPane.buttonFont", font);
  }

  private void updateMediaDirButtons()
  {
    if (_lstMediaLibraryDirs.getModel().getSize() == 0)
    {
      _removeMediaDirButton.setEnabled(false);
      _refreshMediaDirsButton.setEnabled(false);
    }
    else if (_lstMediaLibraryDirs.getModel().getSize() != 0)
    {
      _removeMediaDirButton.setEnabled(true);
      _refreshMediaDirsButton.setEnabled(true);
    }
  }

  private void updatePlaylistDirectoryPanel()
  {
    if (_playlistDirectoryTree.getModel().getRoot() != null)
    {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      Enumeration treeStateEnum = saveExpansionState(_playlistDirectoryTree);
      ((DefaultTreeModel) _playlistDirectoryTree.getModel()).setRoot(FileTreeNodeGenerator.addNodes(null, new File(this.getOptions().getPlaylistsDirectory())));
      addPlaylistPanelModelListener();
      loadExpansionState(_playlistDirectoryTree, treeStateEnum);
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }

  /**
   * Save the expansion state of a tree.
   *
   * @param tree
   * @return expanded tree path as Enumeration
   */
  private Enumeration saveExpansionState(JTree tree)
  {
    return tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
  }

  /**
   * Restore the expansion state of a JTree.
   *
   * @param tree
   * @param enumeration an Enumeration of expansion state. You can get it using {@link #saveExpansionState(javax.swing.JTree)}.
   */
  private void loadExpansionState(JTree tree, Enumeration enumeration)
  {
    if (enumeration != null)
    {
      while (enumeration.hasMoreElements())
      {
        TreePath treePath = (TreePath) enumeration.nextElement();
        // tree.
        tree.expandPath(treePath);
      }
    }
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
      String realClassName = className;
      if (className.equalsIgnoreCase("com.jgoodies.looks.plastic.theme.DarkStar"))
      {
        PlasticLookAndFeel.setPlasticTheme(new DarkStar());
        realClassName = "com.jgoodies.looks.plastic.PlasticLookAndFeel";
      }
      else if (className.equals("com.jgoodies.looks.plastic.theme.SkyBlue"))
      {
        PlasticLookAndFeel.setPlasticTheme(new SkyBlue());
        realClassName = "com.jgoodies.looks.plastic.PlasticLookAndFeel";
      }
      else if (className.equalsIgnoreCase("com.jgoodies.looks.plastic.PlasticLookAndFeel"))
      {
        PlasticLookAndFeel.setPlasticTheme(new LightGray());
      }
      else if (className.equalsIgnoreCase("com.jgoodies.looks.plastic.Plastic3DLookAndFeel"))
      {
        PlasticLookAndFeel.setPlasticTheme(new LightGray());
      }
      else if (className.equalsIgnoreCase("com.jgoodies.looks.plastic.PlasticXPLookAndFeel"))
      {
        PlasticLookAndFeel.setPlasticTheme(new LightGray());
      }

      UIManager.setLookAndFeel(realClassName);
      updateAllComponentTreeUIs();
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
           UnsupportedLookAndFeelException ex)
    {
      _logger.error("Error while changing look & feel", ex);
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws IOException, InterruptedException, InvocationTargetException
  {
    _logger.info(String.format("Starting ListFix() version \"%s\"...", applicationVersion));

    // EDT Exception
    SwingUtilities.invokeAndWait(() -> {
      // We are in the event dispatching thread
      Thread.currentThread().setUncaughtExceptionHandler((thread, e) -> {
        _logger.error("Uncaught Exception", e);
      });
    });

    IAppOptions tempOptions = ApplicationOptionsConfiguration.load().getConfig();
    InitApplicationFont(tempOptions.getAppFont());
    GUIScreen mainWindow = new GUIScreen();

    if (mainWindow.getLocation().equals(new Point(0, 0)))
    {
      DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
      java.awt.Dimension labelSize = mainWindow.getPreferredSize();
      mainWindow.setLocation(dm.getWidth() / 2 - (labelSize.width / 2), dm.getHeight() / 2 - (labelSize.height / 2));
    }
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
      catch (Exception ex)
      {
        _logger.error("Error opening playlists from command line", ex);
      }
    }
  }

  // <editor-fold defaultstate="collapsed" desc="Generated Code">
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenuItem _aboutMenuItem;
  private javax.swing.JButton _addMediaDirButton;
  private javax.swing.JMenuItem _appOptionsMenuItem;
  private javax.swing.JMenuItem _batchRepairWinampMenuItem;
  private javax.swing.JButton _btnDeepRepair;
  private javax.swing.JButton _btnOpenSelected;
  private javax.swing.JButton _btnQuickRepair;
  private javax.swing.JButton _btnRefresh;
  private javax.swing.JButton _btnSetPlaylistsDir;
  private javax.swing.JMenuItem _clearHistoryMenuItem;
  private javax.swing.JMenuItem _closeAllMenuItem;
  private javax.swing.JMenuItem _closeMenuItem;
  private javax.swing.JPanel _docTabPanel;
  private JDocumentTabbedPane _documentPane;
  private javax.swing.JMenuItem _exitMenuItem;
  private javax.swing.JMenuItem _extractPlaylistsMenuItem;
  private javax.swing.JMenu _fileMenu;
  private javax.swing.JPanel _gettingStartedPanel;
  private javax.swing.JMenu _helpMenu;
  private javax.swing.JMenuItem _helpMenuItem;
  private javax.swing.JSplitPane _leftSplitPane;
  private javax.swing.JMenuItem _loadMenuItem;
  private javax.swing.JMenuItem _openPlaylistLocationMenuItem;
  private javax.swing.JList _lstMediaLibraryDirs;
  private javax.swing.JMenuBar _mainMenuBar;
  private javax.swing.JPanel _mediaLibraryButtonPanel;
  private javax.swing.JPanel _mediaLibraryPanel;
  private javax.swing.JScrollPane _mediaLibraryScrollPane;
  private javax.swing.JMenuItem _miBatchRepair;
  private javax.swing.JMenuItem _miClosestMatchRepairOpenPlaylists;
  private javax.swing.JMenuItem _miClosestMatchesSearch;
  private javax.swing.JMenuItem _miDeletePlaylist;
  private javax.swing.JMenuItem _miExactMatchRepairOpenPlaylists;
  private javax.swing.JMenuItem _miExactMatchesSearch;
  private javax.swing.JMenuItem _miOpenSelectedPlaylists;
  private javax.swing.JMenuItem _miOpenSelectedPlaylistLocation;
  private javax.swing.JMenuItem _miRefreshDirectoryTree;
  private javax.swing.JMenuItem _miReload;
  private javax.swing.JMenuItem _miReloadAll;
  private javax.swing.JMenuItem _miRenameSelectedItem;
  private javax.swing.JButton _newIconButton;
  private javax.swing.JMenuItem _newPlaylistMenuItem;
  private javax.swing.JButton _openIconButton;
  private javax.swing.JPanel _playlistDirectoryPanel;
  private javax.swing.JTree _playlistDirectoryTree;
  private javax.swing.JPanel _playlistPanel;
  private javax.swing.JPopupMenu _playlistTreeRightClickMenu;
  private javax.swing.JPanel _playlistsDirectoryButtonPanel;
  private javax.swing.JButton _refreshMediaDirsButton;
  private javax.swing.JButton _removeMediaDirButton;
  private javax.swing.JMenu _repairMenu;
  private javax.swing.JMenuItem _saveAllMenuItem;
  private javax.swing.JMenuItem _saveAsMenuItem;
  private javax.swing.JMenuItem _saveMenuItem;
  private javax.swing.JPanel _spacerPanel;
  private javax.swing.JSplitPane _splitPane;
  private javax.swing.JPanel _statusPanel;
  private javax.swing.JScrollPane _treeScrollPane;
  private javax.swing.JMenuItem _updateCheckMenuItem;
  private javax.swing.JPanel _verticalPanel;
  private javax.swing.JPopupMenu.Separator jSeparator1;
  private javax.swing.JPopupMenu.Separator jSeparator2;
  private javax.swing.JPopupMenu.Separator jSeparator3;
  private javax.swing.JPopupMenu.Separator jSeparator4;
  private javax.swing.JPopupMenu.Separator jSeparator5;
  private javax.swing.JMenu recentMenu;
  private javax.swing.JLabel statusLabel;
  // End of variables declaration//GEN-END:variables
  // </editor-fold>
}
