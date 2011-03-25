/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2010 Jeremy Caron
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import listfix.model.PlaylistEntry;
import listfix.model.enums.PlaylistType;
import listfix.util.ExStack;
import listfix.util.UnicodeUtils;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;
import org.apache.log4j.Logger;

/*
============================================================================
= Author:   Jeremy Caron
= File:     PLSFileReader.java
= Purpose:  Read in the playlist file and return a Vector containing
=           PlaylistEntries that represent the files in the playlist.
============================================================================
 */
public class PLSReader implements IPlaylistReader
{
	private File plsFile = null;
	private List<PlaylistEntry> results = new ArrayList<PlaylistEntry>();
	private String encoding = "";
	private static final PlaylistType type = PlaylistType.PLS;
	private static final Logger _logger = Logger.getLogger(PLSReader.class);

	public PLSReader(File in) throws FileNotFoundException
	{
		try
		{
			encoding = UnicodeUtils.getEncoding(in);
			if (encoding.equals("UTF-8"))
			{
				encoding = "UTF-8";
			}
			plsFile = in;
		}
		catch (Exception e)
		{
			_logger.error(ExStack.toString(e));
		}
	}

	@Override
	public List<PlaylistEntry> readPlaylist(IProgressObserver observer) throws IOException
	{
		ProgressAdapter progress = ProgressAdapter.wrap(observer);

		PLSProperties propBag = new PLSProperties();
		propBag.load(new FileInputStream(plsFile));
		int entries = Integer.parseInt((propBag.getProperty("NumberOfEntries", "0")));
		progress.setTotal((int) entries);

		for (int i = 1; i <= entries; i++)
		{
			if (!observer.getCancelled())
			{
				processEntry(propBag, i);
				progress.setCompleted(i);
			}
			else
			{
				return null;
			}
		}
		return results;
	}

	@Override
	public List<PlaylistEntry> readPlaylist() throws IOException
	{
		PLSProperties propBag = new PLSProperties();
		propBag.load(new FileInputStream(plsFile));
		int entries = Integer.parseInt((propBag.getProperty("NumberOfEntries", "0")));
		for (int i = 1; i <= entries; i++)
		{
			processEntry(propBag, i);
		}
		return results;
	}

	private void processEntry(PLSProperties propBag, int index) throws IOException
	{
		String file = propBag.getProperty("File" + index, "");
		String title = propBag.getProperty("Title" + index, "");
		String length = propBag.getProperty("Length" + index, "");

		if (file.contains("://"))
		{
			try
			{
				results.add(new PlaylistEntry(new URI(file), title, length));
			}
			catch (URISyntaxException ex)
			{
				_logger.warn(ExStack.toString(ex));
			}
		}
		else
		{
			results.add(new PlaylistEntry(new File(file), title, length, plsFile));
		}
	}

	@Override
	public String getEncoding()
	{
		return encoding;
	}

	@Override
	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	public PlaylistType getPlaylistType()
	{
		return type;
	}
}
