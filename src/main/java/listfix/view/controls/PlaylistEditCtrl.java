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

package listfix.view.controls;

import listfix.config.IMediaLibrary;
import listfix.io.*;
import listfix.io.filters.AudioFileFilter;
import listfix.io.filters.ExtensionFilter;
import listfix.model.BatchMatchItem;
import listfix.model.EditFilenameResult;
import listfix.model.PlaylistEntryList;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.*;
import listfix.util.ArrayFunctions;
import listfix.util.ExStack;
import listfix.view.GUIScreen;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
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
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * @author jcaron
 */
public class PlaylistEditCtrl extends javax.swing.JPanel
{
  private static final Logger _logger = LogManager.getLogger(PlaylistEditCtrl.class);
  private static final NumberFormat _intFormatter = NumberFormat.getIntegerInstance();
  private static final DataFlavor _playlistEntryListFlavor = new DataFlavor(PlaylistEntryList.class, "PlaylistEntyList");

  private static final DataFlavor _playlistFlavor = new DataFlavor(Playlist.class, "Playlist");
  private static final Marker markerPlaylistControl = MarkerManager.getMarker("PlaylistCtrl");
  private static final Marker markerRepair = MarkerManager.getMarker("PlaylistCtrl-Repair").setParents(markerPlaylistControl);
  private static final Marker markerRepairWorker = MarkerManager.getMarker("PlaylistCtrl-Repair-Worker").setParents(markerRepair);
  private FolderChooser _destDirFileChooser = new FolderChooser();

  private int currentlySelectedRow = 0;
  private Playlist _playlist;

  private GUIScreen mainWindow;

  private final IPlaylistModifiedListener listener = new IPlaylistModifiedListener()
  {
    @Override
    public void playlistModified(Playlist list)
    {
      onPlaylistModified(list);
    }
  };

  public PlaylistEditCtrl()
  {
    initComponents();
    initPlaylistTable(null);
    initFolderChooser();
    SwingUtilities.updateComponentTreeUI(this);
  }

  /**
   *
   */
  public PlaylistEditCtrl(GUIScreen mainWindow)
  {
    this.mainWindow = mainWindow;
    initComponents();
    initPlaylistTable(mainWindow);
    initFolderChooser();
    SwingUtilities.updateComponentTreeUI(this);
  }

  protected IPlaylistOptions getPlaylistOptions()
  {
    return this.mainWindow.getOptions();
  }

  private IMediaLibrary getMediaLibrary()
  {
    return this.mainWindow.getMediaLibrary();
  }

  private void onPlaylistModified(Playlist list)
  {
    boolean hasSelected = _uiTable.getSelectedRowCount() > 0;
    _btnReload.setEnabled(list == null ? false : list.isModified());
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
    this.getMediaLibrary().getDirectories().stream().findFirst().ifPresent(mediaDir -> chooser.setCurrentDirectory(new File(mediaDir)));
    if (chooser.showOpenDialog(getParentFrame()) == JFileChooser.APPROVE_OPTION)
    {
      File[] tempFileList = chooser.getSelectedFiles();
      Arrays.sort(tempFileList);
      final File[] files = tempFileList;
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
            // Adding somewhere in the middle
            int count = _playlist.addAt(insertIx, files, this);
            firstIx = insertIx;
            lastIx = firstIx + count - 1;
          }
          else
          {
            // Adding at the end...
            firstIx = _playlist.size() > 0 ? _playlist.size() : 0;
            int numAdded = _playlist.add(files, this);
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
    ProgressWorker<List<Integer>, Void> worker = new ProgressWorker<>()
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
          List<Integer> fixed = get();
          for (Integer fixIx : fixed)
          {
            int viewIx = _uiTable.convertRowIndexToView(fixIx.intValue());
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

      RowSorter sorter = _uiTable.getRowSorter();
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
    final Collection<String> libraryFiles = mainWindow.getMediaLibrary().getNestedMediaFiles();
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
      chooser.setDialogTitle("Replacing '" + entry.getFileName() + "'");

      File deepest = FileUtils.findDeepestPathToExist(new File(entry.getPath()));
      if (deepest != null)
      {
        chooser.setCurrentDirectory(deepest);
      }
      else
      {
        this.mainWindow.getMediaLibrary().getDirectories().stream().findFirst().ifPresent(mediaDir -> chooser.setCurrentDirectory(new File(mediaDir)));
      }

      if (entry instanceof FilePlaylistEntry)
      {
        chooser.setSelectedFile(((FilePlaylistEntry) entry).getAbsolutePath().toFile());
      }
      if (chooser.showOpenDialog(getParentFrame()) == JFileChooser.APPROVE_OPTION)
      {
        File file = chooser.getSelectedFile();

        // make sure the replacement file is not a playlist
        if (Playlist.isPlaylist(file, PlaylistEditCtrl.this.getPlaylistOptions()))
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
    int dupCount = _playlist.removeDuplicates();
    if (dupCount > 0)
    {
      getTableModel().fireTableDataChanged();
    }
    String msg = dupCount == 1 ? "Removed 1 duplicate" : String.format("Removed %d duplicates", dupCount);
    JOptionPane.showMessageDialog(getParentFrame(), new JTransparentTextArea(msg), "Duplicates Removed", JOptionPane.INFORMATION_MESSAGE);
  }

  private void removeMissing()
  {
    int count = _playlist.removeMissing();
    if (count > 0)
    {
      getTableModel().fireTableDataChanged();
    }
    String msg = count == 1 ? "Removed 1 missing entry" : String.format("Removed %d missing entries", count);
    JOptionPane.showMessageDialog(getParentFrame(), new JTransparentTextArea(msg), "Missing Entries Removed", JOptionPane.INFORMATION_MESSAGE);
  }

  private void savePlaylist()
  {
    if (_playlist.isNew())
    {
      JFileChooser chooser = new JFileChooser();

      chooser.setAcceptAllFileFilterUsed(false);
      chooser.addChoosableFileFilter(new ExtensionFilter("m3u", "M3U Playlist (*.m3u)"));
      chooser.addChoosableFileFilter(new ExtensionFilter("m3u8", "M3U8 Playlist (*.m3u8)"));
      chooser.addChoosableFileFilter(new ExtensionFilter("pls", "PLS Playlist (*.pls)"));
      chooser.addChoosableFileFilter(new ExtensionFilter("wpl", "WPL Playlist (*.wpl)"));
      chooser.addChoosableFileFilter(new ExtensionFilter("xspf", "XSPF Playlist (*.xspf)"));
      chooser.addChoosableFileFilter(new ExtensionFilter("xml", "iTunes Playlist (*.xml)"));

      chooser.setSelectedFile(_playlist.getFile());
      int rc = chooser.showSaveDialog(getParentFrame());
      if (rc == JFileChooser.APPROVE_OPTION)
      {
        try
        {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

          File playlist = chooser.getSelectedFile();

          // prompt for confirmation if the file already exists...
          if (playlist.exists())
          {
            int result = JOptionPane.showConfirmDialog(this.getParent(), new JTransparentTextArea("You picked a file that already exists, should I really overwrite it?"), "File Exists Warning", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION)
            {
              return;
            }
          }

          String extension = ((ExtensionFilter) chooser.getFileFilter()).getExtension();

          // make sure file has correct extension
          String normalizedName = playlist.getName().trim().toLowerCase();
          if (!Playlist.isPlaylist(playlist, this.getPlaylistOptions()) || (!normalizedName.endsWith(extension)))
          {
            if (extension.equals("m3u") && _playlist.isUtfFormat())
            {
              playlist = new File(playlist.getPath() + ".m3u8");
            }
            else
            {
              playlist = new File(playlist.getPath() + "." + extension);
            }
          }

          final File finalPlaylistFile = playlist;
          ProgressWorker<Void, String> worker = new ProgressWorker<>()
          {
            @Override
            protected Void doInBackground() throws Exception
            {
              boolean saveRelative = PlaylistEditCtrl.this.getPlaylistOptions().getSavePlaylistsWithRelativePaths();
              _playlist.saveAs(finalPlaylistFile, this);
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
        catch (InterruptedException | ExecutionException e)
        {
          _logger.error("Error saving playlist", e);

          JOptionPane.showMessageDialog(getParentFrame(),
            new JTransparentTextArea(ExStack.textFormatErrorForUser("Sorry, there was an error saving your playlist.  Please try again, or file a bug report.", e.getCause())),
            "Save Playlist Error", JOptionPane.ERROR_MESSAGE);
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
        ProgressWorker<Void, String> worker = new ProgressWorker<Void, String>()
        {
          @Override
          protected Void doInBackground() throws Exception
          {
            boolean saveRelative = PlaylistEditCtrl.this.getPlaylistOptions().getSavePlaylistsWithRelativePaths();
            _playlist.save(saveRelative, this);
            return null;
          }
        };
        ProgressDialog pd = new ProgressDialog(getParentFrame(), true, worker, "Saving...", _playlist.getType() == PlaylistType.ITUNES || _playlist.getType() == PlaylistType.XSPF, false);
        pd.setMessage("Please wait while your playlist is saved to disk.");
        pd.setVisible(true);
        worker.get();
      }
      catch (InterruptedException | ExecutionException ex)
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
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    _playlistEntryRightClickMenu = new javax.swing.JPopupMenu();
    _miEditFilename = new javax.swing.JMenuItem();
    _miReplace = new javax.swing.JMenuItem();
    jSeparator3 = new javax.swing.JPopupMenu.Separator();
    _miFindClosest = new javax.swing.JMenuItem();
    jSeparator4 = new javax.swing.JPopupMenu.Separator();
    _miRemoveDups = new javax.swing.JMenuItem();
    _miRemoveMissing = new javax.swing.JMenuItem();
    _miCopySelectedFiles = new javax.swing.JMenuItem();
    _miNewPlaylistFromSelected = new javax.swing.JMenuItem();
    _uiToolbar = new javax.swing.JToolBar();
    _btnSave = new javax.swing.JButton();
    _btnReload = new javax.swing.JButton();
    _btnAdd = new javax.swing.JButton();
    _btnDelete = new javax.swing.JButton();
    jSeparator1 = new javax.swing.JToolBar.Separator();
    _btnUp = new javax.swing.JButton();
    _btnDown = new javax.swing.JButton();
    _btnInvert = new javax.swing.JButton();
    _btnReorder = new javax.swing.JButton();
    jSeparator2 = new javax.swing.JToolBar.Separator();
    _btnMagicFix = new javax.swing.JButton();
    _btnLocate = new javax.swing.JButton();
    _btnPlay = new javax.swing.JButton();
    jSeparator5 = new javax.swing.JToolBar.Separator();
    _btnPrevMissing = new javax.swing.JButton();
    _btnNextMissing = new javax.swing.JButton();
    _uiTableScrollPane = new javax.swing.JScrollPane();
    _uiTable = createTable();

    _miEditFilename.setText("Edit Filename");
    _miEditFilename.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        onMenuEditFilenameActionPerformed(evt);
      }
    });
    _playlistEntryRightClickMenu.add(_miEditFilename);

    _miReplace.setText("Replace Selected Entry");
    _miReplace.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        onMenuReplaceSelectedEntry(evt);
      }
    });
    _playlistEntryRightClickMenu.add(_miReplace);
    _playlistEntryRightClickMenu.add(jSeparator3);

    _miFindClosest.setText("Find Closest Matches");
    _miFindClosest.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        onMenuFindClosestActionPerformed(evt);
      }
    });
    _playlistEntryRightClickMenu.add(_miFindClosest);
    _playlistEntryRightClickMenu.add(jSeparator4);

    _miRemoveDups.setText("Remove Duplicates");
    _miRemoveDups.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        onMenuRemoveDuplicatesActionPerformed(evt);
      }
    });
    _playlistEntryRightClickMenu.add(_miRemoveDups);

    _miRemoveMissing.setText("Remove Missing");
    _miRemoveMissing.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        onMenuRemoveMissingActionPerformed(evt);
      }
    });
    _playlistEntryRightClickMenu.add(_miRemoveMissing);

    _miCopySelectedFiles.setText("Copy Selected Items To...");
    _miCopySelectedFiles.setToolTipText("");
    _miCopySelectedFiles.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        _miCopySelectedFilesActionPerformed(evt);
      }
    });
    _playlistEntryRightClickMenu.add(_miCopySelectedFiles);

    _miNewPlaylistFromSelected.setText("Create New Playlist with Selected Items...");
    _miNewPlaylistFromSelected.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        _miNewPlaylistFromSelectedActionPerformed(evt);
      }
    });
    _playlistEntryRightClickMenu.add(_miNewPlaylistFromSelected);

    setLayout(new java.awt.BorderLayout());

    _uiToolbar.setFloatable(false);
    _uiToolbar.setRollover(true);

    _btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save.gif"))); // NOI18N
    _btnSave.setToolTipText("Save");
    _btnSave.setEnabled(_playlist != null);
    _btnSave.setFocusable(false);
    _btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    _btnSave.setMaximumSize(new java.awt.Dimension(31, 31));
    _btnSave.setMinimumSize(new java.awt.Dimension(31, 31));
    _btnSave.setPreferredSize(new java.awt.Dimension(31, 31));
    _btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    _btnSave.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
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
    _btnReload.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(java.awt.event.MouseEvent evt)
      {
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
    _btnAdd.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        onBtnAddActionPerformed(evt);
      }
    });
    _uiToolbar.add(_btnAdd);

    _btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/edit-delete.gif"))); // NOI18N
    _btnDelete.setToolTipText("Delete");
    _btnDelete.setEnabled(false);
    _btnDelete.setFocusable(false);
    _btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    _btnDelete.setMaximumSize(new java.awt.Dimension(31, 31));
    _btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    _btnDelete.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
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
    _btnUp.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
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
    _btnDown.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        onBtnDownActionPerformed(evt);
      }
    });
    _uiToolbar.add(_btnDown);

    _btnInvert.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/invert.png"))); // NOI18N
    _btnInvert.setToolTipText("Inverts the current selection");
    _btnInvert.setEnabled(false);
    _btnInvert.setFocusable(false);
    _btnInvert.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    _btnInvert.setMaximumSize(new java.awt.Dimension(31, 31));
    _btnInvert.setMinimumSize(new java.awt.Dimension(31, 31));
    _btnInvert.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    _btnInvert.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        _btnInvertActionPerformed(evt);
      }
    });
    _uiToolbar.add(_btnInvert);

    _btnReorder.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/reorder.png"))); // NOI18N
    _btnReorder.setToolTipText("Change Playlist Order");
    _btnReorder.setEnabled(false);
    _btnReorder.setFocusable(false);
    _btnReorder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    _btnReorder.setMaximumSize(new java.awt.Dimension(31, 31));
    _btnReorder.setMinimumSize(new java.awt.Dimension(31, 31));
    _btnReorder.setPreferredSize(new java.awt.Dimension(31, 31));
    _btnReorder.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    _btnReorder.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        onBtnReorderActionPerformed(evt);
      }
    });
    _uiToolbar.add(_btnReorder);
    _uiToolbar.add(jSeparator2);

    _btnMagicFix.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/magic-fix.png"))); // NOI18N
    _btnMagicFix.setToolTipText("Fix Everything");
    _btnMagicFix.setEnabled(_playlist == null ? false : _playlist.getFile().exists());
    _btnMagicFix.setFocusable(false);
    _btnMagicFix.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    _btnMagicFix.setMaximumSize(new java.awt.Dimension(31, 31));
    _btnMagicFix.setMinimumSize(new java.awt.Dimension(31, 31));
    _btnMagicFix.setPreferredSize(new java.awt.Dimension(31, 31));
    _btnMagicFix.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    _btnMagicFix.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        _btnMagicFixonBtnLocateActionPerformed(evt);
      }
    });
    _uiToolbar.add(_btnMagicFix);

    _btnLocate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/edit-find.gif"))); // NOI18N
    _btnLocate.setToolTipText("Find Exact Matches");
    _btnLocate.setEnabled(_playlist != null && _playlist.getFile().exists());
    _btnLocate.setFocusable(false);
    _btnLocate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    _btnLocate.setMaximumSize(new java.awt.Dimension(31, 31));
    _btnLocate.setMinimumSize(new java.awt.Dimension(31, 31));
    _btnLocate.setPreferredSize(new java.awt.Dimension(31, 31));
    _btnLocate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    _btnLocate.addActionListener(evt -> onBtnLocateActionPerformed(evt));
    _uiToolbar.add(_btnLocate);

    _btnPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/play.png"))); // NOI18N
    _btnPlay.setToolTipText("Play Selected");
    _btnPlay.setEnabled(_playlist != null);
    _btnPlay.setFocusable(false);
    _btnPlay.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    _btnPlay.setMaximumSize(new java.awt.Dimension(31, 31));
    _btnPlay.setMinimumSize(new java.awt.Dimension(31, 31));
    _btnPlay.setPreferredSize(new java.awt.Dimension(31, 31));
    _btnPlay.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    _btnPlay.addActionListener(this::onBtnPlayActionPerformed);
    _uiToolbar.add(_btnPlay);
    _uiToolbar.add(jSeparator5);

    _btnPrevMissing.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/prev.png"))); // NOI18N
    _btnPrevMissing.setToolTipText("Previous Missing Entry");
    _btnPrevMissing.setEnabled(_playlist != null && _playlist.getMissingCount() > 0);
    _btnPrevMissing.setFocusable(false);
    _btnPrevMissing.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    _btnPrevMissing.setMaximumSize(new java.awt.Dimension(31, 31));
    _btnPrevMissing.setMinimumSize(new java.awt.Dimension(31, 31));
    _btnPrevMissing.setPreferredSize(new java.awt.Dimension(31, 31));
    _btnPrevMissing.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    _btnPrevMissing.addActionListener(evt -> _btnPrevMissingActionPerformed(evt));
    _uiToolbar.add(_btnPrevMissing);

    _btnNextMissing.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/next.png"))); // NOI18N
    _btnNextMissing.setToolTipText("Next Missing Entry");
    _btnNextMissing.setEnabled(_playlist != null && _playlist.getMissingCount() > 0);
    _btnNextMissing.setFocusable(false);
    _btnNextMissing.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    _btnNextMissing.setMaximumSize(new java.awt.Dimension(31, 31));
    _btnNextMissing.setMinimumSize(new java.awt.Dimension(31, 31));
    _btnNextMissing.setPreferredSize(new java.awt.Dimension(31, 31));
    _btnNextMissing.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    _btnNextMissing.addActionListener(evt -> _btnNextMissingActionPerformed(evt));
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
  }//GEN-LAST:event_onBtnPlayActionPerformed

  private void _uiTableMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event__uiTableMousePressed
  {//GEN-HEADEREND:event__uiTableMousePressed
    if (SwingUtilities.isLeftMouseButton(evt))
    {
      currentlySelectedRow = _uiTable.rowAtPoint(evt.getPoint());
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
  }//GEN-LAST:event__uiTableMousePressed

  private void _btnReloadMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event__btnReloadMouseClicked
  {//GEN-HEADEREND:event__btnReloadMouseClicked
    reloadPlaylist();
  }//GEN-LAST:event__btnReloadMouseClicked

  /**
   * @param table
   * @param row
   * @param column
   */
  public void scrollCellToVisible(JTable table, int row, int column)
  {
    table.scrollRectToVisible(table.getCellRect(row, column, true));
  }

  /**
   *
   */
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
        ProgressWorker worker = new ProgressWorker()
        {
          @Override
          protected Object doInBackground() throws IOException
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

  private void _uiTableKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event__uiTableKeyPressed
  {//GEN-HEADEREND:event__uiTableKeyPressed
    int keyVal = evt.getKeyCode();
    if (keyVal == KeyEvent.VK_DELETE && _playlist.getType() != PlaylistType.ITUNES)
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

  private void _btnNextMissingActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__btnNextMissingActionPerformed
  {//GEN-HEADEREND:event__btnNextMissingActionPerformed
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
  }//GEN-LAST:event__btnNextMissingActionPerformed

  private void scrollEditorRowIntoView(int i)
  {
    _uiTable.setRowSelectionInterval(i, i);
    scrollCellToVisible(_uiTable, i, 0);
  }

  private boolean entryNotFound(int modelRow)
  {
    return !_playlist.get(modelRow).isFound() && !_playlist.get(modelRow).isURL();
  }

  private void _btnPrevMissingActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__btnPrevMissingActionPerformed
  {//GEN-HEADEREND:event__btnPrevMissingActionPerformed
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
  }//GEN-LAST:event__btnPrevMissingActionPerformed

  private void _btnInvertActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__btnInvertActionPerformed
  {//GEN-HEADEREND:event__btnInvertActionPerformed
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
  }//GEN-LAST:event__btnInvertActionPerformed

  private void _miCopySelectedFilesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miCopySelectedFilesActionPerformed
  {//GEN-HEADEREND:event__miCopySelectedFilesActionPerformed
    int response = _destDirFileChooser.showOpenDialog(getParentFrame());
    if (response == JFileChooser.APPROVE_OPTION)
    {
      try
      {
        final File destDir = _destDirFileChooser.getSelectedFile();

        ProgressWorker<Void, Void> worker = new ProgressWorker<Void, Void>()
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
  }//GEN-LAST:event__miCopySelectedFilesActionPerformed

  private void _miNewPlaylistFromSelectedActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__miNewPlaylistFromSelectedActionPerformed
  {//GEN-HEADEREND:event__miNewPlaylistFromSelectedActionPerformed
    if (getParentFrame() instanceof GUIScreen)
    {
      List<Integer> rowList = new ArrayList<>();
      int[] uiRows = _uiTable.getSelectedRows();
      for (int x : uiRows)
      {
        rowList.add(_uiTable.convertRowIndexToModel(x));
      }
      int[] rows = ArrayFunctions.integerListToArray(rowList);
      try
      {
        // Creating the playlist and copying the entries gives us a new list w/ the "Untitled-X" style name.
        Playlist sublist = new Playlist(this.getPlaylistOptions());
        sublist.addAllAt(0, _playlist.getSublist(rows).getEntries());
        ((GUIScreen) getParentFrame()).openNewTabForPlaylist(sublist);
      }
      catch (Exception e)
      {

      }
    }
  }//GEN-LAST:event__miNewPlaylistFromSelectedActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
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
  private javax.swing.JMenuItem _miCopySelectedFiles;
  private javax.swing.JMenuItem _miEditFilename;
  private javax.swing.JMenuItem _miFindClosest;
  private javax.swing.JMenuItem _miNewPlaylistFromSelected;
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
  private javax.swing.JToolBar.Separator jSeparator5;
  // End of variables declaration//GEN-END:variables

  /**
   * @return
   */
  public Playlist getPlaylist()
  {
    return _playlist;
  }

  /**
   * @param list
   */
  public void setPlaylist(Playlist list)
  {
    setPlaylist(list, false);
  }

  /**
   * @param list
   * @param force
   */
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
                return FilenameUtils.normalize(entry.getPath() + Constants.FS + entry.getFileName());
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

  private void initPlaylistTable(final GUIScreen parent)
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
        _btnDelete.setEnabled(hasSelected && _playlist.getType() != PlaylistType.ITUNES);
        _btnUp.setEnabled(_isSortedByFileIx && hasSelected && _playlist.getType() != PlaylistType.ITUNES && _uiTable.getSelectedRow() > 0);
        _btnDown.setEnabled(_isSortedByFileIx && hasSelected && _playlist.getType() != PlaylistType.ITUNES && _uiTable.getSelectedRow() < _uiTable.getRowCount() - 1);
        _btnPlay.setEnabled(_playlist != null && _playlist.getType() != PlaylistType.ITUNES && (_uiTable.getSelectedRow() < 0 || (_uiTable.getSelectedRows().length > 0 && selectedRowsContainFoundEntry())));
        _btnReload.setEnabled(_playlist == null ? false : _playlist.isModified());
        _btnSave.setEnabled(_playlist != null);
        _btnNextMissing.setEnabled(_playlist != null && _playlist.getMissingCount() > 0);
        _btnPrevMissing.setEnabled(_playlist != null && _playlist.getMissingCount() > 0);
        _btnReorder.setEnabled(_playlist != null && _playlist.getType() != PlaylistType.ITUNES && _playlist.size() > 1);
        _btnInvert.setEnabled(hasSelected);
        if (_isSortedByFileIx)
        {
          refreshAddTooltip(hasSelected);
        }
      }
    });

    // addAt sort listener
    RowSorter sorter = _uiTable.getRowSorter();
    sorter.addRowSorterListener(new RowSorterListener()
    {
      @Override
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
          return HandleFileListFlavor(info, dl, parent);
        }
        else
        {
          // This is the flavor we handle when the user drags playlists over from the playlist panel, or when dragging in any file from linux
          return HandleStringFlavor(info, dl, parent);
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

      private boolean HandleFileListFlavor(TransferSupport info, final JTable.DropLocation dl, GUIScreen parent)
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
              final List list = (List) t.getTransferData(flavor);
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
              final List list = (List) t.getTransferData(flavor);
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

      private void ProcessFileListDrop(List list, final JTable.DropLocation dl)
      {
        int insertAt = dl.getRow();
        for (Object list1 : list)
        {
          final File tempFile = (File) list1;
          if (tempFile instanceof File)
          {
            if (Playlist.isPlaylist(tempFile, PlaylistEditCtrl.this.getPlaylistOptions()))
            {
              insertAt += ProcessDroppedPlaylist(tempFile, insertAt);
            }
            else if (tempFile.isDirectory())
            {
              List<File> filesToInsert = PlaylistScanner.getAllPlaylists(tempFile);
              for (File f : filesToInsert)
              {
                insertAt += ProcessDroppedPlaylist(f, insertAt);
              }
            }
            else if (FileUtils.isMediaFile(tempFile))
            {
              // addAt it to the playlist!
              try
              {
                _playlist.addAt(insertAt, new File[]{tempFile}, null);
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

      private void OpenFileListDrop(List list, GUIScreen parent)
      {
        for (Object list1 : list)
        {
          final File tempFile = (File) list1;
          if (tempFile instanceof File)
          {
            if (Playlist.isPlaylist(tempFile, PlaylistEditCtrl.this.getPlaylistOptions()))
            {
              parent.openPlaylist(tempFile);
            }
            else if (tempFile.isDirectory())
            {
              List<File> filesToInsert = PlaylistScanner.getAllPlaylists(tempFile);
              for (File f : filesToInsert)
              {
                parent.openPlaylist(f);
              }
            }
          }
        }
      }

      private int ProcessDroppedPlaylist(final File tempFile, int insertAt)
      {
        final Collection<String> libraryFiles;
        final int currentInsertPoint = insertAt;

        ProgressWorker<Playlist, Void> worker = new ProgressWorker<Playlist, Void>()
        {
          @Override
          protected Playlist doInBackground() throws Exception
          {
            Playlist list = PlaylistFactory.getPlaylist(tempFile, this, PlaylistEditCtrl.this.getPlaylistOptions());
            if (PlaylistEditCtrl.this.mainWindow.getOptions().getAutoLocateEntriesOnPlaylistLoad())
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
        ProgressDialog pd = new ProgressDialog((GUIScreen) getParentFrame(), true, worker, "Loading '" + (tempFile.getName().length() > 70 ? tempFile.getName().substring(0, 70) : tempFile.getName()) + "'...");
        int plistSize = _playlist.size();
        pd.setVisible(true);
        return _playlist.size() - plistSize;
      }

      private boolean HandleStringFlavor(TransferSupport info, final JTable.DropLocation dl, GUIScreen parent)
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
                List<File> files = new ArrayList<>();
                for (String path : paths)
                {
                  files.add(new File(path));
                }
                ProcessFileListDrop(files, dl);
              }
              else
              {
                if (t.getTransferData(flavor) instanceof InputStreamReader)
                {
                  List<File> filesToProcess = new ArrayList<>();
                  try (InputStreamReader reader = (InputStreamReader) t.getTransferData(flavor); BufferedReader temp = new BufferedReader(reader))
                  {
                    String filePath = temp.readLine();
                    while (filePath != null && !filePath.isEmpty())
                    {
                      filesToProcess.add(new File(new URI(filePath)));
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
              final String data;
              if (t.getTransferData(flavor) instanceof String)
              {
                // In this case, it's a magically delicious string coming from the playlist panel
                String input = (String) t.getTransferData(flavor);
                List<String> paths = StringArrayListSerializer.deserialize(input);
                List<File> files = new ArrayList<>();
                for (String path : paths)
                {
                  files.add(new File(path));
                }
                OpenFileListDrop(files, parent);
              }
              else
              {
                if (t.getTransferData(flavor) instanceof InputStreamReader)
                {
                  List<File> filesToProcess = new ArrayList<>();
                  try (InputStreamReader reader = (InputStreamReader) t.getTransferData(flavor); BufferedReader temp = new BufferedReader(reader))
                  {
                    String filePath = temp.readLine();
                    while (filePath != null && !filePath.isEmpty())
                    {
                      filesToProcess.add(new File(new URI(filePath)));
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
