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

package listfix.model;

import java.io.File;
import java.util.Vector;
import listfix.io.M3UFileReader;
import listfix.tasks.OpenM3UTask;

/**
 *
 * @author jcaron
 */
public class Playlist
{
    private File file;
    private Vector<PlaylistEntry> entries = new Vector<PlaylistEntry>();
    private Vector<PlaylistEntry> originalEntries = new Vector<PlaylistEntry>(); 
	
	public Playlist()
	{
	}
	
	public Playlist(File playlist, OpenM3UTask task)
	{
		try
		{
			M3UFileReader playlistProcessor = new M3UFileReader(playlist);
			this.setEntries(playlistProcessor.readM3U(task));
			file = playlist;
		}
		catch(Exception e)
        {
			e.printStackTrace();
        }
	}

	public File getFile()
	{
		return file;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	public Vector<PlaylistEntry> getEntries()
	{
		return entries;
	}

	public void setEntries(Vector<PlaylistEntry> aEntries)
	{
		entries = aEntries;
		originalEntries.clear();
        for (int i = 0; i < entries.size(); i++)
        {
            originalEntries.add((PlaylistEntry)entries.get(i).clone());
        }
	}
	
	public void updateEntries(Vector<PlaylistEntry> aEntries)
	{
		entries = aEntries;
	}
	
	public int getEntryCount()
    {
        return entries.size();
    }
	
	public boolean playlistModified()
    {
        boolean result = false;
        if (originalEntries.size() != entries.size())
        {
            result = true;
        }
        else
        {
            for (int i = 0; i < entries.size(); i++)
            {
                PlaylistEntry entryA = entries.get(i);
                PlaylistEntry entryB = originalEntries.get(i);
                if ((entryA.isURL() && entryB.isURL()) || (!entryA.isURL() && !entryB.isURL()))
                {
                    if (!entryA.isURL())
                    {
                        if (!entryA.getFile().getPath().equals(entryB.getFile().getPath()))
                        {
                            result = true;
                            break;
                        }
                    }
                    else
                    {
                        if (!entryA.getURI().toString().equals(entryB.getURI().toString()))
                        {
                            result = true;
                            break;
                        }
                    }
                }
                else
                {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
	
	public int getURLCount()
    {
        int result = 0;
        int size = this.getEntryCount();
        for (int i = 0; i < size; i++)
        {
            PlaylistEntry tempEntry = entries.elementAt(i);
            if (tempEntry.isURL())
            {
                result++;
            }
        }
        return result;
    }
	
	
    public int getLostEntryCount()
    {
        int result = 0;
        int size = this.getEntryCount();
        for (int i = 0; i < size; i++)
        {
            PlaylistEntry tempEntry = entries.elementAt(i);
            if (!tempEntry.isFound() && !tempEntry.isURL() && (tempEntry.skipExistsCheck() || !tempEntry.exists()))
            {
                result++;
            }
        }
        return result;
    }
	
	public String getFilename()
    {
        if (file == null)
        {
            return "";
        }
        return file.getName();
    }
	
    public boolean isListEmpty()
    {
        return this.getEntryCount() == 0;
    }
}
