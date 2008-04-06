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

package listfix.tasks;

/**
 *
 * @author  jcaron
 * @version 
 */
import listfix.model.PlaylistEntry;
import java.util.Vector;

public class LocateFilesTask extends listfix.controller.Task 
{
    private Vector entries;
    private String[] mediaLibraryFileList;
    
    /** Creates new LocateFilesTask */
    public LocateFilesTask(Vector x, String[] y) 
    {
        entries = x;
        mediaLibraryFileList = y;
    }

    /** Run the task. This method is the body of the thread for this task.  */
    public void run() 
    {
        PlaylistEntry tempEntry = null;
        for (int i = 0; i < entries.size(); i++)
        {
            tempEntry = (PlaylistEntry) entries.elementAt(i);
            if (!tempEntry.isURL())
            {
                if (tempEntry.exists())
                {
                    tempEntry.setMessage("Found!");
                }
                else
                {
                    tempEntry.findNewLocationFromFileList(mediaLibraryFileList);
                }            
            }
            this.notifyObservers((int)((double)i/(double)(entries.size()-1) * 100.0));
        }
        this.notifyObservers(100);
    }
    
    public Vector locateFiles()
    {
        return entries;
    }
}
