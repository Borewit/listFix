package listfix.view;

import io.github.borewit.lizzy.playlist.PlaylistFormat;
import listfix.config.*;
import listfix.controller.ListFixController;
import listfix.controller.MediaLibraryOperator;
import listfix.exceptions.MediaDirNotFoundException;
import listfix.io.*;
import listfix.io.filters.AllPlaylistFileFilter;
import listfix.io.filters.SpecificPlaylistFileFilter;
import listfix.io.playlists.LizzyPlaylistUtil;
import listfix.json.JsonAppOptions;
import listfix.model.PlaylistHistory;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistFactory;
import listfix.model.playlists.PlaylistProviderNotFoundException;
import listfix.swing.IDocumentChangeListener;
import listfix.swing.JPlaylistComponent;
import listfix.swing.JDocumentTabbedPane;
import listfix.util.ArrayFunctions;
import listfix.util.ExStack;
import listfix.util.FileTypeSearch;
import listfix.view.controls.JTransparentTextArea;
import listfix.view.controls.PlaylistEditCtrl;
import listfix.view.dialogs.*;
import listfix.view.support.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.JPopupMenu.Separator;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public final class GUIScreen extends JFrame implements IListFixGui
{
  private final JFileChooser _jOpenPlaylistFileChooser = new JFileChooser();
  private final JFileChooser _savePlaylistAsFileChooser = new JFileChooser();
  private final FolderChooser _jMediaDirChooser = new FolderChooser();
  private final Image applicationIcon = getImageIcon("icon.png").getImage();
  private final listfix.view.support.SplashScreen splashScreen = new listfix.view.support.SplashScreen(getImageIcon("listfixSplashScreen.png"));
  private ListFixController _listFixController = null;
  private Playlist _currentPlaylist;
  private IPlaylistModifiedListener _playlistListener;

  private static final Logger _logger = LogManager.getLogger(GUIScreen.class);

  private static final String applicationVersion = getBuildNumber();

  private static final String DefaultPlaylistFormatExtension = ".m3u8";

  /**
   * The components should only be enabled when 1 or more playlists are loaded
   */
  private Component[] componentsRequireActivePlaylist;

  private PlaylistTransferHandler playlistTransferHandler;

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

  public static String getBuildNumber() {
    URL url = getResourceUrl("/META-INF/MANIFEST.MF");
    try
    {
      Manifest manifest = new Manifest(url.openStream());
      Attributes mainAttributes = manifest.getMainAttributes();
      return mainAttributes.getValue("Implementation-Version");
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  /**
   * Show the splash screen with initial text, load the media library & option files into memory, then prepare to init the UI
   */
  private void preInitComponents()
  {
    splashScreen.setVisible(true);
    splashScreen.setStatusBar("Loading Media Library & Options...");
    _listFixController = ListFixController.getInstance();
    splashScreen.setStatusBar("Initializing UI...");
    this.playlistTransferHandler = new PlaylistTransferHandler(this);
  }

  private IAppOptions getApplicationConfig()
  {
    return this._listFixController.getApplicationConfiguration().getConfig();
  }

  /**
   * All UI components have been instantiated at this point.
   */
  private void postInitComponents()
  {
    // Set the user-selected font and look & feel
    final IAppOptions appConfig = this.getApplicationConfig();
    setApplicationFont(appConfig.getAppFont());

    if (appConfig.getLookAndFeel() == null || appConfig.getLookAndFeel().isEmpty())
    {
      appConfig.setLookAndFeel(
        Arrays.stream(UIManager.getInstalledLookAndFeels())
          .filter(laf -> laf.getName().equals("Nimbus"))
          .map(UIManager.LookAndFeelInfo::getClassName)
          .findFirst()
          .orElse(UIManager.getSystemLookAndFeelClassName()
          )
      );
      _logger.info("Initialize default look & feel to " + appConfig.getLookAndFeel());
    }

    this.setLookAndFeel(appConfig.getLookAndFeel());

    configureFileAndFolderChoosers();

    _playlistTabbedPane.setTransferHandler(this.playlistTransferHandler);

    // Warn the user if no media directories have been defined, and set the
    if (this._listFixController.getShowMediaDirWindow())
    {
      JOptionPane.showMessageDialog(
        this,
        new JTransparentTextArea("You need to add a media directory before you can find the new locations of your files.  See help for more information."),
        "Reminder",
        JOptionPane.INFORMATION_MESSAGE);
    }
    else
    {
      _lstMediaLibraryDirs.setListData(new Vector<>(_listFixController.getMediaLibrary().getMediaDirectories()));
    }

    updateMediaDirButtons();
    updateRecentMenu();
    onPlaylistModified(null);

    ((CardLayout) _playlistPanel.getLayout()).show(_playlistPanel, "_gettingStartedPanel");

    initPlaylistListener();

    // drag-n-drop support for the playlist directory tree
    _playlistDirectoryTree.setTransferHandler(new FileListTransferHandler()
    {
      @Override
      public boolean handleFileList(List<File> fileList)
      {
        return fileList.stream()
          .filter(File::isDirectory)
          .map(folder -> {
            GUIScreen.this.addPlaylistFolder(folder);
            return true;
          })
          .reduce(false, (t, v) -> true);
      }
    });
    _playlistDirectoryTree.setRootVisible(false);
    // Show tooltips
    ToolTipManager.sharedInstance().registerComponent(_playlistDirectoryTree);
    _playlistDirectoryTree.setCellRenderer(new TreeTooltipRenderer());

    _playlistDirectoryTree.getSelectionModel().addTreeSelectionListener(e -> {
      boolean hasSelected = _playlistDirectoryTree.getSelectionCount() > 0;
      _btnOpenSelected.setEnabled(hasSelected);
    });

    // addAt popup menu to playlist tree on right-click
    _playlistDirectoryTree.addMouseListener(createPlaylistTreeMouseListener());

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

    // Load the position the window was in when it was last closed.
    WindowSaver windowSaver = new WindowSaver(this._listFixController.getApplicationState());
    windowSaver.loadSettings(this);

    // Set the position of the divider in the left split pane.
    _leftSplitPane.setDividerLocation(.7);

    updateMenuItemStatuses();

    // JCaron - 2012.05.03 - Global listener for Ctrl-Tab and Ctrl-Shift-Tab
    configureGlobalKeyboardListener();

    // Stop showing the loading screen
    splashScreen.setVisible(false);

    _logger.info("post init");

    EventQueue.invokeLater(() -> {
      IApplicationState appState = _listFixController.getApplicationConfiguration().getConfig().getApplicationState();
      // Restore previous opened playlists
      appState.getPlaylistsOpened().stream()
        .map(Path::of)
        .filter(Files::exists)
        .forEach(GUIScreen.this::openPlaylist);

      final Integer playlistIndex = appState.getActivePlaylistIndex();
      if (playlistIndex != null && playlistIndex < _playlistTabbedPane.getDocumentCount())
      {
        _playlistTabbedPane.setSelectedIndex(playlistIndex);
      }
    });
  }

  private void recheckStatusOfOpenPlaylists()
  {
    this._playlistTabbedPane.getPlaylistEditors().stream().map(PlaylistEditCtrl::getPlaylist).forEach(Playlist::updateModifiedStatus);
  }

  private void configureGlobalKeyboardListener()
  {
    KeyEventPostProcessor pp = new KeyEventPostProcessor()
    {
      @Override
      public boolean postProcessKeyEvent(KeyEvent e)
      {
        // Advance the selected tab on Ctrl-Tab (make sure Shift isn't pressed)
        if (ctrlTabWasPressed(e) && _playlistTabbedPane.getDocumentCount() > 1)
        {
          _playlistTabbedPane.nextDocument();
        }

        // Regress the selected tab on Ctrl-Shift-Tab
        if (ctrlShiftTabWasPressed(e) && _playlistTabbedPane.getDocumentCount() > 1)
        {
          _playlistTabbedPane.prevPlaylist();
        }

        return true;
      }

      private boolean ctrlShiftTabWasPressed(KeyEvent e)
      {
        return (e.getKeyCode() == KeyEvent.VK_TAB) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) && _playlistTabbedPane.getDocumentCount() > 0 && e.getID() == KeyEvent.KEY_PRESSED;
      }

      private boolean ctrlTabWasPressed(KeyEvent e)
      {
        return (e.getKeyCode() == KeyEvent.VK_TAB) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) && ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) == 0) && _playlistTabbedPane.getDocumentCount() > 0 && e.getID() == KeyEvent.KEY_PRESSED;
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
          if (isOverItem && (e.getModifiersEx() & ActionEvent.CTRL_MASK) > 0)
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

          final TreePath[] selectPath = _playlistDirectoryTree.getSelectionPaths();
          final boolean allTopLevel = selectPath != null && Arrays.stream(selectPath)
            .allMatch(path -> path.getParentPath() != null && path.getParentPath().getParentPath() == null);

          _miRemovePlaylistDirectory.setVisible(allTopLevel);

          if (_playlistDirectoryTree.getSelectionCount() > 0)
          {
            _miOpenSelectedPlaylists.setEnabled(true);
            boolean singleFileSelected = _playlistDirectoryTree.getSelectionCount() == 1;
            _miRenameSelectedItem.setEnabled(singleFileSelected
              && !allTopLevel
              && Files.isRegularFile(GUIScreen.this.getSelectedPlaylistTreeNodes().get(0).getUserObject())
            );
            _miDeleteFile.setEnabled(true);
          }
          else
          {
            _miOpenSelectedPlaylists.setEnabled(false);
            _miDeleteFile.setEnabled(false);
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

  private void configureFileAndFolderChoosers()
  {
    _jOpenPlaylistFileChooser.setDialogTitle("Choose Playlists...");
    _jOpenPlaylistFileChooser.setAcceptAllFileFilterUsed(false);
    _jOpenPlaylistFileChooser.setFileFilter(new AllPlaylistFileFilter());
    _jOpenPlaylistFileChooser.setMultiSelectionEnabled(true);

    _jMediaDirChooser.setDialogTitle("Specify a media directory...");
    _jMediaDirChooser.setAcceptAllFileFilterUsed(false);
    _jMediaDirChooser.setMinimumSize(new Dimension(400, 500));
    _jMediaDirChooser.setPreferredSize(new Dimension(400, 500));

    _savePlaylistAsFileChooser.setDialogTitle("Save File:");
    _savePlaylistAsFileChooser.setAcceptAllFileFilterUsed(false);
    _savePlaylistAsFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    // Generate filters from dynamically loaded Lizzy playlist service providers
    LizzyPlaylistUtil.getPlaylistExtensionFilters().forEach(_savePlaylistAsFileChooser::addChoosableFileFilter);
    // Adjust target file name based on the filter selection
    _savePlaylistAsFileChooser.addPropertyChangeListener(propertyChangeEvent -> {
      if (_savePlaylistAsFileChooser.isVisible() && propertyChangeEvent.getPropertyName().equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY))
      {
        System.out.printf("Selected file = %s\n", _savePlaylistAsFileChooser.getSelectedFile());
        SpecificPlaylistFileFilter fileFilter = (SpecificPlaylistFileFilter) propertyChangeEvent.getNewValue();
        final File selectedFile = _savePlaylistAsFileChooser.getSelectedFile();
        if (selectedFile != null)
        {
          // Current selected file is not compliant with FileFilter, let's adjust it
          if (!fileFilter.getContentType().accept(selectedFile))
          {
            String curName = selectedFile.getName();
            String nameWithoutExtension = FileUtils.getExtension(selectedFile.getName()).map(
              ext -> curName.substring(0, curName.lastIndexOf("."))).orElse(curName);
            String newName = nameWithoutExtension + fileFilter.getContentType().getExtensions()[0];
            _savePlaylistAsFileChooser.setSelectedFile(new File(newName));
          }
        }
      }
      else
      {
        System.out.printf("property %s = %s\n", propertyChangeEvent.getPropertyName(), propertyChangeEvent.getNewValue());
      }
    });
  }

  public IAppOptions getOptions()
  {
    return this._listFixController.getApplicationConfiguration().getConfig();
  }

  public IMediaLibrary getMediaLibrary()
  {
    return this._listFixController.getMediaLibrary();
  }

  private void fireOptionsPopup()
  {
    final ApplicationOptionsConfiguration applicationConfiguration = this._listFixController.getApplicationConfiguration();
    AppOptionsDialog optDialog = new AppOptionsDialog(this, "listFix() options", true, (JsonAppOptions) applicationConfiguration.getConfig());
    JsonAppOptions options = optDialog.showDialog();
    if (optDialog.getResultCode() == AppOptionsDialog.OK)
    {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      _listFixController.setAppOptions(options);
      _listFixController.getHistory().setCapacity(options.getMaxPlaylistHistoryEntries());
      final MediaLibraryConfiguration mediaLibraryConfiguration = _listFixController.getMediaLibraryConfiguration();
      if (options.getAlwaysUseUNCPaths())
      {
        mediaLibraryConfiguration.switchMediaLibraryToUNCPaths();
        _lstMediaLibraryDirs.setListData(new Vector<>(_listFixController.getMediaLibrary().getMediaDirectories()));
      }
      else
      {
        _listFixController.switchMediaLibraryToMappedDrives();
        _lstMediaLibraryDirs.setListData(new Vector<>(_listFixController.getMediaLibrary().getMediaDirectories()));
      }
      try
      {
        applicationConfiguration.write();
      }
      catch (IOException e)
      {
        _logger.error("Writing application configuration", e);
        throw new RuntimeException("Writing application configuration", e);
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
    _playlistTreeRightClickMenu = new JPopupMenu();
    _miRefreshDirectoryTree = new JMenuItem();
    _miRemovePlaylistDirectory = new JMenuItem();
    _miOpenSelectedPlaylists = new JMenuItem();
    JMenuItem _miOpenSelectedPlaylistLocation = new JMenuItem();
    _miDeleteFile = new JMenuItem();
    _miRenameSelectedItem = new JMenuItem();
    JPanel _statusPanel = new JPanel();
    statusLabel = new JLabel();
    JSplitPane _splitPane = new JSplitPane();
    _leftSplitPane = new JSplitPane();
    JPanel _mediaLibraryPanel = new JPanel();
    JPanel _mediaLibraryButtonPanel = new JPanel();
    JButton _addMediaDirButton = new JButton();
    _removeMediaDirButton = new JButton();
    _refreshMediaDirsButton = new JButton();
    JScrollPane _mediaLibraryScrollPane = new JScrollPane();
    _lstMediaLibraryDirs = new JList<>(new String[]{"Please Add A Media Directory..."});
    JPanel _playlistDirectoryPanel = new JPanel();
    JScrollPane _treeScrollPane = new JScrollPane();
    _playlistDirectoryTree = new JTree();
    JPanel _playlistsDirectoryButtonPanel = new JPanel();
    JButton _btnSetPlaylistsDir = new JButton();
    JButton _btnRefresh = new JButton();
    _btnOpenSelected = new JButton();
    _playlistPanel = new JPanel();
    JPanel _gettingStartedPanel = new JPanel();
    JPanel _verticalPanel = new JPanel();
    JButton _openIconButton = new JButton();
    JPanel _spacerPanel = new JPanel();
    JButton _newIconButton = new JButton();
    JPanel _docTabPanel = new JPanel();
    _playlistTabbedPane = new JDocumentTabbedPane();
    JMenuBar _mainMenuBar = new JMenuBar();
    JMenu _fileMenu = new JMenu();
    JMenuItem _newPlaylistMenuItem = new JMenuItem();
    JMenuItem _loadMenuItem = new JMenuItem();
    JMenuItem _openPlaylistLocationMenuItem = new JMenuItem();
    JMenuItem _closeMenuItem = new JMenuItem();
    JMenuItem _closeAllMenuItem = new JMenuItem();
    Separator jSeparator1 = new Separator();
    JMenuItem _saveMenuItem = new JMenuItem();
    JMenuItem _saveAsMenuItem = new JMenuItem();
    JMenuItem _saveAllMenuItem = new JMenuItem();
    Separator jSeparator2 = new Separator();
    JMenuItem _miReload = new JMenuItem();
    JMenuItem _miReloadAll = new JMenuItem();
    Separator jSeparator3 = new Separator();
    recentMenu = new JMenu();
    JMenuItem _clearHistoryMenuItem = new JMenuItem();
    Separator jSeparator4 = new Separator();
    JMenuItem _appOptionsMenuItem = new JMenuItem();
    Separator jSeparator5 = new Separator();
    JMenuItem _exitMenuItem = new JMenuItem();
    JMenu _repairMenu = new JMenu();
    JMenuItem _miClosestMatchRepairOpenPlaylists = new JMenuItem();
    JMenu _helpMenu = new JMenu();
    JMenuItem _helpMenuItem = new JMenuItem();
    JMenuItem _updateCheckMenuItem = new JMenuItem();
    JMenuItem _aboutMenuItem = new JMenuItem();

    _miRemovePlaylistDirectory.setText("Remove playlist directory");
    _miRemovePlaylistDirectory.setToolTipText("Remove playlist directory from configuration");
    _miRemovePlaylistDirectory.addActionListener(evt -> this.removePlaylistDirectory(this._playlistDirectoryTree.getSelectionPaths()));
    _playlistTreeRightClickMenu.add(_miRemovePlaylistDirectory);

    _miRefreshDirectoryTree.setText("Refresh");
    _miRefreshDirectoryTree.addActionListener(evt -> this.updatePlaylistDirectoryPanel());
    _playlistTreeRightClickMenu.add(_miRefreshDirectoryTree);

    _miOpenSelectedPlaylists.setText("Open");
    _miOpenSelectedPlaylists.addActionListener(evt -> this.openTreeSelectedPlaylists());
    _playlistTreeRightClickMenu.add(_miOpenSelectedPlaylists);

    _miOpenSelectedPlaylistLocation.setText("Open playlist location");
    _miOpenSelectedPlaylistLocation.addActionListener(evt -> this.openPlaylistFoldersFromPlaylistTree());
    _playlistTreeRightClickMenu.add(_miOpenSelectedPlaylistLocation);

    _miDeleteFile.setMnemonic('D');
    _miDeleteFile.setText("Delete file(s)");
    _miDeleteFile.setToolTipText("Delete selected file(s)");
    _miDeleteFile.addActionListener(evt -> this.deleteTreeSelectedPlaylists());
    _playlistTreeRightClickMenu.add(_miDeleteFile);

    _miRenameSelectedItem.setMnemonic('R');
    _miRenameSelectedItem.setText("Rename");
    _miRenameSelectedItem.setToolTipText("Rename selected file or folder");
    _miRenameSelectedItem.addActionListener(evt -> this.renameTreeSelectedNode());
    _playlistTreeRightClickMenu.add(_miRenameSelectedItem);

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setTitle(String.format("listFix() version %s", applicationVersion));
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    setIconImage(this.applicationIcon);
    setMinimumSize(new Dimension(600, 149));
    setName("mainFrame"); // NOI18N
    addComponentListener(new ComponentAdapter()
    {
      @Override
      public void componentResized(ComponentEvent evt)
      {
        _leftSplitPane.setDividerLocation(.7);
      }
    });
    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent evt)
      {
        GUIScreen.this.confirmCloseApp();
      }
    });

    _statusPanel.setBorder(BorderFactory.createEtchedBorder());
    _statusPanel.setLayout(new BorderLayout());

    statusLabel.setForeground(new Color(75, 75, 75));
    statusLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    statusLabel.setText("Untitled List     Number of entries in list: 0     Number of lost entries: 0     Number of URLs: 0     Number of open playlists: 0");
    _statusPanel.add(statusLabel, BorderLayout.WEST);

    getContentPane().add(_statusPanel, BorderLayout.SOUTH);

    _splitPane.setDividerSize(7);
    _splitPane.setContinuousLayout(true);
    _splitPane.setMaximumSize(null);
    _splitPane.setOneTouchExpandable(true);
    _splitPane.setPreferredSize(new Dimension(785, 489));

    _leftSplitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    _leftSplitPane.setDividerLocation(280);
    _leftSplitPane.setDividerSize(7);
    _leftSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    _leftSplitPane.setContinuousLayout(true);
    _leftSplitPane.setMaximumSize(null);
    _leftSplitPane.setOneTouchExpandable(true);

    _mediaLibraryPanel.setBorder(BorderFactory.createTitledBorder(null, "Media Directories", TitledBorder.LEFT, TitledBorder.TOP));
    _mediaLibraryPanel.setAlignmentX(0.0F);
    _mediaLibraryPanel.setAlignmentY(0.0F);
    _mediaLibraryPanel.setLayout(new BorderLayout());

    _mediaLibraryButtonPanel.setMaximumSize(null);

    _addMediaDirButton.setText("Add");
    _addMediaDirButton.setToolTipText("Where do you keep your music?");
    _addMediaDirButton.setFocusable(false);
    _addMediaDirButton.setMargin(new Insets(2, 8, 2, 8));
    _addMediaDirButton.setMinimumSize(new Dimension(53, 25));
    _addMediaDirButton.addActionListener(evt -> _addMediaDirButtonActionPerformed());
    _mediaLibraryButtonPanel.add(_addMediaDirButton);

    _removeMediaDirButton.setText("Remove");
    _removeMediaDirButton.setToolTipText("Remove a directory from the search list");
    _removeMediaDirButton.setFocusable(false);
    _removeMediaDirButton.setMargin(new Insets(2, 8, 2, 8));
    _removeMediaDirButton.setMinimumSize(new Dimension(73, 25));
    _removeMediaDirButton.addActionListener(evt -> _removeMediaDirButtonActionPerformed());
    _mediaLibraryButtonPanel.add(_removeMediaDirButton);

    _refreshMediaDirsButton.setText("Refresh");
    _refreshMediaDirsButton.setToolTipText("The contents of your media library are cached; refresh to pickup changes");
    _refreshMediaDirsButton.setFocusable(false);
    _refreshMediaDirsButton.setMargin(new Insets(2, 8, 2, 8));
    _refreshMediaDirsButton.setMinimumSize(new Dimension(71, 25));
    _refreshMediaDirsButton.addActionListener(evt -> this.refreshMediaDirs());
    _mediaLibraryButtonPanel.add(_refreshMediaDirsButton);

    _mediaLibraryPanel.add(_mediaLibraryButtonPanel, BorderLayout.SOUTH);

    _lstMediaLibraryDirs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _lstMediaLibraryDirs.setTransferHandler(new FileListTransferHandler()
    {
      @Override
      public boolean handleFileList(List<File> fileList)
      {
        return fileList.stream()
          .filter(File::isDirectory)
          .map(folder -> {
            GUIScreen.this.addMediaFolder(folder);
            return true;
          })
          .reduce(false, (t, v) -> true);
      }
    });

    _mediaLibraryScrollPane.setViewportView(_lstMediaLibraryDirs);

    _mediaLibraryPanel.add(_mediaLibraryScrollPane, BorderLayout.CENTER);

    _leftSplitPane.setBottomComponent(_mediaLibraryPanel);

    _playlistDirectoryPanel.setBorder(BorderFactory.createTitledBorder(null, "Playlists Directories", TitledBorder.LEFT, TitledBorder.TOP));
    _playlistDirectoryPanel.setAlignmentX(0.0F);
    _playlistDirectoryPanel.setAlignmentY(0.0F);
    _playlistDirectoryPanel.setLayout(new BorderLayout());

    _playlistDirectoryTree.setDragEnabled(true);
    _playlistDirectoryTree.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          GUIScreen.this.openTreeSelectedPlaylists();
        }
      }
    });
    _treeScrollPane.setViewportView(_playlistDirectoryTree);

    _playlistDirectoryPanel.add(_treeScrollPane, BorderLayout.CENTER);

    _playlistsDirectoryButtonPanel.setMaximumSize(null);
    _playlistsDirectoryButtonPanel.setMinimumSize(new Dimension(300, 35));
    _playlistsDirectoryButtonPanel.setName(""); // NOI18N

    _btnSetPlaylistsDir.setText("Add");
    _btnSetPlaylistsDir.setToolTipText("Adds another playlist directory (folder) to the configuration");
    _btnSetPlaylistsDir.setMargin(new Insets(2, 8, 2, 8));
    _btnSetPlaylistsDir.addActionListener(evt -> _btnAddPlaylistsDirActionPerformed());
    _playlistsDirectoryButtonPanel.add(_btnSetPlaylistsDir);

    _btnRefresh.setText("Refresh");
    _btnRefresh.setToolTipText("Refresh Playlists Tree");
    _btnRefresh.setFocusable(false);
    _btnRefresh.setMargin(new Insets(2, 8, 2, 8));
    _btnRefresh.setMinimumSize(new Dimension(71, 25));
    _btnRefresh.addActionListener(evt -> updatePlaylistDirectoryPanel());
    _playlistsDirectoryButtonPanel.add(_btnRefresh);

    _btnOpenSelected.setText("Open");
    _btnOpenSelected.setToolTipText("Open Selected Playlist(s)");
    _btnOpenSelected.setEnabled(false);
    _btnOpenSelected.setFocusable(false);
    _btnOpenSelected.setMargin(new Insets(2, 8, 2, 8));
    _btnOpenSelected.setMinimumSize(new Dimension(71, 25));
    _btnOpenSelected.addActionListener(evt -> GUIScreen.this.openTreeSelectedPlaylists());
    _playlistsDirectoryButtonPanel.add(_btnOpenSelected);

    _playlistDirectoryPanel.add(_playlistsDirectoryButtonPanel, BorderLayout.PAGE_END);

    _leftSplitPane.setTopComponent(_playlistDirectoryPanel);

    _splitPane.setLeftComponent(_leftSplitPane);

    _playlistPanel.setBackground(SystemColor.window);
    _playlistPanel.setLayout(new CardLayout());

    _gettingStartedPanel.setBackground(new Color(255, 255, 255));
    _gettingStartedPanel.setLayout(new GridBagLayout());

    _verticalPanel.setBackground(new Color(255, 255, 255));
    _verticalPanel.setLayout(new BoxLayout(_verticalPanel, BoxLayout.Y_AXIS));

    _openIconButton.setIcon(getImageIcon("open-big.png")); // NOI18N
    _openIconButton.setText("Open a Playlist");
    _openIconButton.setToolTipText("Open a Playlist");
    _openIconButton.setAlignmentY(0.0F);
    _openIconButton.setFocusable(false);
    _openIconButton.setHorizontalTextPosition(SwingConstants.CENTER);
    _openIconButton.setIconTextGap(-2);
    _openIconButton.setMaximumSize(new Dimension(220, 180));
    _openIconButton.setMinimumSize(new Dimension(220, 180));
    _openIconButton.setPreferredSize(new Dimension(220, 180));
    _openIconButton.setVerticalAlignment(SwingConstants.TOP);
    _openIconButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    _openIconButton.addActionListener(evt -> _openIconButtonActionPerformed1());
    _verticalPanel.add(_openIconButton);

    _spacerPanel.setBackground(new Color(255, 255, 255));
    _verticalPanel.add(_spacerPanel);

    _newIconButton.setIcon(getImageIcon("icon_new_file.png")); // NOI18N
    _newIconButton.setText("New playlist");
    _newIconButton.setToolTipText("Create new empty playlist");
    _newIconButton.setAlignmentY(0.0F);
    _newIconButton.setFocusable(false);
    _newIconButton.setHorizontalTextPosition(SwingConstants.CENTER);
    _newIconButton.setIconTextGap(3);
    _newIconButton.setMaximumSize(new Dimension(220, 180));
    _newIconButton.setMinimumSize(new Dimension(220, 180));
    _newIconButton.setPreferredSize(new Dimension(220, 180));
    _newIconButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    _newIconButton.addActionListener(evt -> _newIconButtonActionPerformed());
    _verticalPanel.add(_newIconButton);

    _gettingStartedPanel.add(_verticalPanel, new GridBagConstraints());

    _gettingStartedPanel.setTransferHandler(this.playlistTransferHandler);

    _playlistPanel.add(_gettingStartedPanel, "_gettingStartedPanel");

    _docTabPanel.setLayout(new BorderLayout());
    _docTabPanel.setBorder(BorderFactory.createTitledBorder(null, "Playlists Editor", TitledBorder.LEFT, TitledBorder.TOP));
    _docTabPanel.add(_playlistTabbedPane, BorderLayout.CENTER);

    _playlistPanel.add(_docTabPanel, "_docTabPanel");

    _splitPane.setRightComponent(_playlistPanel);

    getContentPane().add(_splitPane, BorderLayout.CENTER);

    _mainMenuBar.setBorder(null);

    _fileMenu.setMnemonic('F');
    _fileMenu.setText("File");

    _newPlaylistMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
    _newPlaylistMenuItem.setMnemonic('N');
    _newPlaylistMenuItem.setText("New Playlist");
    _newPlaylistMenuItem.setToolTipText("Creates a New Playlist");
    _newPlaylistMenuItem.addActionListener(evt -> GUIScreen.this._newIconButtonActionPerformed());
    _fileMenu.add(_newPlaylistMenuItem);

    _loadMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
    _loadMenuItem.setMnemonic('O');
    _loadMenuItem.setText("Open Playlist");
    _loadMenuItem.setToolTipText("Opens a Playlist");
    _loadMenuItem.addActionListener(evt -> openIconButtonActionPerformed());
    _fileMenu.add(_loadMenuItem);

    _openPlaylistLocationMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
    _openPlaylistLocationMenuItem.setMnemonic('E');
    _openPlaylistLocationMenuItem.setText("Open Playlist Location");
    _openPlaylistLocationMenuItem.setToolTipText("Opens playlist folder in Explorer");
    _openPlaylistLocationMenuItem.addActionListener(evt -> openPlaylistLocation(this._currentPlaylist));
    _fileMenu.add(_openPlaylistLocationMenuItem);


    _closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
    _closeMenuItem.setMnemonic('C');
    _closeMenuItem.setText("Close");
    _closeMenuItem.setToolTipText("Closes The Current Playlist");
    _closeMenuItem.addActionListener(evt -> _closeMenuItemActionPerformed());
    _fileMenu.add(_closeMenuItem);

    _closeAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
    _closeAllMenuItem.setMnemonic('A');
    _closeAllMenuItem.setText("Close all");
    _closeAllMenuItem.setToolTipText("Closes all open playlists");
    _closeAllMenuItem.addActionListener(evt -> this._playlistTabbedPane.closeAll());
    _fileMenu.add(_closeAllMenuItem);
    _fileMenu.add(jSeparator1);

    _saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
    _saveMenuItem.setMnemonic('S');
    _saveMenuItem.setText("Save");
    _saveMenuItem.addActionListener(evt -> _saveMenuItemActionPerformed());
    _fileMenu.add(_saveMenuItem);

    _saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
    _saveAsMenuItem.setMnemonic('V');
    _saveAsMenuItem.setText("Save as");
    _saveAsMenuItem.addActionListener(evt -> _saveAsMenuItemActionPerformed());
    _fileMenu.add(_saveAsMenuItem);

    _saveAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
    _saveAllMenuItem.setMnemonic('S');
    _saveAllMenuItem.setText("Save All");
    _saveAllMenuItem.setToolTipText("Save all open playlists");
    _saveAllMenuItem.addActionListener(evt -> _saveAllMenuItemActionPerformed());
    _fileMenu.add(_saveAllMenuItem);
    _fileMenu.add(jSeparator2);

    _miReload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
    _miReload.setMnemonic('R');
    _miReload.setText("Reload");
    _miReload.setToolTipText("Reloads the current open playlist");
    _miReload.addActionListener(evt -> _miReloadActionPerformed());
    _fileMenu.add(_miReload);

    _miReloadAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
    _miReloadAll.setMnemonic('l');
    _miReloadAll.setText("Reload All");
    _miReloadAll.setToolTipText("Reloads All Currently Open Playlists");
    _miReloadAll.addActionListener(evt -> this.reloadAllTabs());
    _fileMenu.add(_miReloadAll);
    _fileMenu.add(jSeparator3);

    recentMenu.setText("Recent Playlists");
    recentMenu.setToolTipText("Recently Opened Playlists");
    _fileMenu.add(recentMenu);

    _clearHistoryMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
    _clearHistoryMenuItem.setMnemonic('H');
    _clearHistoryMenuItem.setText("Clear Playlist History");
    _clearHistoryMenuItem.setToolTipText("Clears the recently opened playlist history");
    _clearHistoryMenuItem.addActionListener(evt -> _clearHistoryMenuItemActionPerformed());
    _fileMenu.add(_clearHistoryMenuItem);
    _fileMenu.add(jSeparator4);

    _appOptionsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_DOWN_MASK));
    _appOptionsMenuItem.setMnemonic('p');
    _appOptionsMenuItem.setText("Options...");
    _appOptionsMenuItem.setToolTipText("Opens the Options Screen");
    _appOptionsMenuItem.addActionListener(evt -> fireOptionsPopup());
    _fileMenu.add(_appOptionsMenuItem);
    _fileMenu.add(jSeparator5);

    _exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
    _exitMenuItem.setMnemonic('x');
    _exitMenuItem.setText("Exit");
    _exitMenuItem.addActionListener(evt -> this.confirmCloseApp());
    _fileMenu.add(_exitMenuItem);

    _mainMenuBar.add(_fileMenu);

    _repairMenu.setText("Repair");

    _miClosestMatchRepairOpenPlaylists.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
    _miClosestMatchRepairOpenPlaylists.setText("Repair all open playlists");
    _miClosestMatchRepairOpenPlaylists.addActionListener(evt -> this.runClosestMatchOnAllTabs());
    _repairMenu.add(_miClosestMatchRepairOpenPlaylists);

    _mainMenuBar.add(_repairMenu);

    _helpMenu.setMnemonic('H');
    _helpMenu.setText("Help");

    _helpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
    _helpMenuItem.setMnemonic('H');
    _helpMenuItem.setText("Help");
    _helpMenuItem.setToolTipText("Open listFix() documentation");
    _helpMenuItem.addActionListener(evt -> launchListFixProjectUrl());
    _helpMenu.add(_helpMenuItem);

    _updateCheckMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
    _updateCheckMenuItem.setMnemonic('C');
    _updateCheckMenuItem.setText("Check For Updates");
    _updateCheckMenuItem.setToolTipText("Opens the listFix() download site");
    _updateCheckMenuItem.addActionListener(evt -> launchListFixUpdateUrl());
    _helpMenu.add(_updateCheckMenuItem);

    _aboutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK));
    _aboutMenuItem.setMnemonic('A');
    _aboutMenuItem.setText("About");
    _aboutMenuItem.setToolTipText("Version info and such...");
    _aboutMenuItem.addActionListener(evt -> _aboutMenuItemActionPerformed());
    _helpMenu.add(_aboutMenuItem);

    _mainMenuBar.add(_helpMenu);

    _playlistTabbedPane.addDocumentChangeListener(new IDocumentChangeListener()
    {
      @Override
      public boolean tryClosingDocument(JPlaylistComponent document)
      {
        return GUIScreen.this.tryCloseTab(document);
      }

      @Override
      public void documentOpened(JPlaylistComponent document)
      {
        updateMenuItemStatuses();
      }

      @Override
      public void documentClosed(JPlaylistComponent playlistComponent)
      {
        cleanupOnTabClose(playlistComponent);
        if (_playlistTabbedPane.getDocumentCount() == 0)
        {
          ((CardLayout) _playlistPanel.getLayout()).show(_playlistPanel, "_gettingStartedPanel");
          currentTabChanged();
          updateMenuItemStatuses();
        }
      }

      @Override
      public void documentActivated(JPlaylistComponent doc)
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

      _miClosestMatchRepairOpenPlaylists,
    };

    splashScreen.setIconImage(applicationIcon);

    this.updatePlaylistDirectoryPanel();

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void openTreeSelectedPlaylists()
  {
    this.getSelectedFilesFromTreePlaylists().forEach(toOpen -> {
      if (Files.isDirectory(toOpen))
      {
        List<Path> files = new FileTypeSearch().findFiles(toOpen, new AllPlaylistFileFilter());
        for (Path f : files)
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

  private List<Path> getSelectedFilesFromTreePlaylists()
  {
    TreePath[] paths = this._playlistDirectoryTree.getSelectionPaths();
    if (paths == null)
    {
      return Collections.emptyList();
    }
    return Arrays.stream(paths).map(FileTreeNodeGenerator::treePathToFileSystemPath).collect(Collectors.toList());
  }

  private void deleteTreeSelectedPlaylists()
  {
    int[] selRows = _playlistDirectoryTree.getSelectionRows();
    if (selRows == null ||
      selRows.length == 0 ||
      JOptionPane.showConfirmDialog(this, new JTransparentTextArea("Are you sure you want to delete the selected file?"), "Delete Selected File?", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
    {
      return;
    }
    final List<Path> playlistsDirectoriesToRemove = new ArrayList<>();
    for (TreePath selPath : this.getSelectedPlaylistTreePaths())
    {
      PlaylistTreeNode treeNode = (PlaylistTreeNode) selPath.getLastPathComponent();
      Path toDelete = ((PlaylistTreeNode) selPath.getLastPathComponent()).getUserObject();
      if (Files.isDirectory(toDelete))
      {
        try
        {
          FileUtils.deleteDirectory(toDelete);
          Files.delete(toDelete);
        }
        catch (IOException ioe)
        {
          final String message = String.format("Failed to delete folder: %s: %s", toDelete, ioe.getMessage());
          _logger.error(message, ioe);
          JOptionPane.showMessageDialog(this, message, "Deleting Playlist Failed", JOptionPane.WARNING_MESSAGE);
        }
        if (treeNode.getParent() == null)
        {
          // Node is a configured playlist directory
          playlistsDirectoriesToRemove.add(treeNode.getUserObject());
        }
      }
      else
      {
        if (Files.isWritable(toDelete))
        {
          try
          {
            Files.delete(toDelete);
          }
          catch (IOException e)
          {
            throw new RuntimeException(e);
          }
          this._playlistDirectoryTree.makeVisible(selPath);
          DefaultTreeModel treeModel = (DefaultTreeModel) this._playlistDirectoryTree.getModel();
          treeModel.removeNodeFromParent(treeNode);
          this._playlistTabbedPane.remove(toDelete);
        }
        else
        {
          final String fileShortname = toDelete.getFileName().toString();
          JOptionPane.showMessageDialog(this, String.format("Failed to delete playlist: %s", fileShortname), "Deleting Playlist Failed", JOptionPane.WARNING_MESSAGE);
        }
      }
    }
    this.removePlaylistDirectory(playlistsDirectoriesToRemove);
  }

  public List<TreePath> getSelectedPlaylistTreePaths()
  {
    int[] selRows = _playlistDirectoryTree.getSelectionRows();
    if (selRows != null)
    {
      return Arrays.stream(selRows).mapToObj(i -> _playlistDirectoryTree.getPathForRow(i)).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public List<PlaylistTreeNode> getSelectedPlaylistTreeNodes()
  {
    return this.getSelectedPlaylistTreePaths().stream().map(path -> (PlaylistTreeNode) path.getLastPathComponent()).collect(Collectors.toList());
  }

  private void renameTreeSelectedNode()
  {
    List<PlaylistTreeNode> selectedTreeNodes = getSelectedPlaylistTreeNodes();
    if (selectedTreeNodes.size() != 1)
    {
      _logger.debug(String.format("Will only rename when exactly 1 file is selected, got %s.", selectedTreeNodes.size()));
      return;
    }

    for (PlaylistTreeNode treeNode : selectedTreeNodes)
    {
      Path nodePath = treeNode.getUserObject();
      String str = treeNode.toString();
      String reply = JOptionPane.showInputDialog(this, new JTransparentTextArea("Rename " + str), nodePath.getFileName().toString());
      if (reply != null && !reply.equals(""))
      {
        final Path destPath = nodePath.getParent().resolve(reply);
        _logger.info(String.format("Rename playlist \"%s\" to \"%s\"", nodePath, destPath));
        try
        {
          Files.move(nodePath, destPath);
        }
        catch (IOException ioe)
        {
          _logger.warn("Renaming playlist failed", ioe);
          JOptionPane.showMessageDialog(this, new JTransparentTextArea("Failed to rename file."), "File playlist failed", JOptionPane.ERROR_MESSAGE);
          continue;
        }
        treeNode.setUserObject(destPath);
        ((DefaultTreeModel) _playlistDirectoryTree.getModel()).nodeChanged(treeNode);
        JPlaylistComponent doc = this._playlistTabbedPane.getPlaylist(nodePath);
        if (doc != null)
        {
          // Update playlist editor, if open
          doc.setPlaylist(doc.getPlaylist());
        }

      }
    }
  }

  private void playlistDirectoryTreeNodeDoubleClicked(TreePath selPath)
  {
    Path toOpen = FileTreeNodeGenerator.treePathToFileSystemPath(selPath);
    if (Files.isRegularFile(toOpen))
    {
      this.openPlaylist(toOpen);
    }
  }

  private void _clearHistoryMenuItemActionPerformed()
  {
    try
    {
      _listFixController.clearM3UHistory();
    }
    catch (IOException e)
    {
      _logger.error("Error clear M3U history", e);
    }
    updateRecentMenu();
  }

  private void _saveAsMenuItemActionPerformed()
  {
    if (_currentPlaylist == null)
    {
      return;
    }

    this.showPlaylistSaveAsDialog(_currentPlaylist);
  }

  private void openIconButtonActionPerformed()
  {
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
        this.openPlaylist(file.toPath());
      }
    }
  }

  private void openPlaylistFoldersFromPlaylistTree()
  {
    this.getSelectedFilesFromTreePlaylists()
      .forEach(this::openFileLocation);
  }

  private void openPlaylistLocation(Playlist playList)
  {
   this.openFileLocation(playList.getPath());
  }

  private void openFileLocation(Path path)
  {
    new OpenFileLocation(this).openFileLocation(path);
  }

  @Override
  public IApplicationConfiguration getApplicationConfiguration()
  {
    return this._listFixController;
  }

  @Override
  public void openFileListDrop(List<File> droppedFiles)
  {
    this.openPathListDrop(droppedFiles.stream().map(File::toPath).collect(Collectors.toList()));
  }

  @Override
  public void openPathListDrop(List<Path> droppedFiles)
  {
    try
    {
      for (Path filePath : droppedFiles)
      {
        if (LizzyPlaylistUtil.isPlaylist(filePath))
        {
          this.openPlaylist(filePath);
        }
        else if (Files.isDirectory(filePath))
        {
          List<Path> filesToInsert = PlaylistScanner.getAllPlaylists(filePath);
          for (Path f : filesToInsert)
          {
            this.openPlaylist(f);
          }
        }
      }
    }
    catch (IOException ioe)
    {
      _logger.warn("Failed to open playlist", ioe);
      JOptionPane.showMessageDialog(this, new JTransparentTextArea(ExStack.textFormatErrorForUser("There was a problem opening the file you selected.", ioe.getCause())),
        "Open Playlist Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  @Override
  public void openPlaylist(final Path playlistPath)
  {
    // do nothing if the file is already open.

    this._playlistTabbedPane.getPlaylistEditors().stream()
      .map(PlaylistEditCtrl::getPlaylist)
      .filter(list -> list.getPath().equals(playlistPath))
      .findAny().ifPresent(list -> _playlistTabbedPane.setActivePlaylist(list));

    try
    {
      if (!Files.exists(playlistPath))
      {
        JOptionPane.showMessageDialog(this, new JTransparentTextArea("File Not Found."), "Open Playlist Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    catch (HeadlessException ex)
    {
      _logger.error("Open playlist error", ex);
      JOptionPane.showMessageDialog(this, new JTransparentTextArea(ExStack.textFormatErrorForUser("There was a problem opening the file you selected.", ex.getCause())),
        "Open Playlist Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    JPlaylistComponent tempComp = _playlistTabbedPane.getPlaylist(playlistPath);
    if (tempComp == null)
    {
      ProgressWorker<Playlist, String> worker = new ProgressWorker<>()
      {
        @Override
        protected Playlist doInBackground() throws Exception
        {
          this.setMessage("Please wait while your playlist is opened and analyzed.");
          Playlist list = PlaylistFactory.getPlaylist(playlistPath, this, GUIScreen.this.getOptions());
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
          PlaylistHistory history = _listFixController.getHistory();
          history.add(playlistPath.toString());
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

      boolean textOnly = false; // ToDo, was: type == PlaylistType.ITUNES || type == PlaylistType.XSPF;
      // Can't show a progress dialog for these as we have no way to track them at present.
      final String filename = playlistPath.getFileName().toString();
      ProgressDialog pd = new ProgressDialog(this, true, worker, "Loading '" + (filename.length() > 70 ? filename.substring(0, 70) : filename) + "'...", textOnly, true);
      pd.setVisible(true);
    }
    else
    {
      _playlistTabbedPane.setActivePlaylist(tempComp.getPlaylist());
    }
  }

  @Override
  public void openNewTabForPlaylist(Playlist playlist)
  {
    PlaylistEditCtrl editor = new PlaylistEditCtrl(this);
    editor.setPlaylist(playlist);

    final JPlaylistComponent tempComp = _playlistTabbedPane.openPlaylist(editor, playlist);
    _playlistTabbedPane.setActivePlaylist(playlist);

    // update title and status bar if list was modified during loading (due to fix on load option)
    if (playlist.isModified())
    {
      this.onPlaylistModified(playlist);
      updateTabTitleForPlaylist(playlist, tempComp);
    }

    if (_playlistTabbedPane.getDocumentCount() == 1)
    {
      ((CardLayout) _playlistPanel.getLayout()).show(_playlistPanel, "_docTabPanel");
    }
  }

  public void updateRecentMenu()
  {
    recentMenu.removeAll();
    String[] files = _listFixController.getRecentM3Us();
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
        temp.addActionListener(this::recentPlaylistActionPerformed);
        recentMenu.add(temp);
      }
    }
  }

  private void updateAllComponentTreeUIs()
  {
    SwingUtilities.updateComponentTreeUI(this);
    SwingUtilities.updateComponentTreeUI(_jOpenPlaylistFileChooser);
    SwingUtilities.updateComponentTreeUI(_jMediaDirChooser);
    SwingUtilities.updateComponentTreeUI(_savePlaylistAsFileChooser);
    SwingUtilities.updateComponentTreeUI(_playlistTreeRightClickMenu);
    SwingUtilities.updateComponentTreeUI(_playlistTabbedPane);
  }

  private Playlist getPlaylistFromDocumentComponent(JPlaylistComponent ctrl)
  {
    return ctrl.getComponent().getPlaylist();
  }

  private void handlePlaylistSave(final Playlist list) throws HeadlessException
  {
    if (list.isNew())
    {
      this.showPlaylistSaveAsDialog(list);
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
            list.save(list.getType(), this);
            return null;
          }
        };
        ProgressDialog pd = new ProgressDialog(this, true, worker, "Saving...", false, false);
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

  @Override
  public boolean showPlaylistSaveAsDialog(Playlist playlist)
  {
    _savePlaylistAsFileChooser.setSelectedFile(playlist.getFile());
    // Select the file-filter to the current playlist
    Arrays.stream(_savePlaylistAsFileChooser.getChoosableFileFilters())
      .map(filter -> (SpecificPlaylistFileFilter) filter)
      .filter(filter -> filter.getPlaylistProvider().getId().equals(playlist.getType().name()))
      .findFirst()
      .ifPresent(_savePlaylistAsFileChooser::setFileFilter);

    int rc = _savePlaylistAsFileChooser.showSaveDialog(this);
    if (rc == JFileChooser.APPROVE_OPTION)
    {
      final File newPlaylistFile = _savePlaylistAsFileChooser.getSelectedFile();

      // prompt for confirmation if the file already exists...
      if (newPlaylistFile.exists())
      {
        int result = JOptionPane.showConfirmDialog(this, new JTransparentTextArea("You picked a file that already exists, should I really overwrite it?"), "File Exists Warning", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.NO_OPTION)
        {
          return false;
        }
      }

      SpecificPlaylistFileFilter targetFileFilter = (SpecificPlaylistFileFilter) _savePlaylistAsFileChooser.getFileFilter();
      PlaylistFormat targetFormat = PlaylistFormat.valueOf(targetFileFilter.getPlaylistProvider().getId());
      if (!targetFileFilter.getPlaylistProvider().getId().equals(playlist.getType().name()))
      {
        _logger.info(String.format("Conversion of playlist is requested from %s to %s", playlist.getType(), targetFormat));
      }

      final Cursor originalCursor = getCursor();
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      try
      {
        this.savePlaylist(playlist, newPlaylistFile, targetFormat);
        return true;
      }
      catch (CancellationException e)
      {
        return false;
      }
      catch (HeadlessException | InterruptedException | ExecutionException | IOException e)
      {
        _logger.error("Error saving your playlist", e);
        JOptionPane.showMessageDialog(this, new JTransparentTextArea("Sorry, there was an error saving your playlist.  Please try again, or file a bug report."));
        return false;
      }
      finally
      {
        setCursor(originalCursor);
      }
    }
    return false;
  }

  @Override
  public void savePlaylist(Playlist playlist) throws InterruptedException, IOException, ExecutionException
  {
    this.savePlaylist(playlist, playlist.getFile(), playlist.getType());
  }

  @Override
  public void savePlaylist(Playlist playlist, File saveAsFile, PlaylistFormat format) throws InterruptedException, IOException, ExecutionException
  {
    this.savePlaylist(playlist, saveAsFile.toPath(), format);
  }

  public void savePlaylist(Playlist playlist, Path saveAsPath, PlaylistFormat format) throws InterruptedException, IOException, ExecutionException
  {
    final Path originalPlaylistPath = playlist.getPath();
    ProgressWorker<Void, String> worker = new ProgressWorker<>()
    {
      @Override
      protected Void doInBackground() throws Exception
      {
        playlist.saveAs(saveAsPath, format, this);
        return null;
      }
    };

    ProgressDialog pd = new ProgressDialog(this, true, worker, "Saving...", false, false);
    pd.setMessage("Please wait while your playlist is saved to disk.");
    pd.setVisible(true);

    worker.get();

    updatePlaylistDirectoryPanel();

    // update playlist history
    PlaylistHistory history = _listFixController.getHistory();
    history.add(playlist.getFile().getPath());
    history.write();
    updateRecentMenu();
  }

  private void updateMenuItemStatuses()
  {
    boolean enable = _playlistTabbedPane.getDocumentCount() > 0;
    Arrays.stream(componentsRequireActivePlaylist).forEach(c -> c.setEnabled(enable));
  }

  private void currentTabChanged()
  {
    this.currentTabChanged(_playlistTabbedPane.getActiveTab());
  }

  private void currentTabChanged(JPlaylistComponent documentComponent)
  {
    Playlist list = documentComponent != null ? documentComponent.getComponent().getPlaylist() : null;
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

    this.onPlaylistModified(_currentPlaylist);
    if (_currentPlaylist != null)
    {
      _currentPlaylist.addModifiedListener(_playlistListener);
    }
  }

  // Setup the listener for changes to the current playlist.  Essentially turns around and calls onPlaylistModified().
  private void initPlaylistListener()
  {
    _playlistListener = this::onPlaylistModified;
  }

  private void onPlaylistModified(Playlist list)
  {
    if (list != null)
    {
      String fmt = "Currently Open: %s%s     Number of entries in list: %d     Number of lost entries: %d     Number of URLs: %d     Number of open playlists: %d";
      String txt = String.format(fmt, list.getFilename(), list.isModified() ? "*" : "", list.size(), list.getMissingCount(), list.getUrlCount(), _playlistTabbedPane.getDocumentCount());
      statusLabel.setText(txt);
    }
    else
    {
      statusLabel.setText("No list(s) loaded");
    }
  }

  private void updateTabTitleForPlaylist(Playlist list, JPlaylistComponent comp)
  {
    comp.setPlaylist(list);
  }

  public void runClosestMatchOnAllTabs()
  {
    for (PlaylistEditCtrl ctrl : this._playlistTabbedPane.getPlaylistEditors()) {
      this._playlistTabbedPane.setActivePlaylist(ctrl.getPlaylist());
      if (!ctrl.locateMissingFiles() || !ctrl.bulkFindClosestMatches()) {
        break;
      }
    }
  }

  private void reloadAllTabs()
  {
    this._playlistTabbedPane.getPlaylistEditors().forEach(ctrl -> {
      this._playlistTabbedPane.setActivePlaylist(ctrl.getPlaylist());
      ctrl.reloadPlaylist();
    });
  }

  public boolean tryCloseTab(JPlaylistComponent ctrl)
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
            playlist.save(playlist.getType(), this);
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
            this.cleanupOnTabClose(ctrl);
            return true;
          }
        }
        catch (InterruptedException ignored)
        {
          // Cancelled
        }
        catch (ExecutionException ex)
        {
          _logger.error("Save Error", ex);
          JOptionPane.showMessageDialog(this, ex.getCause(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
      }
      else if (rc == 1)
      {
        return this.showPlaylistSaveAsDialog(playlist);
      }
      else if (rc == 2)
      {
        this.cleanupOnTabClose(ctrl);
        return true;
      }
      return false;
    }
    return true;
  }

  private void cleanupOnTabClose(JPlaylistComponent playlistComponent)
  {
    playlistComponent.setPlaylist(null); // Remove listeners
  }

  private void confirmCloseApp()
  {
    final Collection<Playlist> modifiedPlaylists = this._playlistTabbedPane.getPlaylistEditors().stream()
      .map(PlaylistEditCtrl::getPlaylist)
      .filter(Playlist::isModified)
      .collect(Collectors.toList());

    if (!modifiedPlaylists.isEmpty()) {
      Object[] options =
        {
          "Discard changes and exit", "Cancel"
        };
      int rc = JOptionPane.showOptionDialog(this, new JTransparentTextArea("You have unsaved changes. Do you really want to discard these changes and exit?"), "Confirm Close",
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
      if (rc == JOptionPane.NO_OPTION)
      {
        return;
      }
      else if (rc == JOptionPane.YES_OPTION)
      {
        // Close unsaved playlists to prevent to prevent those from being re-opened next run
        modifiedPlaylists.stream().filter(Playlist::isNew).forEach(unsavedPlaylist -> {
          this._playlistTabbedPane.remove(unsavedPlaylist);
        });
      }
    }

    // Store open playlists
    List<String> openPlaylists = this._playlistTabbedPane.getPlaylistEditors().stream()
      .map(playlistEditCtrl -> playlistEditCtrl.getPlaylist().getPath().toString())
      .collect(Collectors.toList());

    IApplicationState appState = this._listFixController.getApplicationConfiguration().getConfig().getApplicationState();
    List<String> playlistPaths = appState.getPlaylistsOpened();
    playlistPaths.clear();
    playlistPaths.addAll(openPlaylists);
    appState.setActivePlaylistIndex(openPlaylists.isEmpty() ? null : _playlistTabbedPane.getSelectedIndex());

    try
    {
      // Write application settings to config file
      this._listFixController.getApplicationConfiguration().write();
    }
    catch (IOException e)
    {
      _logger.error("Failed to save application settings", e);
    }
    System.exit(0);
  }

  private void _addMediaDirButtonActionPerformed()
  {
    int response = _jMediaDirChooser.showOpenDialog(this);
    if (response == JFileChooser.APPROVE_OPTION)
    {
      this.addMediaFolder(_jMediaDirChooser.getSelectedFile());
    }
    else
    {
      _jMediaDirChooser.cancelSelection();
    }
    updateMediaDirButtons();
  }

  private void addMediaFolder(File mediaFolderToAdd)
  {

    UNCFile mediaDir = new UNCFile(mediaFolderToAdd);
    if (getApplicationConfig().getAlwaysUseUNCPaths())
    {
      if (mediaDir.onNetworkDrive())
      {
        mediaDir = new UNCFile(mediaDir.getUNCPath());
      }
    }
    final String dir = mediaDir.getPath();

    // first let's see if this is a subdirectory of any of the media directories already in the list, and error out if so...
    if (ArrayFunctions.containsStringPrefixingAnotherString(_listFixController.getMediaLibrary().getMediaDirectories(), dir, !ListFixController.FILE_SYSTEM_IS_CASE_SENSITIVE))
    {
      JOptionPane.showMessageDialog(this, new JTransparentTextArea("The directory you attempted to add is a subdirectory of one already in your media library, no change was made."),
        "Notification", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    else
    {
      // Now check if any of the media directories is a subdirectory of the one we're adding and remove the media directory if so.
      int removedFolders = 0;
      for (String dirToCheck : new ArrayList<>(_listFixController.getMediaLibrary().getMediaDirectories()))
      {
        if (dirToCheck.startsWith(dir))
        {
          // Only showing the message the first time we find this condition...
          removeMediaDir(dirToCheck);
          removedFolders++;
        }
      }
      if (removedFolders > 0)
      {
        String message = String.format("Removed %d duplicated media directories", removedFolders);
        _logger.info(message);
        JOptionPane.showMessageDialog(this,
          new JTransparentTextArea(String.format(message)),
          "Notification", JOptionPane.INFORMATION_MESSAGE);
      }
    }

    ProgressWorker<Void, Void> worker = new ProgressWorker<>()
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
    }
    catch (InterruptedException | CancellationException ex)
    {
      _logger.debug("Cancelled");
    }
    catch (ExecutionException ex)
    {
      _logger.error(ex);
    }
    _lstMediaLibraryDirs.setListData(new Vector<>(_listFixController.getMediaLibrary().getMediaDirectories()));
    updateMediaDirButtons();
  }

  private void _removeMediaDirButtonActionPerformed()
  {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    try
    {
      String selection = _lstMediaLibraryDirs.getSelectedValue();
      if (selection != null)
      {
        if (!selection.equals("Please Add A Media Directory..."))
        {
          _listFixController.getMediaLibraryConfiguration().removeMediaDir(selection);
          _lstMediaLibraryDirs.setListData(new Vector<>(_listFixController.getMediaLibrary().getMediaDirectories()));
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
  }

  private void removeMediaDir(String mediaDirectory)
  {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    try
    {
      _listFixController.getMediaLibraryConfiguration().removeMediaDir(mediaDirectory);
      _lstMediaLibraryDirs.setListData(new Vector<>(_listFixController.getMediaLibrary().getMediaDirectories()));
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

  private void _aboutMenuItemActionPerformed()
  {
    JOptionPane.showMessageDialog(this, "listFix( ) v" + applicationVersion + "\n\nBrought To You By: " +
        "\n          Borewit" +
        "\n          Jeremy Caron (firewyre) " +
        "\n          Kennedy Akala (kennedyakala)" +
        "\n          John Peterson (johnpeterson)" +
        "\n\nProject home: https://github.com/Borewit/listFix",
      "About", JOptionPane.INFORMATION_MESSAGE);
  }

  private void refreshMediaDirs()
  {
    ProgressWorker<Void, Void> worker = new ProgressWorker<>()
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
      _lstMediaLibraryDirs.setListData(_listFixController.getMediaLibrary().getMediaDirectories().toArray(new String[]{}));
    }
    catch (InterruptedException | CancellationException ex)
    {
      _logger.warn("Cancelled");
    }
    catch (ExecutionException ex)
    {
      _logger.error("Error refresh media directories", ex);
      throw new RuntimeException(ex);
    }
  }

  private void launchListFixProjectUrl()
  {
    BrowserLauncher.launch("https://github.com/Borewit/listFix");
  }

  private void launchListFixUpdateUrl()
  {
    BrowserLauncher.launch("https://github.com/Borewit/listFix/releases");
  }

  private void _openIconButtonActionPerformed1()
  {
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
        this.openPlaylist(file.toPath());
      }
    }
    else
    {
      _jOpenPlaylistFileChooser.cancelSelection();
    }
  }

  private void _saveMenuItemActionPerformed()
  {
    if (_currentPlaylist == null)
    {
      return;
    }

    handlePlaylistSave(_currentPlaylist);
  }

  private void _newIconButtonActionPerformed()
  {
    try
    {
      _currentPlaylist = Playlist.makeNewPersistentPlaylist(DefaultPlaylistFormatExtension, this.getOptions());
      PlaylistEditCtrl editor = new PlaylistEditCtrl(this);
      editor.setPlaylist(_currentPlaylist);
      _playlistTabbedPane.openPlaylist(editor, _currentPlaylist);
      _playlistTabbedPane.setActivePlaylist(_currentPlaylist);

      onPlaylistModified(_currentPlaylist);
      _currentPlaylist.addModifiedListener(_playlistListener);

      if (_playlistTabbedPane.getDocumentCount() == 1)
      {
        ((CardLayout) _playlistPanel.getLayout()).show(_playlistPanel, "_docTabPanel");
      }
    }
    catch (IOException | PlaylistProviderNotFoundException ex)
    {
      _logger.error("Error creating a new playlist", ex);
      JOptionPane.showMessageDialog(this,
        new JTransparentTextArea(ExStack.textFormatErrorForUser("Sorry, there was an error creating a new playlist.  Please try again, or file a bug report.", ex.getCause())),
        "New Playlist Error",
        JOptionPane.ERROR_MESSAGE);
    }
  }

  private void _closeMenuItemActionPerformed()
  {
    if (_playlistTabbedPane.getDocumentCount() > 0)
    {
      _playlistTabbedPane.closeActiveDocument();
    }
  }

  private void _btnAddPlaylistsDirActionPerformed()
  {
    int response = _jMediaDirChooser.showOpenDialog(this);
    if (response == JFileChooser.APPROVE_OPTION)
    {
      this.addPlaylistFolder(_jMediaDirChooser.getSelectedFile().getAbsoluteFile());
    }
  }

  private void _miReloadActionPerformed()
  {
    if (_playlistTabbedPane.getActiveTab() != null)
    {
      _playlistTabbedPane.getActiveTab().getComponent().reloadPlaylist();
    }
  }

  private void _saveAllMenuItemActionPerformed()
  {
    for (int i = 0; i < _playlistTabbedPane.getDocumentCount(); i++)
    {
      Playlist list = getPlaylistFromDocumentComponent(_playlistTabbedPane.getComponentAt(i));
      handlePlaylistSave(list);
    }
  }

  private void setApplicationFont(Font font)
  {
    GUIScreen.initApplicationFont(font);
    this.updateAllComponentTreeUIs();
  }

  private static void initApplicationFont(Font font)
  {
    Enumeration<Object> enumer = UIManager.getDefaults().keys();
    while (enumer.hasMoreElements())
    {
      Object key = enumer.nextElement();
      Object value = UIManager.get(key);
      if (value instanceof FontUIResource)
      {
        UIManager.put(key, new FontUIResource(font));
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
    final Cursor restoreCursorState = this.getCursor();
    try
    {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      List<Path> playListDirFiles = this.getApplicationConfig().getPlaylistDirectories().stream()
        .map(Path::of)
        .collect(Collectors.toList());

      PlaylistTreeNode playlistTreeNode = FileTreeNodeGenerator.addNodes(null, playListDirFiles);

      Enumeration<TreePath> treeStateEnum = saveExpansionState(this._playlistDirectoryTree);
      try
      {
        DefaultTreeModel treeModel = (DefaultTreeModel) this._playlistDirectoryTree.getModel();
        treeModel.setRoot(playlistTreeNode);
        loadExpansionState(this._playlistDirectoryTree, treeStateEnum);
      }
      finally
      {
        loadExpansionState(this._playlistDirectoryTree, treeStateEnum);
      }
    }
    finally
    {
      this.setCursor(restoreCursorState);
    }
  }

  private void removePlaylistDirectory(TreePath[] selectedPath)
  {
    if (selectedPath != null)
    {
      removePlaylistDirectory(Arrays.stream(selectedPath)
        .map(FileTreeNodeGenerator::treePathToFileSystemPath)
        .collect(Collectors.toList()));
    }
  }

  private void removePlaylistDirectory(Collection<Path> selectedPath)
  {
    selectedPath.forEach(playlistDirectory -> {
      _logger.info(String.format("Removing playlist directory from configuration: %s", playlistDirectory));
      this.getApplicationConfig().getPlaylistDirectories().remove(playlistDirectory.toString());
    });
    this.updatePlaylistDirectoryPanel();
    try
    {
      this._listFixController.getApplicationConfiguration().write();
    }
    catch (IOException e)
    {
      throw new RuntimeException("Failed to write updated application configuration", e);
    }
  }

  /**
   * Save the expansion state of a tree.
   *
   * @return expanded tree path as Enumeration
   */
  private Enumeration<TreePath> saveExpansionState(JTree tree)
  {
    return tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
  }

  /**
   * Restore the expansion state of a JTree.
   *
   * @param enumeration An enumeration of expansion state. You can get it using {@link #saveExpansionState(javax.swing.JTree)}.
   */
  private void loadExpansionState(JTree tree, Enumeration<TreePath> enumeration)
  {
    if (enumeration != null)
    {
      while (enumeration.hasMoreElements())
      {
        TreePath treePath = enumeration.nextElement();
        // tree.
        tree.expandPath(treePath);
      }
    }
  }

  private void recentPlaylistActionPerformed(ActionEvent evt)
  {
    JMenuItem temp = (JMenuItem) evt.getSource();
    Path playlist = Path.of(temp.getText());
    openPlaylist(playlist);
  }

  private void setLookAndFeel(String className)
  {
    try
    {
      UIManager.setLookAndFeel(className);
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
      Thread.currentThread().setUncaughtExceptionHandler((thread, e) -> _logger.error("Uncaught Exception", e));
    });

    IAppOptions tempOptions = ApplicationOptionsConfiguration.load().getConfig();
    assert tempOptions != null;
    GUIScreen.initApplicationFont(tempOptions.getAppFont());
    GUIScreen mainWindow = new GUIScreen();

    if (mainWindow.getLocation().equals(new Point(0, 0)))
    {
      DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
      Dimension labelSize = mainWindow.getPreferredSize();
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
        mainWindow.openPlaylist(Path.of(arg));
      }
      catch (Exception ex)
      {
        _logger.error("Error opening playlists from command line", ex);
      }
    }
  }

  @Override
  public void addPlaylistFolder(File playlistFile)
  {
    if (playlistFile.exists())
    {
      _logger.info(String.format("Add playlist directory to configuration: %s", playlistFile));
      this.getApplicationConfig().getPlaylistDirectories().add(playlistFile.getAbsolutePath());
    }
    else
    {
      JOptionPane.showMessageDialog(this, new JTransparentTextArea("The directory you selected/entered does not exist."));
    }
    try
    {
      this._listFixController.getApplicationConfiguration().write();
    }
    catch (IOException e)
    {
      throw new RuntimeException("Failed to write application configuration", e);
    }
    this.updatePlaylistDirectoryPanel();
  }

  private static ImageIcon getImageIcon(String imageFilename)
  {
    return new ImageIcon(getResourceUrl("/images/" + imageFilename));
  }

  private static URL getResourceUrl(String resourcePath)
  {
    URL url = GUIScreen.class.getResource(resourcePath);
    if (url == null)
      throw new RuntimeException(String.format("Failed to load resource %s", resourcePath));
    return url;
  }

  private JButton _btnOpenSelected;
  private JDocumentTabbedPane _playlistTabbedPane;
  private JSplitPane _leftSplitPane;
  private JList<String> _lstMediaLibraryDirs;
  private JMenuItem _miDeleteFile;
  private JMenuItem _miOpenSelectedPlaylists;
  private JMenuItem _miRefreshDirectoryTree;
  private JMenuItem _miRemovePlaylistDirectory;
  private JMenuItem _miRenameSelectedItem;
  private JTree _playlistDirectoryTree;
  private JPanel _playlistPanel;
  private JPopupMenu _playlistTreeRightClickMenu;
  private JButton _refreshMediaDirsButton;
  private JButton _removeMediaDirButton;
  private JMenu recentMenu;
  private JLabel statusLabel;
}
