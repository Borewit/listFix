package listfix.view.controls;

import listfix.config.IMediaLibrary;
import listfix.io.*;
import listfix.io.filters.AudioFileFilter;
import listfix.model.BatchMatchItem;
import listfix.model.EditFilenameResult;
import listfix.model.PlaylistEntryList;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.*;
import listfix.util.ArrayFunctions;
import listfix.util.ExStack;

import listfix.view.IListFixGui;
import listfix.view.dialogs.*;
import listfix.view.support.IPlaylistModifiedListener;
import listfix.view.support.ImageIcons;
import listfix.view.support.ProgressWorker;
import listfix.view.support.ZebraJTable;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class PlaylistEditCtrl extends javax.swing.JPanel
{
  private static final Logger _logger = LogManager.getLogger(PlaylistEditCtrl.class);
  private static final NumberFormat _intFormatter = NumberFormat.getIntegerInstance();
  private static final DataFlavor _playlistEntryListFlavor = new DataFlavor(PlaylistEntryList.class, "PlaylistEntyList");
  private static final Marker markerPlaylistControl = MarkerManager.getMarker("PlaylistCtrl");
  private static final Marker markerRepair = MarkerManager.getMarker("PlaylistCtrl-Repair").setParents(markerPlaylistControl);
  private static final Marker markerRepairWorker = MarkerManager.getMarker("PlaylistCtrl-Repair-Worker").setParents(markerRepair);
  private static final String txtOpenFileLocationSingular = "Open File Location";
  private static final String txtOpenFileLocationPlural = "Open File Locations";
  private final FolderChooser _destDirFileChooser = new FolderChooser();
  private Playlist _playlist;
  protected final IListFixGui listFixGui;

  private final IPlaylistModifiedListener listener = this::onPlaylistModified;

  public PlaylistEditCtrl(IListFixGui listFixGui)
  {
    this.listFixGui = listFixGui;
    initComponents();
    initPlaylistTable();
    initFolderChooser();
    SwingUtilities.updateComponentTreeUI(this);
  }

  protected IPlaylistOptions getPlaylistOptions()
  {
    return this.listFixGui.getApplicationConfiguration().getAppOptions();
  }

  private IMediaLibrary getMediaLibrary()
  {
    return this.listFixGui.getApplicationConfiguration().getMediaLibrary();
  }

  private void onPlaylistModified(Playlist list)
  {
    boolean hasSelected = _uiTable.getSelectedRowCount() > 0;
    _btnReload.setEnabled(list != null && list.isModified());
    _btnSave.setEnabled(_playlist != null);
    _btnUp.setEnabled(_isSortedByFileIx && hasSelected && _uiTable.getSelectedRow() > 0);
    _btnDown.setEnabled(_isSortedByFileIx && hasSelected && _uiTable.getSelectedRow() < _uiTable.getRowCount() - 1);
    _btnPlay.setEnabled(_playlist != null);
    _btnNextMissing.setEnabled(_playlist != null && _playlist.getMissingCount() > 0);
    _btnPrevMissing.setEnabled(_playlist != null && _playlist.getMissingCount() > 0);
    getTableModel().fireTableDataChanged();
  }

  private void addItems()
  {
    JFileChooser chooser = new JFileChooser();
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.addChoosableFileFilter(new AudioFileFilter());
    chooser.setMultiSelectionEnabled(true);
    this.getMediaLibrary().getMediaDirectories().stream().findFirst().ifPresent(mediaDir -> chooser.setCurrentDirectory(new File(mediaDir)));
    if (chooser.showOpenDialog(getParentFrame()) == JFileChooser.APPROVE_OPTION)
    {
      File[] files = chooser.getSelectedFiles();
      Arrays.sort(files);
      if (files.length == 0)
      {
        return;
      }

      showWaitCursor(true);

      final List<Path> pathList = Arrays.stream(files)
        .map(File::toPath).collect(Collectors.toList());

      ProgressWorker<Void, String> worker = new ProgressWorker<>()
      {
        @Override
        protected Void doInBackground() throws IOException
        {
          int selected = _uiTable.getSelectedRow();
          int insertIx = selected >= 0 ? _uiTable.convertRowIndexToModel(_uiTable.getSelectedRow()) : -1;

          if (insertIx >= 0)
          {
            // Adding somewhere in the middle
            int count = _playlist.addAt(insertIx, pathList, this);
            firstIx = insertIx;
            lastIx = firstIx + count - 1;
          }
          else
          {
            // Adding at the end...
            firstIx = Math.max(_playlist.size(), 0);
            int numAdded = _playlist.add(pathList, this);
            lastIx = firstIx + numAdded - 1;
          }

          return null;
        }

        private int firstIx;
        private int lastIx;

        @Override
        protected void done()
        {
          try
          {
            get();
          }
          catch (InterruptedException ex)
          {
            _logger.info("Cancelled");
          }
          catch (ExecutionException ex)
          {
            showWaitCursor(false);
            _logger.error("Add File Error", ex);
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

  public void locateMissingFiles()
  {
    _logger.debug(markerRepair, "Start locateMissingFiles()");
    ProgressWorker<List<Integer>, String> worker = new ProgressWorker<>()
    {
      @Override
      protected List<Integer> doInBackground()
      {
        _logger.debug(markerRepairWorker, "Start repairing in background....");
        List<Integer> result = _playlist.repair(PlaylistEditCtrl.this.getMediaLibrary(), this);
        _logger.debug(markerRepairWorker, "Repair completed.");
        return result;
      }

      @Override
      protected void done()
      {
        _logger.debug(markerRepair, "Handling background done()");
        try
        {
          _logger.debug(markerRepair, "Updating UI-table...");
          _uiTable.clearSelection();
          List<Integer> fixed = this.get();
          for (Integer fixIx : fixed)
          {
            int viewIx = _uiTable.convertRowIndexToView(fixIx);
            _uiTable.addRowSelectionInterval(viewIx, viewIx);
          }
          _logger.debug(markerRepair, "Completed updating UI-table");
        }
        catch (CancellationException | InterruptedException exception)
        {
          _logger.warn(markerRepair, "Repair interrupted", exception);
        }
        catch (ExecutionException ex)
        {
          _logger.error(markerRepair, "Error processing missing files", ex);
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
      RowSorter<? extends javax.swing.table.TableModel> sorter = _uiTable.getRowSorter();
      List<? extends RowSorter.SortKey> keys = sorter.getSortKeys();
      if (keys.size() > 0)
      {
        RowSorter.SortKey key = keys.get(0);
        switch (key.getColumn())
        {
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

      RowSorter<? extends TableModel> sorter = _uiTable.getRowSorter();
      ArrayList<RowSorter.SortKey> keys = new ArrayList<>();
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
      EditFilenameResult response = EditFilenameDialog.showDialog(getParentFrame(), "Edit Filename", true, entry.getTrackFileName());
      if (response.getResultCode() == EditFilenameDialog.OK)
      {
        _playlist.changeEntryFileName(rowIx, response.getFileName());
        //entry.setFileName(response.getFileName());
        getTableModel().fireTableRowsUpdated(rowIx, rowIx);
      }
    }
  }

  private void openPlayListEntryLocation() throws IOException
  {
    for (PlaylistEntry entry : this.getSelectedPlayListEntries().stream().distinct().collect(Collectors.toList()))
    {
      if (entry instanceof FilePlaylistEntry)
      {
        Path parent = ((FilePlaylistEntry) entry).getAbsolutePath().getParent();
        if (parent != null)
        {
          Desktop.getDesktop().open(parent.toFile());
        }
      }
      else if (entry instanceof UriPlaylistEntry)
      {
        Desktop.getDesktop().browse(((UriPlaylistEntry) entry).getURI());
      }
    }
  }

  private List<PlaylistEntry> getSelectedPlayListEntries()
  {
    int[] rows = this._uiTable.getSelectedRows();
    if (rows == null)
    {
      return Collections.emptyList();
    }
    return Arrays.stream(rows).mapToObj(rowIx -> this._playlist.get(rowIx)).collect(Collectors.toList());
  }

  private void findClosestMatches()
  {
    final Collection<String> libraryFiles = this.getMediaLibrary().getNestedMediaFiles();
    ProgressWorker<List<BatchMatchItem>, String> worker = new ProgressWorker<>()
    {
      @Override
      protected List<BatchMatchItem> doInBackground()
      {
        List<Integer> rowList = new ArrayList<>();
        int[] uiRows = _uiTable.getSelectedRows();
        for (int x : uiRows)
        {
          rowList.add(_uiTable.convertRowIndexToModel(x));
        }
        return _playlist.findClosestMatchesForSelectedEntries(rowList, libraryFiles, this);
      }
    };
    ProgressDialog pd = new ProgressDialog(getParentFrame(), true, worker, "Finding closest matches for all selected files...");
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
      _logger.error("Error finding closest matches", ex);
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

  public void bulkFindClosestMatches()
  {
    final Collection<String> libraryFiles = listFixGui.getApplicationConfiguration().getMediaLibrary().getNestedMediaFiles();
    ProgressWorker<List<BatchMatchItem>, String> worker = new ProgressWorker<>()
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
    catch (InterruptedException | ExecutionException ex)
    {
      _logger.error("Error finding closest matches", ex);
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
      chooser.addChoosableFileFilter(new AudioFileFilter());
      chooser.setDialogTitle("Replacing '" + entry.getTrackFileName() + "'");

      File deepest = FileUtils.findDeepestPathToExist(new File(entry.getTrackFolder()));
      if (deepest != null)
      {
        chooser.setCurrentDirectory(deepest);
      }
      else
      {
        this.listFixGui.getApplicationConfiguration().getMediaLibrary().getMediaDirectories().stream().findFirst().ifPresent(mediaDir -> chooser.setCurrentDirectory(new File(mediaDir)));
      }

      if (entry instanceof FilePlaylistEntry)
      {
        chooser.setSelectedFile(((FilePlaylistEntry) entry).getAbsolutePath().toFile());
      }
      if (chooser.showOpenDialog(getParentFrame()) == JFileChooser.APPROVE_OPTION)
      {
        File file = chooser.getSelectedFile();

        // make sure the replacement file is not a playlist
        if (Playlist.isPlaylist(file.toPath()))
        {
          JOptionPane.showMessageDialog(getParentFrame(), new JTransparentTextArea("You cannot replace a file with a playlist file. Use \"Add File\" instead."), "Replace File Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        PlaylistEntry newEntry = new FilePlaylistEntry(file.toPath(), null, _playlist.getFile().toPath());
        _playlist.replace(rowIx, newEntry);
        getTableModel().fireTableRowsUpdated(rowIx, rowIx);
      }
    }

  }

  private void removeDuplicates()
  {
    int dupCount = this._playlist.removeDuplicates();
    if (dupCount > 0)
    {
      getTableModel().fireTableDataChanged();
    }
    String msg = dupCount == 1 ? "Removed 1 duplicate" : String.format("Removed %d duplicates", dupCount);
    JOptionPane.showMessageDialog(getParentFrame(), new JTransparentTextArea(msg), "Duplicates Removed", JOptionPane.INFORMATION_MESSAGE);
  }

  private void removeMissing()
  {
    int count = this._playlist.removeMissing();
    if (count > 0)
    {
      getTableModel().fireTableDataChanged();
    }
    String msg = count == 1 ? "Removed 1 missing entry" : String.format("Removed %d missing entries", count);
    JOptionPane.showMessageDialog(getParentFrame(), new JTransparentTextArea(msg), "Missing Entries Removed", JOptionPane.INFORMATION_MESSAGE);
  }

  private void savePlaylist()
  {
    if (this._playlist.isNew())
    {
      this.listFixGui.showPlaylistSaveAsDialog(this._playlist);
    }
    else
    {
      try
      {
        this.listFixGui.savePlaylist(this._playlist);
      }
      catch (InterruptedException | ExecutionException | IOException ex)
      {
        _logger.error("Save playlist error", ex);

        JOptionPane.showMessageDialog(getParentFrame(),
          new JTransparentTextArea(ExStack.textFormatErrorForUser("Sorry, there was an error saving your playlist.  Please try again, or file a bug report.", ex.getCause())),
          "Save Playlist Error", JOptionPane.ERROR_MESSAGE);
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
      List<Integer> rowList = new ArrayList<>();
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
      JOptionPane.showMessageDialog(getParentFrame(),
        new JTransparentTextArea(ExStack.textFormatErrorForUser("Could not open the selected playlist entries.", ex.getCause())),
        "Playback Error", JOptionPane.ERROR_MESSAGE);
      _logger.error("Playback error", ex);
    }
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   */
  private void initComponents()
  {

    _playlistEntryRightClickMenu = new javax.swing.JPopupMenu();
    _miEditFilename = new javax.swing.JMenuItem();
    _miOpenFileLocation = new javax.swing.JMenuItem();
    _miReplace = new javax.swing.JMenuItem();
    JPopupMenu.Separator jSeparator3 = new JPopupMenu.Separator();
    _miFindClosest = new javax.swing.JMenuItem();
    JPopupMenu.Separator jSeparator4 = new JPopupMenu.Separator();
    JMenuItem _miRemoveDups = new JMenuItem();
    JMenuItem _miRemoveMissing = new JMenuItem();
    JMenuItem _miCopySelectedFiles = new JMenuItem();
    JMenuItem _miNewPlaylistFromSelected = new JMenuItem();
    JToolBar _uiToolbar = new JToolBar();
    _btnSave = new javax.swing.JButton();
    _btnReload = new javax.swing.JButton();
    _btnAdd = new javax.swing.JButton();
    _btnDelete = new javax.swing.JButton();
    JToolBar.Separator jSeparator1 = new JToolBar.Separator();
    _btnUp = new javax.swing.JButton();
    _btnDown = new javax.swing.JButton();
    _btnInvert = new javax.swing.JButton();
    _btnReorder = new javax.swing.JButton();
    JToolBar.Separator jSeparator2 = new JToolBar.Separator();
    _btnMagicFix = new javax.swing.JButton();
    _btnLocate = new javax.swing.JButton();
    _btnPlay = new javax.swing.JButton();
    JToolBar.Separator jSeparator5 = new JToolBar.Separator();
    _btnPrevMissing = new javax.swing.JButton();
    _btnNextMissing = new javax.swing.JButton();
    _uiTableScrollPane = new javax.swing.JScrollPane();
    _uiTable = createTable();

    _miEditFilename.setText("Edit Filename");
    _miEditFilename.addActionListener(evt -> this.editFilenames());
    _playlistEntryRightClickMenu.add(_miEditFilename);

    _miOpenFileLocation = new javax.swing.JMenuItem();
    _miOpenFileLocation.setText(txtOpenFileLocationSingular);
    _miOpenFileLocation.addActionListener(evt -> {
      try
      {
        openPlayListEntryLocation();
      }
      catch (IOException e)
      {
        _logger.error("Failed to open playlist-entry location", e);
        throw new RuntimeException(e);
      }
    });
    _playlistEntryRightClickMenu.add(_miOpenFileLocation);

    _miReplace.setText("Replace Selected Entry");
    _miReplace.addActionListener(evt -> replaceSelectedEntries());
    _playlistEntryRightClickMenu.add(_miReplace);
    _playlistEntryRightClickMenu.add(jSeparator3);

    _miFindClosest.setText("Find Closest Matches");
    _miFindClosest.addActionListener(evt -> findClosestMatches());
    _playlistEntryRightClickMenu.add(_miFindClosest);
    _playlistEntryRightClickMenu.add(jSeparator4);

    _miRemoveDups.setText("Remove Duplicates");
    _miRemoveDups.addActionListener(evt -> removeDuplicates());
    _playlistEntryRightClickMenu.add(_miRemoveDups);

    _miRemoveMissing.setText("Remove Missing");
    _miRemoveMissing.addActionListener(evt -> removeMissing());
    _playlistEntryRightClickMenu.add(_miRemoveMissing);

    _miCopySelectedFiles.setText("Copy Selected Items To...");
    _miCopySelectedFiles.setToolTipText("");
    _miCopySelectedFiles.addActionListener(evt -> _miCopySelectedFilesActionPerformed());
    _playlistEntryRightClickMenu.add(_miCopySelectedFiles);

    _miNewPlaylistFromSelected.setText("Create New Playlist with Selected Items...");
    _miNewPlaylistFromSelected.addActionListener(evt -> _miNewPlaylistFromSelectedActionPerformed());
    _playlistEntryRightClickMenu.add(_miNewPlaylistFromSelected);

    setLayout(new java.awt.BorderLayout());

    _uiToolbar.setFloatable(false);
    _uiToolbar.setRollover(true);

    _btnSave = makeButton("save.gif");
    _btnSave.setToolTipText("Save");
    _btnSave.setEnabled(_playlist != null);
    _btnSave.addActionListener(evt -> savePlaylist());
    _uiToolbar.add(_btnSave);

    _btnReload = makeButton("gtk-refresh.png");
    _btnReload.setToolTipText("Reload");
    _btnReload.setEnabled(_playlist == null ? false : _playlist.isModified());
    _btnReload.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent evt)
      {
        reloadPlaylist();
      }
    });
    _uiToolbar.add(_btnReload);

    _btnAdd = makeButton("edit-add.gif");
    _btnAdd.setToolTipText("Append/Insert");
    _btnAdd.addActionListener(ignore -> this.addItems());
    _uiToolbar.add(_btnAdd);

    _btnDelete = makeButton("edit-delete.gif");
    _btnDelete.setToolTipText("Delete");
    _btnDelete.addActionListener(event -> this.deleteSelectedRows());
    _uiToolbar.add(_btnDelete);
    _uiToolbar.add(jSeparator1);

    _btnUp = makeButton("arrow-up.gif");
    _btnUp.setToolTipText("Move Up");
    _btnUp.addActionListener(event -> this.moveSelectedRowsUp());
    _uiToolbar.add(_btnUp);

    _btnDown = makeButton("arrow_down.gif");
    _btnDown.setToolTipText("Move Down");
    _btnDown.addActionListener(event -> this.moveSelectedRowsDown());
    _uiToolbar.add(_btnDown);

    _btnInvert = makeButton("invert.png");
    _btnInvert.setToolTipText("Inverts the current selection");
    _btnInvert.addActionListener(this::_btnInvertActionPerformed);
    _uiToolbar.add(_btnInvert);

    _btnReorder = makeButton("reorder.png");
    _btnReorder.setToolTipText("Change Playlist Order");
    _btnReorder.addActionListener(event -> this.reorderList());
    _uiToolbar.add(_btnReorder);
    _uiToolbar.add(jSeparator2);

    _btnMagicFix = makeButton("magic-fix.png");
    _btnMagicFix.setToolTipText("Fix Everything");
    _btnMagicFix.setEnabled(_playlist != null && _playlist.getFile().exists());
    _btnMagicFix.addActionListener(evt -> {
      locateMissingFiles();
      bulkFindClosestMatches();
    });
    _uiToolbar.add(_btnMagicFix);

    _btnLocate = makeButton("edit-find.gif");
    _btnLocate.setToolTipText("Find Exact Matches");
    _btnLocate.setEnabled(_playlist != null && _playlist.getFile().exists());
    _btnLocate.addActionListener(evt -> this.locateMissingFiles());
    _uiToolbar.add(_btnLocate);

    _btnPlay = makeButton("play.png");
    _btnPlay.setToolTipText("Play Selected");
    _btnPlay.setEnabled(_playlist != null);
    _btnPlay.addActionListener(this::onBtnPlayActionPerformed);
    _uiToolbar.add(_btnPlay);
    _uiToolbar.add(jSeparator5);

    _btnPrevMissing = makeButton("prev.png");
    _btnPrevMissing.setToolTipText("Previous Missing Entry");
    _btnPrevMissing.setEnabled(_playlist != null && _playlist.getMissingCount() > 0);
    _btnPrevMissing.addActionListener(evt -> _btnPrevMissingActionPerformed());
    _uiToolbar.add(_btnPrevMissing);

    _btnNextMissing = makeButton("next.png");
    _btnNextMissing.setToolTipText("Next Missing Entry");
    _btnNextMissing.setEnabled(_playlist != null && _playlist.getMissingCount() > 0);
    _btnNextMissing.addActionListener(evt -> _btnNextMissingActionPerformed());
    _uiToolbar.add(_btnNextMissing);

    add(_uiToolbar, java.awt.BorderLayout.PAGE_START);

    _uiTableScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    _uiTable.setAutoCreateRowSorter(true);
    _uiTable.setModel(new PlaylistTableModel());
    _uiTable.setDragEnabled(true);
    _uiTable.setFillsViewportHeight(true);
    _uiTable.setGridColor(new java.awt.Color(153, 153, 153));
    _uiTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
    _uiTable.setRowHeight(20);
    _uiTable.getTableHeader().setReorderingAllowed(false);
    _uiTable.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(java.awt.event.MouseEvent evt)
      {
        _uiTableMousePressed(evt);
      }
    });
    _uiTable.addKeyListener(new java.awt.event.KeyAdapter()
    {
      public void keyPressed(java.awt.event.KeyEvent evt)
      {
        _uiTableKeyPressed(evt);
      }
    });
    _uiTableScrollPane.setViewportView(_uiTable);

    add(_uiTableScrollPane, java.awt.BorderLayout.CENTER);
  }

  private JButton makeButton(String iconName)
  {
    JButton button = new JButton();
    URL url = getClass().getResource("/images/" + iconName);
    if (url == null)
      throw new RuntimeException(String.format("Image resource not found for: %s", iconName));
    button.setIcon(new ImageIcon(url));
    button.setEnabled(false);
    button.setFocusable(false);
    button.setHorizontalTextPosition(SwingConstants.CENTER);
    button.setMaximumSize(new Dimension(31, 31));
    button.setMinimumSize(new Dimension(31, 31));
    button.setPreferredSize(new Dimension(31, 31));
    button.setVerticalTextPosition(SwingConstants.BOTTOM);
    return button;
  }

  private void onBtnPlayActionPerformed(ActionEvent evt)
  {
    if (_uiTable.getSelectedRowCount() > 0)
    {
      playSelectedEntries();
    }
    else
    {
      if (_playlist.isModified())
      {
        // this plays all entries if nothing is selected, and plays what the user has in memory
        playSelectedEntries();
      }
      else
      {
        _playlist.play();
      }
    }
  }

  private void _uiTableMousePressed(MouseEvent evt)
  {
    if (SwingUtilities.isLeftMouseButton(evt))
    {
      int currentlySelectedRow = _uiTable.rowAtPoint(evt.getPoint());
      if (evt.getClickCount() == 2)
      {
        if (_uiTable.getSelectedRowCount() == 1 && !_playlist.get(_uiTable.convertRowIndexToModel(_uiTable.getSelectedRow())).isFound()
          && !_playlist.get(_uiTable.convertRowIndexToModel(_uiTable.getSelectedRow())).isURL())
        {
          findClosestMatches();
        }
        else if (currentlySelectedRow != -1 && (evt.getModifiers() & ActionEvent.CTRL_MASK) <= 0)
        {
          playSelectedEntries();
        }
      }
    }
  }

  public void scrollCellToVisible(JTable table, int row, int column)
  {
    table.scrollRectToVisible(table.getCellRect(row, column, true));
  }

  public void reloadPlaylist()
  {
    if (_playlist.isModified())
    {
      Object[] options =
        {
          "Discard Changes and Reload", "Cancel"
        };
      int rc = JOptionPane.showOptionDialog(this.getParentFrame(), new JTransparentTextArea("The current list is modified, do you really want to discard these changes and reload from source?"), "Confirm Reload",
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
      if (rc == JOptionPane.NO_OPTION)
      {
        return;
      }
      else
      {
        showWaitCursor(true);
        ProgressWorker<Void, String> worker = new ProgressWorker<>()
        {
          @Override
          protected Void doInBackground() throws IOException
          {
            this.setMessage("Please wait while your playlist is reloaded from disk.");
            _playlist.reload(this);
            return null;
          }

          @Override
          protected void done()
          {
            try
            {
              get();
            }
            catch (CancellationException | InterruptedException ex)
            {
              return;
            }
            catch (ExecutionException ex)
            {
              showWaitCursor(false);
              _logger.error("Reload error", ex);
              JOptionPane.showMessageDialog(PlaylistEditCtrl.this.getParentFrame(), ex, "Reload Error", JOptionPane.ERROR_MESSAGE);
              return;
            }

            getTableModel().fireTableDataChanged();
            resizeAllColumns();
          }
        };

        boolean textOnly = false;
        if (_playlist.getType() == PlaylistType.ITUNES || _playlist.getType() == PlaylistType.XSPF)
        {
          // Can't show a progress dialog for these as we have no way to track them at present.
          textOnly = true;
        }
        ProgressDialog pd = new ProgressDialog(this.getParentFrame(), true, worker, "Reloading '" + _playlist.getFilename() + "'...", textOnly, true);
        pd.setVisible(true);

        showWaitCursor(false);
      }
    }
  }

  private void _uiTableKeyPressed(KeyEvent evt)
  {
    int keyVal = evt.getKeyCode();
    if (keyVal == KeyEvent.VK_DELETE && _playlist.getType() != PlaylistType.ITUNES)
    {
      deleteSelectedRows();
    }
    else if (keyVal == KeyEvent.VK_ENTER)
    {
      playSelectedEntries();
    }
  }

  private void _btnNextMissingActionPerformed()
  {
    if (_playlist.size() > 0)
    {
      int row = _uiTable.getSelectedRow();
      int modelRow;

      // search from the selected row (or the beginning of the list)
      // for the next missing entry, if found, jump to it and bail out.
      for (int i = (row < 0 ? 0 : row + 1); i < _playlist.size(); i++)
      {
        modelRow = _uiTable.convertRowIndexToModel(i);
        if (entryNotFound(modelRow))
        {
          scrollEditorRowIntoView(i);
          return;
        }
      }

      // if we made it this far, and we didn't search from the beginning
      // of the list, loop back around...
      if (row >= 0)
      {
        for (int i = 0; i <= row; i++)
        {
          modelRow = _uiTable.convertRowIndexToModel(i);
          if (entryNotFound(modelRow))
          {
            scrollEditorRowIntoView(i);
            return;
          }
        }
      }
    }
  }

  private void scrollEditorRowIntoView(int i)
  {
    _uiTable.setRowSelectionInterval(i, i);
    scrollCellToVisible(_uiTable, i, 0);
  }

  private boolean entryNotFound(int modelRow)
  {
    return !_playlist.get(modelRow).isFound() && !_playlist.get(modelRow).isURL();
  }

  private void _btnPrevMissingActionPerformed()
  {
    if (_playlist.size() > 0)
    {
      int row = _uiTable.getSelectedRow();
      int modelRow = 0;

      // search from the selected row (or the beginning of the list)
      // for the next missing entry, if found, jump to it and bail out.
      for (int i = (row < 0 ? _playlist.size() - 1 : row - 1); i >= 0; i--)
      {
        modelRow = _uiTable.convertRowIndexToModel(i);
        if (entryNotFound(modelRow))
        {
          scrollEditorRowIntoView(i);
          return;
        }
      }

      // if we made it this far, and we didn't search from the end
      // of the list, loop back around...
      if (row >= 0)
      {
        for (int i = _playlist.size() - 1; i >= row; i--)
        {
          modelRow = _uiTable.convertRowIndexToModel(i);
          if (entryNotFound(modelRow))
          {
            scrollEditorRowIntoView(i);
            return;
          }
        }
      }
    }
  }

  private void _btnInvertActionPerformed(ActionEvent evt)
  {
    int[] selectedIndexs = _uiTable.getSelectedRows();
    _uiTable.selectAll();

    for (int i = 0; i < _uiTable.getRowCount(); i++)
    {
      for (int selectedIndex : selectedIndexs)
      {
        if (selectedIndex == i)
        {
          _uiTable.removeRowSelectionInterval(i, i);
          break;
        }
      }
    }
  }

  private void _miCopySelectedFilesActionPerformed()
  {
    int response = _destDirFileChooser.showOpenDialog(getParentFrame());
    if (response == JFileChooser.APPROVE_OPTION)
    {
      try
      {
        final File destDir = _destDirFileChooser.getSelectedFile();

        ProgressWorker<Void, String> worker = new ProgressWorker<>()
        {
          @Override
          protected Void doInBackground()
          {
            List<Integer> rowList = new ArrayList<>();
            int[] uiRows = _uiTable.getSelectedRows();
            for (int x : uiRows)
            {
              rowList.add(_uiTable.convertRowIndexToModel(x));
            }
            _playlist.copySelectedEntries(rowList, destDir, this);
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
      catch (InterruptedException | ExecutionException e)
      {
        JOptionPane.showMessageDialog(getParentFrame(),
          new JTransparentTextArea(ExStack.textFormatErrorForUser("An error has occured, 1 or more files were not copied.", e.getCause())),
          "Copy Error", JOptionPane.ERROR_MESSAGE);
        _logger.error("Error", e);
      }
    }
  }

  private void _miNewPlaylistFromSelectedActionPerformed()
  {

    List<Integer> rowList = new ArrayList<>();
    int[] uiRows = _uiTable.getSelectedRows();
    for (int x : uiRows)
    {
      rowList.add(_uiTable.convertRowIndexToModel(x));
    }
    int[] rows = ArrayFunctions.integerListToArray(rowList);
    // Creating the playlist and copying the entries gives us a new list w/ the "Untitled-X" style name.
    try
    {
      Playlist sublist = new Playlist(this.getPlaylistOptions());
      sublist.addAllAt(0, _playlist.getSublist(rows).getEntries());
      this.listFixGui.openNewTabForPlaylist(sublist);
    }
    catch (Exception e)
    {
      throw new RuntimeException("Failed to create new playlist", e);
    }

  }

  private javax.swing.JButton _btnAdd;
  private javax.swing.JButton _btnDelete;
  private javax.swing.JButton _btnDown;
  private javax.swing.JButton _btnInvert;
  private javax.swing.JButton _btnLocate;
  private javax.swing.JButton _btnMagicFix;
  private javax.swing.JButton _btnNextMissing;
  private javax.swing.JButton _btnPlay;
  private javax.swing.JButton _btnPrevMissing;
  private javax.swing.JButton _btnReload;
  private javax.swing.JButton _btnReorder;
  private javax.swing.JButton _btnSave;
  private javax.swing.JButton _btnUp;
  private javax.swing.JMenuItem _miEditFilename;
  private javax.swing.JMenuItem _miOpenFileLocation;
  private javax.swing.JMenuItem _miFindClosest;
  private javax.swing.JMenuItem _miReplace;
  private javax.swing.JPopupMenu _playlistEntryRightClickMenu;
  private listfix.view.support.ZebraJTable _uiTable;
  private javax.swing.JScrollPane _uiTableScrollPane;

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

    _btnAdd.setEnabled(hasPlaylist && _playlist.getType() != PlaylistType.ITUNES);
    _btnLocate.setEnabled(hasPlaylist);
    _btnMagicFix.setEnabled(hasPlaylist);
    _btnReorder.setEnabled(hasPlaylist && _playlist.getType() != PlaylistType.ITUNES && _playlist.size() > 1);
    _btnReload.setEnabled(hasPlaylist && _playlist.isModified());
    _btnPlay.setEnabled(hasPlaylist && _playlist.getType() != PlaylistType.ITUNES);
    _btnNextMissing.setEnabled(hasPlaylist && _playlist.getMissingCount() > 0);
    _btnPrevMissing.setEnabled(hasPlaylist && _playlist.getMissingCount() > 0);
    _btnSave.setEnabled(_playlist != null);

    if (_playlist != null && !_playlist.isEmpty())
    {
      resizeAllColumns();
    }

    if (_playlist != null)
    {
      _playlist.addModifiedListener(listener);
    }

    _uiTable.setDragEnabled(_playlist.getType() != PlaylistType.ITUNES);
  }

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
    RowSorter<? extends TableModel> sorter = _uiTable.getRowSorter();
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
          else if (rowIx >= 0 && rowIx < _playlist.size() && (colIx == 3))
          {
            PlaylistEntry entry = _playlist.get(rowIx);
            if (entry instanceof UriPlaylistEntry)
            {
              // Show the URL
              return ((UriPlaylistEntry) entry).getURI().toString();
            }
            else if (entry instanceof FilePlaylistEntry)
            {
              FilePlaylistEntry filePlaylistEntry = (FilePlaylistEntry) entry;
              if (filePlaylistEntry.isRelative())
              {
              /*
              Shows full relative path
              if (!entry.getPath().isEmpty())
              {
                return entry.getPath() + entry.getFileName();
              }
              else
              {
                return "." + Constants.FS + entry.getFileName();
              }
              */

                // Show the full absolute path of a relative entry
                return FilenameUtils.normalize(filePlaylistEntry.getAbsolutePath().toString());
              }
              else
              {
                // Show the file location
                return FilenameUtils.normalize(entry.getTrackFolder() + Constants.FS + entry.getTrackFileName());
              }
            }
            else
            {
              throw new RuntimeException("Invalid playlist entry");
            }
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

    // resize columns
    TableColumnModel cm = _uiTable.getColumnModel();
    cm.getColumn(0).setPreferredWidth(20);
    cm.getColumn(1).setPreferredWidth(20);
    cm.getColumn(2).setPreferredWidth(100);
    cm.getColumn(3).setPreferredWidth(100);
    _uiTable.setFillerColumnWidth(_uiTableScrollPane);

    // addAt selection listener
    _uiTable.getSelectionModel().addListSelectionListener(e -> {
      if (e.getValueIsAdjusting())
      {
        return;
      }

      boolean hasSelected = _uiTable.getSelectedRowCount() > 0;
      _btnDelete.setEnabled(hasSelected && _playlist.getType() != PlaylistType.ITUNES);
      _btnUp.setEnabled(_isSortedByFileIx && hasSelected && _playlist.getType() != PlaylistType.ITUNES && _uiTable.getSelectedRow() > 0);
      _btnDown.setEnabled(_isSortedByFileIx && hasSelected && _playlist.getType() != PlaylistType.ITUNES && _uiTable.getSelectedRow() < _uiTable.getRowCount() - 1);
      _btnPlay.setEnabled(_playlist != null && _playlist.getType() != PlaylistType.ITUNES && (_uiTable.getSelectedRow() < 0 || (_uiTable.getSelectedRows().length > 0 && selectedRowsContainFoundEntry())));
      _btnReload.setEnabled(_playlist != null && _playlist.isModified());
      _btnSave.setEnabled(_playlist != null);
      _btnNextMissing.setEnabled(_playlist != null && _playlist.getMissingCount() > 0);
      _btnPrevMissing.setEnabled(_playlist != null && _playlist.getMissingCount() > 0);
      _btnReorder.setEnabled(_playlist != null && _playlist.getType() != PlaylistType.ITUNES && _playlist.size() > 1);
      _btnInvert.setEnabled(hasSelected);
      if (_isSortedByFileIx)
      {
        refreshAddTooltip(hasSelected);
      }
    });

    // addAt sort listener
    RowSorter sorter = _uiTable.getRowSorter();
    sorter.addRowSorterListener(e -> {
      RowSorter sorter1 = e.getSource();
      List<RowSorter.SortKey> keys = sorter1.getSortKeys();
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
    });

    // set sort to #
    ArrayList<RowSorter.SortKey> keys = new ArrayList<>();
    keys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
    sorter.setSortKeys(keys);

    // addAt popup menu to list, handle's right click actions.
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
              _miOpenFileLocation.setText(txtOpenFileLocationPlural);
            }
            else
            {
              _miEditFilename.setText("Edit Filename");
              _miReplace.setText("Replace Selected Entry");
              _miOpenFileLocation.setText(txtOpenFileLocationSingular);
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

    // drag-n-drop support for the insertion of playlists or entries
    _uiTable.setTransferHandler(new TransferHandler()
    {
      @Override
      public boolean canImport(TransferHandler.TransferSupport info)
      {
        if (info.isDataFlavorSupported(_playlistEntryListFlavor)
          || info.isDataFlavorSupported(DataFlavor.stringFlavor)
          || info.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        {
          JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
          if (dl.getRow() == -1)
          {
            return false;
          }

          return _isSortedByFileIx;
        }

        return false;
      }

      @Override
      public boolean importData(TransferHandler.TransferSupport info)
      {
        if (!info.isDrop())
        {
          return false;
        }

        final JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();

        if (info.isDataFlavorSupported(_playlistEntryListFlavor))
        {
          // This is the flavor we handle when the user is dragging entries around that are already part of the list
          return HandlePlaylistEntryFlavor(info, dl);
        }
        else if (info.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        {
          // This is the flavor we handle when the user drags any file in from a Windows OS
          return HandleFileListFlavor(info, dl, PlaylistEditCtrl.this.listFixGui);
        }
        else
        {
          // This is the flavor we handle when the user drags playlists over from the playlist panel, or when dragging in any file from linux
          return HandleStringFlavor(info, dl, PlaylistEditCtrl.this.listFixGui);
        }
      }

      private boolean HandlePlaylistEntryFlavor(TransferSupport info, final JTable.DropLocation dl)
      {
        // Get the entries that are being dropped.
        Transferable t = info.getTransferable();
        try
        {
          PlaylistEntryList data = (PlaylistEntryList) t.getTransferData(_playlistEntryListFlavor);
          List<PlaylistEntry> entries = data.getList();
          int removedAt;
          int insertAtUpdated = dl.getRow();
          int i = 0;
          for (PlaylistEntry entry : entries)
          {
            // remove them all, we'll re-addAt them in bulk...
            removedAt = _playlist.remove(entry);

            // Was the thing we just removed above where we're inserting?
            if (removedAt < insertAtUpdated)
            {
              insertAtUpdated--;
            }
            i++;
          }

          _playlist.addAllAt(insertAtUpdated, entries);

          return true;
        }
        catch (UnsupportedFlavorException | IOException e)
        {
          // Don't bother logging, could overlog if people try to drag the wrong stuff.
          return false;
        }
      }

      private boolean HandleFileListFlavor(TransferSupport info, final JTable.DropLocation dl, IListFixGui parent)
      {
        int result = JOptionPane.showOptionDialog(getParentFrame(), new JTransparentTextArea("You dragged one or more playlists into this list, would you like to insert them or open them for repair?"),
          "Insert or Open?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Insert", "Open", "Cancel"}, "Insert");

        if (result == JOptionPane.YES_OPTION)
        {
          // Insert
          Transferable t = info.getTransferable();
          DataFlavor[] flavors = t.getTransferDataFlavors();
          for (DataFlavor flavor : flavors)
          {
            try
            {
              final List<Path> list = (List<Path>) t.getTransferData(flavor);
              ProcessFileListDrop(list, dl);
              resizeAllColumns();
              return true;
            }
            catch (UnsupportedFlavorException | IOException ex)
            {
              _logger.warn(ex);
            }
          }
          return false;
        }
        else if (result == JOptionPane.NO_OPTION && parent != null)
        {
          // Open
          Transferable t = info.getTransferable();
          DataFlavor[] flavors = t.getTransferDataFlavors();
          for (DataFlavor flavor : flavors)
          {
            try
            {
              final List<Path> list = (List<Path>) t.getTransferData(flavor);
              OpenFileListDrop(list, parent);
              return true;
            }
            catch (UnsupportedFlavorException | IOException ex)
            {
              _logger.warn(ex);
            }
          }
          return false;
        }
        return false;
      }

      private void ProcessFileListDrop(List<Path> list, final JTable.DropLocation dl) throws IOException
      {
        int insertAt = dl.getRow();
        for (Object list1 : list)
        {
          final Path tempFile = (Path) list1;
          if (tempFile != null)
          {
            if (Playlist.isPlaylist(tempFile))
            {
              insertAt += ProcessDroppedPlaylist(tempFile, insertAt);
            }
            else if (Files.isDirectory(tempFile))
            {
              List<Path> filesToInsert = PlaylistScanner.getAllPlaylists(tempFile);
              for (Path f : filesToInsert)
              {
                insertAt += ProcessDroppedPlaylist(f, insertAt);
              }
            }
            else if (FileUtils.isMediaFile(tempFile))
            {
              // addAt it to the playlist!
              try
              {
                _playlist.addAt(insertAt, Collections.singleton(tempFile), null);
              }
              catch (Exception ex)
              {
                _logger.warn(ex);
              }
              insertAt++;
            }
          }
        }
      }

      private void OpenFileListDrop(List<Path> list, IListFixGui parent) throws IOException
      {
        for (Path file : list)
        {
          if (file != null)
          {
            if (Playlist.isPlaylist(file))
            {
              parent.openPlaylist(file);
            }
            else if (Files.isDirectory(file))
            {
              List<Path> filesToInsert = PlaylistScanner.getAllPlaylists(file);
              for (Path f : filesToInsert)
              {
                parent.openPlaylist(f);
              }
            }
          }
        }
      }

      private int ProcessDroppedPlaylist(final Path tempFile, int insertAt)
      {
        final int currentInsertPoint = insertAt;

        ProgressWorker<Playlist, String> worker = new ProgressWorker<>()
        {
          @Override
          protected Playlist doInBackground() throws Exception
          {
            Playlist list = PlaylistFactory.getPlaylist(tempFile, this, PlaylistEditCtrl.this.getPlaylistOptions());
            if (PlaylistEditCtrl.this.listFixGui.getApplicationConfiguration().getAppOptions().getAutoLocateEntriesOnPlaylistLoad())
            {
              list.repair(PlaylistEditCtrl.this.getMediaLibrary(), this);
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
              _playlist.addAllAt(currentInsertPoint, list.getEntries());
            }
            catch (CancellationException ex)
            {
            }
            catch (InterruptedException | ExecutionException ex)
            {
              JOptionPane.showMessageDialog(PlaylistEditCtrl.this.getParentFrame(),
                new JTransparentTextArea(ExStack.textFormatErrorForUser("There was a problem opening the file you selected, are you sure it was a playlist?", ex.getCause())),
                "Open Playlist Error", JOptionPane.ERROR_MESSAGE);
              _logger.error("Open playlist error", ex);
            }
          }
        };
        String filename = tempFile.toString();
        ProgressDialog pd = new ProgressDialog(getParentFrame(), true, worker, "Loading '" + (filename.length() > 70 ? filename.substring(0, 70) : filename) + "'...");
        int plistSize = _playlist.size();
        pd.setVisible(true);
        return _playlist.size() - plistSize;
      }

      private boolean HandleStringFlavor(TransferSupport info, final JTable.DropLocation dl, IListFixGui parent)
      {
        int result = JOptionPane.showOptionDialog(getParentFrame(), new JTransparentTextArea("You dragged one or more playlists into this list, would you like to insert them or open them for repair?"),
          "Insert or Open?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Insert", "Open", "Cancel"}, "Insert");

        if (result == JOptionPane.YES_OPTION)
        {
          Transferable t = info.getTransferable();
          DataFlavor[] flavors = t.getTransferDataFlavors();
          for (DataFlavor flavor : flavors)
          {
            try
            {
              if (t.getTransferData(flavor) instanceof String)
              {
                // In this case, it's a magically delicious string coming from the playlist panel
                String input = (String) t.getTransferData(flavor);
                List<String> paths = StringArrayListSerializer.deserialize(input);
                // Turn this into a list of files, and reuse the processing code above
                List<Path> files = paths.stream().map(Path::of).collect(Collectors.toList());
                ProcessFileListDrop(files, dl);
              }
              else
              {
                if (t.getTransferData(flavor) instanceof InputStreamReader)
                {
                  List<Path> filesToProcess = new ArrayList<>();
                  try (InputStreamReader reader = (InputStreamReader) t.getTransferData(flavor); BufferedReader temp = new BufferedReader(reader))
                  {
                    String filePath = temp.readLine();
                    while (filePath != null && !filePath.isEmpty())
                    {
                      filesToProcess.add(Path.of(new URI(filePath)));
                      filePath = temp.readLine();
                    }
                  }
                  Collections.sort(filesToProcess);
                  ProcessFileListDrop(filesToProcess, dl);
                  // In the linux case, we need to stop here, because the other flavors are different representations of the data we just processed...
                  return true;
                }
              }
            }
            catch (UnsupportedFlavorException | IOException | ClassNotFoundException | URISyntaxException ex)
            {
              _logger.error("Error", ex);
              return false;
            }
          }
          return true;
        }
        else if (result == JOptionPane.NO_OPTION && parent != null)
        {
          // We're opening these items rather than inserting them into our playlist...
          Transferable t = info.getTransferable();
          DataFlavor[] flavors = t.getTransferDataFlavors();
          for (DataFlavor flavor : flavors)
          {
            try
            {
              if (t.getTransferData(flavor) instanceof String)
              {
                // In this case, it's a magically delicious string coming from the playlist panel
                String input = (String) t.getTransferData(flavor);
                List<String> paths = StringArrayListSerializer.deserialize(input);
                List<Path> files = paths.stream().map(Path::of).collect(Collectors.toList());
                OpenFileListDrop(files, parent);
              }
              else
              {
                if (t.getTransferData(flavor) instanceof InputStreamReader)
                {
                  List<Path> filesToProcess = new ArrayList<>();
                  try (InputStreamReader reader = (InputStreamReader) t.getTransferData(flavor); BufferedReader temp = new BufferedReader(reader))
                  {
                    String filePath = temp.readLine();
                    while (filePath != null && !filePath.isEmpty())
                    {
                      filesToProcess.add(Path.of(new URI(filePath)));
                    }
                    OpenFileListDrop(filesToProcess, parent);
                  }
                  // In the linux case, we need to stop here, because the other flavors are different representations of the data we just processed...
                  return true;
                }
              }
            }
            catch (UnsupportedFlavorException | IOException | ClassNotFoundException | URISyntaxException ex)
            {
              _logger.error("Error", ex);
              return false;
            }
          }
          return true;
        }
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
            _logger.error("Error", ex);
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

  private void initFolderChooser()
  {
    _destDirFileChooser.setDialogTitle("Choose a destination directory...");
  }

  private class PlaylistTableModel extends AbstractTableModel
  {
    @Override
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

    @Override
    public int getColumnCount()
    {
      return 4;
    }

    @Override
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
            return ImageIcons.IMG_URL;
          }
          else if (entry.isFixed())
          {
            return ImageIcons.IMG_FIXED;
          }
          else if (entry.isFound())
          {
            return ImageIcons.IMG_FOUND;
          }
          else
          {
            return ImageIcons.IMG_MISSING;
          }
        case 2:
          return entry.getTrackFileName();
        case 3:
          return entry.getTrackFolder();
        default:
          return null;
      }
    }

    @Override
    public String getColumnName(int column)
    {
      return switch (column)
        {
          case 0 -> "#";
          case 1 -> "";
          case 2 -> "File Name";
          case 3 -> "Location";
          default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
      return switch (columnIndex)
        {
          case 0 -> Integer.class;
          case 1 -> ImageIcon.class;
          default -> Object.class;
        };
    }
  }

  private static class IntRenderer extends DefaultTableCellRenderer
  {
    IntRenderer()
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
