/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2012 Jeremy Caron
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

package listfix.io;

import java.io.File;
import java.io.IOException;

import listfix.model.Playlist;
import listfix.model.PlaylistEntry;
import listfix.util.ExStack;

import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;
import org.apache.log4j.Logger;

public class PlaylistEntryFileCopier
{
	private Playlist _list;
	private File _destination;
	private IProgressObserver _observer;
	private static final Logger _logger = Logger.getLogger(PlaylistEntryFileCopier.class);

	public PlaylistEntryFileCopier(Playlist x, File y, IProgressObserver observer)
	{
		_list = x;
		_destination = y;
		_observer = observer;
	}

	// Perform the copy
	public void copy()
	{
		ProgressAdapter progress = ProgressAdapter.wrap(_observer);
		progress.setTotal(_list.size());
		PlaylistEntry tempEntry = null;
		File fileToCopy = null;
		File dest = null;
		for (int i = 0; i < _list.size(); i++)
		{
			if (!_observer.getCancelled())
			{
				tempEntry = _list.get(i);
				if (!tempEntry.isURL())
				{
					fileToCopy = tempEntry.getAbsoluteFile();
					if (tempEntry.isFound()) // && fileToCopy.exists())
					{
						dest = new File(_destination.getPath() + Constants.FS + tempEntry.getFileName());
						try
						{
							FileCopier.copy(fileToCopy, dest);
						}
						catch (IOException e)
						{
							// eat the error and continue
							_logger.error(ExStack.toString(e));
						}
					}
				}
				progress.stepCompleted();
			}
			else
			{
				return;
			}
		}
	}
}
