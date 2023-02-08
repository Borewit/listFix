/**
 * listFix() - Fix Broken Playlists!
 * <p>
 * This file is part of listFix().
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.model;

import listfix.config.IMediaLibrary;
import listfix.io.IPlaylistOptions;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistFactory;
import listfix.view.support.DualProgressAdapter;
import listfix.view.support.IDualProgressObserver;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Serves to model the batch repair operations on multiple playlists, both closest matches and exact matches.
 *
 * @author jcaron
 */

public class BatchRepair
{
  // The wrappers around the playlists to be repaired.
  private final List<BatchRepairItem> _items = new ArrayList<>();

  // The list of files in the media library to be considered during the repair.
  private final IMediaLibrary mediaLibrary;

  // The lowest common directory of all playlists being repaired.
  private File _rootDirectory;

  // The description of this batch repair, currently used in the title of the results dialog for this run.
  private String _description;

  /**
   * @param mediaLibrary Media library to be considered during the repair
   * @param rootDirectory
   */
  public BatchRepair(IMediaLibrary mediaLibrary, File rootDirectory)
  {
    this.mediaLibrary = mediaLibrary;
    _rootDirectory = rootDirectory;
    if (!rootDirectory.isDirectory())
    {
      _rootDirectory = _rootDirectory.getParentFile();
    }
  }

  /**
   * @return
   */
  public List<BatchRepairItem> getItems()
  {
    return _items;
  }

  /**
   * @param ix
   * @return
   */
  public BatchRepairItem getItem(int ix)
  {
    return _items.get(ix);
  }

  public boolean isEmpty()
  {
    return _items.isEmpty();
  }

  public String getDescription()
  {
    return _description;
  }

  public void setDescription(String description)
  {
    _description = description;
  }

  public File getRootDirectory()
  {
    return _rootDirectory;
  }

  public List<Playlist> getPlaylists()
  {
    List<Playlist> result = new ArrayList<>();
    for (BatchRepairItem item : _items)
    {
      result.add(item.getPlaylist());
    }
    return result;
  }

  public void add(BatchRepairItem item)
  {
    _items.add(item);
  }

  /**
   * Performs an exact matches search on all entries in multiple playlists.
   *
   * @param observer The progress observer for this operation.
   */
  public void performExactMatchRepair(IDualProgressObserver<String> observer, IPlaylistOptions filePathOptions)
  {
    DualProgressAdapter<String> progress = DualProgressAdapter.wrap(observer);
    progress.getOverall().setTotal(_items.size() * 2);

    List<BatchRepairItem> toRemoveFromBatch = new ArrayList<>();
    for (BatchRepairItem item : _items)
    {
      if (!observer.getCancelled())
      {
        // load
        progress.getOverall().stepCompleted();
        progress.getTask().reportProgress(0, "Loading \"" + item.getDisplayName() + "\"");
        if (item.getPlaylist() == null)
        {
          File file = new File(item.getPath());
          try
          {
            Playlist temp = PlaylistFactory.getPlaylist(file, progress.getTask(), filePathOptions);
            item.setPlaylist(temp);
          }
          catch (IOException e)
          {
            toRemoveFromBatch.add(item);
          }
        }

        // repair
        if (item.getPlaylist() != null && item.getPlaylist().getMissingCount() > 0)
        {
          progress.getOverall().stepCompleted();
          progress.getTask().reportProgress(0, "Repairing \"" + item.getDisplayName() + "\"");
          Playlist list = item.getPlaylist();
          list.batchRepair(this.mediaLibrary, progress.getTask());
        }
        else
        {
          // Don't fix playlists that have nothing to fix... instead remove them from the result set.
          toRemoveFromBatch.add(item);
        }
      }
      else
      {
        return;
      }
    }

    for (BatchRepairItem item : toRemoveFromBatch)
    {
      _items.remove(item);
    }
  }

  /**
   * Performs a closest matches search on all entries in multiple playlists.
   *
   * @param observer The progress observer for this operation.
   */
  public void performClosestMatchRepair(IDualProgressObserver<String> observer, IPlaylistOptions filePathOptions)
  {
    DualProgressAdapter<String> progress = DualProgressAdapter.wrap(observer);
    progress.getOverall().setTotal(_items.size() * 2);

    List<BatchRepairItem> toRemoveFromBatch = new ArrayList<>();
    for (BatchRepairItem item : _items)
    {
      if (!observer.getCancelled())
      {
        // load
        progress.getOverall().stepCompleted();
        progress.getTask().reportProgress(0, "Loading \"" + item.getDisplayName() + "\"");
        if (item.getPlaylist() == null)
        {
          File file = new File(item.getPath());
          try
          {
            Playlist temp = PlaylistFactory.getPlaylist(file, progress.getTask(), filePathOptions);
            item.setPlaylist(temp);
          }
          catch (IOException e)
          {
            toRemoveFromBatch.add(item);
          }
        }

        // repair
        if (item.getPlaylist() != null && item.getPlaylist().getMissingCount() > 0)
        {
          progress.getOverall().stepCompleted();
          progress.getTask().reportProgress(0, "Repairing \"" + item.getDisplayName() + "\"");
          Playlist list = item.getPlaylist();
          item.setClosestMatches(list.findClosestMatches(this.mediaLibrary.getNestedMediaFiles(), progress.getTask()));
        }
        else
        {
          // Don't fix playlists that have nothing to fix... instead remove them from the result set.
          toRemoveFromBatch.add(item);
        }
      }
      else
      {
        return;
      }
    }

    for (BatchRepairItem item : toRemoveFromBatch)
    {
      _items.remove(item);
    }
  }

  /**
   * Helper method to get auto-generated zip file name for backing up the playlists in this batch repair.
   */
  public String getDefaultBackupName()
  {
    Date timestamp = new Date();
    String name = String.format("playlist backup %1$tY-%1$tm-%1$td %1$tH%1$tM.zip", timestamp);
    File file = new File(_rootDirectory, name);
    return file.getAbsolutePath();
  }

  /**
   * Save out all the playlists in this batch repair and backup the originals if told to do so.
   *
   * @param filePathOptions      Options
   * @param isClosestMatchesSave Are we saving the results of a closest matches search?
   * @param backup               Should we backup the originals to a zip file?
   * @param destination          The path to the backup zip file we'll create if told to backup the originals.
   * @param observer             The progress observer for this operation.
   * @throws Exception
   */
  public void save(IPlaylistOptions filePathOptions, boolean isClosestMatchesSave, boolean backup, String destination, IProgressObserver<String> observer) throws Exception
  {
    ProgressAdapter<String> progress = ProgressAdapter.wrap(observer);

    // get included items
    int stepCount = 0;
    for (BatchRepairItem item : _items)
    {
      Playlist list = item.getPlaylist();
      if (backup)
      {
        stepCount += list.getFile().length();
      }
      stepCount += list.size();
    }

    progress.setTotal(stepCount);

    // backup to zip file
    if (backup)
    {
      URI root = _rootDirectory.toURI();

      FileOutputStream file = new FileOutputStream(destination);
      try (ZipOutputStream zip = new ZipOutputStream(file))
      {
        byte[] buffer = new byte[4096];

        for (BatchRepairItem item : _items)
        {
          File listFile = item.getPlaylist().getFile();

          // make playlist entry relative to root directory
          URI listUrl = root.relativize(listFile.toURI());
          String name = URLDecoder.decode(listUrl.toString(), "UTF-8");

          ZipEntry entry = new ZipEntry(name);
          zip.putNextEntry(entry);
          try (FileInputStream reader = new FileInputStream(listFile))
          {
            while (true)
            {
              int count = reader.read(buffer);
              if (count == -1)
              {
                break;
              }
              zip.write(buffer, 0, count);
              progress.stepCompleted(count);
            }
          }
        }
      }
    }

    // save
    for (BatchRepairItem item : _items)
    {
      if (isClosestMatchesSave)
      {
        item.getPlaylist().applyClosestMatchSelections(item.getClosestMatches());
      }
      item.getPlaylist().save(filePathOptions.getSavePlaylistsWithRelativePaths(), progress);
    }
  }
}
