/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2008 Jeremy Caron
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

package listfix.controller.tasks;

import java.io.File;
import java.util.List;
import java.util.Vector;

import listfix.io.FileWriter;
import listfix.model.Playlist;
import listfix.model.RepairedPlaylistResult;

public class BatchPlaylistRepairTask extends listfix.controller.Task
{
    private Vector<Playlist> playlists = new Vector<Playlist>();
    private String[] mediaLibraryFileList;
    private Vector<RepairedPlaylistResult> results = new Vector<RepairedPlaylistResult>();
    private FileWriter fw = new FileWriter();

    /** Creates new LocateFilesTask */
    public BatchPlaylistRepairTask(List<File> lists, String[] files)
    {
        for (File list : lists)
        {
            playlists.add(new Playlist(list));
        }
        mediaLibraryFileList = files;
    }

    /** Run the task. This method is the body of the thread for this task.  */
    public void run()
    {
        int i = 0;
        for (Playlist tempList : playlists)
        {
            int originalLostCount = tempList.getLostEntryCount();
            tempList.batchRepair(mediaLibraryFileList);
            int resultLostCount = tempList.getLostEntryCount();
            boolean writtenSuccessfully = false;
            try
            {
                fw.writeM3U(tempList.getEntries(), tempList.getFile());
                writtenSuccessfully = true;
            }
            catch (Exception ex)
            {
                writtenSuccessfully = false;
            }
            results.add(new RepairedPlaylistResult(tempList, originalLostCount, resultLostCount, writtenSuccessfully));
            this.notifyObservers((int)((double)i/(double)(playlists.size()-1) * 100.0));
            i++;
        }
        this.notifyObservers(100);
    }

    public List<RepairedPlaylistResult> getResults()
    {
        return results;
    }
}
