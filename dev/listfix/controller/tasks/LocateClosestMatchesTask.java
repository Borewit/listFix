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
import listfix.model.*;
import listfix.util.*;
import java.util.Vector;
import java.io.*;

public class LocateClosestMatchesTask extends listfix.controller.Task
{
	private PlaylistEntry entry;
	private String[] mediaLibraryFileList;
	private Vector<MatchedPlaylistEntry> results = new Vector<MatchedPlaylistEntry>();

	public LocateClosestMatchesTask(PlaylistEntry x, String[] y)
	{
		entry = x;
		mediaLibraryFileList = y;
	}

	/** Run the task. This method is the body of the thread for this task.  */
	@Override
	public void run()
	{
		// implement tokenized file name matching procedure here...
		for (int i = 0; i < mediaLibraryFileList.length; i++)
		{
			File mediaFile = new File(mediaLibraryFileList[i]);
			int score = FileNameTokenizer.score(entry.getFileName().replaceAll("\'", ""), mediaFile.getName().replaceAll("\'", ""));
			if (score > 0)
			{
				results.add(new MatchedPlaylistEntry(mediaFile, score));
			}
			this.notifyObservers((int) ((double) i / (double) (mediaLibraryFileList.length - 1) * 100.0));
		}
	}

	public Vector<MatchedPlaylistEntry> locateClosestMatches()
	{
		return results;
	}
}
