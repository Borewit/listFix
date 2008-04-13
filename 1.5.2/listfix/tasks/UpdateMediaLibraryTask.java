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
import listfix.io.*;
import listfix.controller.*;

public class UpdateMediaLibraryTask extends listfix.controller.Task 
{
    private GUIDriver guiDriver;
    private String[] mediaDir;
    private String[] mediaLibraryDirectoryList;
    private String[] mediaLibraryFileList;  
    
    public UpdateMediaLibraryTask(GUIDriver gd) 
    {
        guiDriver = gd;
        mediaDir = gd.getMediaDirs();
    }

    /** Run the task. This method is the body of the thread for this task.  */
    public void run() 
    {
        if(mediaDir != null)
        {
            DirectoryScanner ds = new DirectoryScanner();
            ds.createMediaLibraryDirectoryAndFileList(guiDriver.getMediaDirs(), this);
            this.setMessage("Finishing...");
            mediaLibraryDirectoryList = ds.getDirectoryList();
            mediaLibraryFileList = ds.getFileList();
            ds.reset();
            java.util.Arrays.sort(mediaDir);
            guiDriver.setMediaDirs(mediaDir);
            java.util.Arrays.sort(mediaLibraryDirectoryList);
            guiDriver.setMediaLibraryDirectoryList(mediaLibraryDirectoryList);
            java.util.Arrays.sort(mediaLibraryFileList);
            guiDriver.setMediaLibraryFileList(mediaLibraryFileList);
            this.notifyObservers(100);
            FileWriter.writeIni(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList, guiDriver.getAppOptions());
        }
    }   
}