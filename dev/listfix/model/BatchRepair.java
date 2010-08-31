/**
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

package listfix.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import listfix.util.Log;
import listfix.view.support.DualProgressAdapter;
import listfix.view.support.IDualProgressObserver;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;

public class BatchRepair
{
    public BatchRepair(String[] mediaFiles, File rootDirectory)
    {
        _mediaFiles = mediaFiles;
        _rootDirectory = rootDirectory;
        if (!rootDirectory.isDirectory())
            _rootDirectory = _rootDirectory.getParentFile();
    }

    public void add(BatchRepairItem item)
    {
        _items.add(item);
    }

    public void load(IDualProgressObserver<String> observer) throws IOException
    {
        DualProgressAdapter<String> progress = DualProgressAdapter.wrap(observer);
        progress.getOverall().setTotal(_items.size() * 2);

        for (BatchRepairItem item : _items)
        {
            // load
            progress.getOverall().stepCompleted();
            progress.getTask().reportProgress(0, "Loading \"" + item.getDisplayName() + "\"");
            File file = new File(item.getPath());
            item.setPlaylist(new Playlist(file, progress.getTask()));

            // repair
            progress.getOverall().stepCompleted();
            progress.getTask().reportProgress(0, "Repairing \"" + item.getDisplayName() + "\"");
            Playlist list = item.getPlaylist();
            list.batchRepair(_mediaFiles, progress.getTask());
        }
    }

    public String getDefaultBackupName()
    {
        Date timestamp = new Date();
        String name = String.format("playlist backup %1$tY-%1$tm-%1$td %1$tH%1$tM.zip", timestamp);
        File file = new File(_rootDirectory, name);
        return file.getAbsolutePath();
    }

    public void save(boolean saveRelative, boolean backup, String destination, IProgressObserver observer) throws FileNotFoundException, IOException
    {
        ProgressAdapter progress = ProgressAdapter.wrap(observer);

        // get included items
        int stepCount = 0;
        for (BatchRepairItem item : _items)
        {
            Playlist list = item.getPlaylist();
            if (backup)
                stepCount += list.getFile().length();
            stepCount += list.size();
        }

        progress.setTotal(stepCount);

        // backup to zip file
        if (backup)
        {
            URI root = _rootDirectory.toURI();

            FileOutputStream file = new FileOutputStream(destination);
            ZipOutputStream zip = new ZipOutputStream(file);

            byte[] buffer = new byte[4096];

            Log.write("backup root: %s", root);
            Log.indent();
            for (BatchRepairItem item : _items)
            {
                File listFile = item.getPlaylist().getFile();

                // make playlist entry relative to root directory
                URI listUrl = root.relativize(listFile.toURI());
                String name = URLDecoder.decode(listUrl.toString(), "UTF-8");
                Log.write("%s", name);

                ZipEntry entry = new ZipEntry(name);
                zip.putNextEntry(entry);

                FileInputStream reader = new FileInputStream(listFile);
                while (true)
                {
                    int count = reader.read(buffer);
                    if (count == -1)
                        break;
                    zip.write(buffer, 0, count);
                    progress.stepCompleted(count);
                }
                reader.close();
            }
            zip.close();

            Log.write("done.");
            Log.unindent();
        }

        // save
        for (BatchRepairItem item : _items)
        {
            item.getPlaylist().save(saveRelative, progress);
        }
    }

    private List<BatchRepairItem> _items = new ArrayList<BatchRepairItem>();
    private String[] _mediaFiles;

    public List<BatchRepairItem> getItems()
    {
        return _items;
    }

    public BatchRepairItem getItem(int ix)
    {
        return _items.get(ix);
    }

    public Boolean isEmpty()
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
    private String _description;

    public File getRootDirectory()
    {
        return _rootDirectory;
    }
    private File _rootDirectory;

}
