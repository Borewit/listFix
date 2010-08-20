/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2009 Jeremy Caron
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

/**
 *
 * @author  jcaron
 * @version 
 */
import java.io.File;

import listfix.controller.*;
import listfix.io.*;
import listfix.model.Playlist;

public class OpenPlaylistTask extends listfix.controller.Task
{
	private GUIDriver guiDriver;
	private File input;

	public OpenPlaylistTask(GUIDriver gd, File f)
	{
		guiDriver = gd;
		input = f;
	}

	/** Run the task. This method is the body of the thread for this task.  */
	@Override
	public void run()
	{
		try
		{
			guiDriver.setPlaylist(new Playlist(input, this));
			guiDriver.getHistory().add(input.getCanonicalPath());
			(new FileWriter()).writeMruPlaylists(guiDriver.getHistory());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			this.notifyObservers(100);
		}
	}
}
