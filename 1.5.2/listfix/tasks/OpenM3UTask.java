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
import java.io.File;
import java.util.*;
import listfix.model.Playlist;

public class OpenM3UTask extends listfix.controller.Task 
{
    private GUIDriver guiDriver;
    private File input; 
    
    public OpenM3UTask(GUIDriver gd, File f) 
    {
        guiDriver = gd;
        input = f;
    }
    
    /** Run the task. This method is the body of the thread for this task.  */
    public void run() 
    {	
		try
		{
			guiDriver.setCurrentList(new Playlist(input, this));
			guiDriver.getHistory().add(input.getCanonicalPath());
			(new FileWriter()).writeMruM3Us(guiDriver.getHistory());
			this.notifyObservers(100);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			this.notifyObservers(100);
		}
    }   
}