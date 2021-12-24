/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2010 Jeremy Caron
 * 
 *  This file is part of listFix().
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.io.writers.playlists;

import christophedelory.playlist.plist.PlistPlaylist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;
import listfix.model.playlists.itunes.ITunesPlaylist;
import listfix.model.playlists.itunes.ITunesPlaylistEntry;
import listfix.model.playlists.itunes.ITunesTrack;
import listfix.util.ExStack;
import listfix.view.support.ProgressAdapter;

import org.apache.log4j.Logger;

/**
 *
 * @author jcaron
 */
public class ITunesXMLWriter implements IPlaylistWriter
{
	private static final Logger _logger = Logger.getLogger(ITunesXMLWriter.class);
	
	private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">";

	@Override
	public void save(Playlist list, boolean saveRelative, ProgressAdapter adapter) throws Exception
	{
		Map<String, ITunesTrack> trackMap = new HashMap<>();		
		
		// Need to take each entry in the playlist and update the Dict in its corresponding ITunesTrack to point to the proper location
		// Add each ITunesTrack to a map
		ITunesTrack tempTrack;
		for (PlaylistEntry entry : list.getEntries())
		{
			tempTrack = ((ITunesPlaylistEntry)entry).getTrack();
			
			URI mediaURI;
			if (entry.isURL())
			{
				mediaURI = entry.getURI();
			}
			else
			{
				if (entry.getAbsoluteFile() != null)
				{
					// Handle found files.
					mediaURI = entry.getAbsoluteFile().toURI();
				}
				else
				{
					// Handle missing files.
					mediaURI = entry.getFile().toURI();
				}
			}			
			mediaURI = normalizeFileUri(mediaURI);
			
			tempTrack.setLocation(mediaURI.toString());
			trackMap.put(tempTrack.getTrackId(), tempTrack);
		}		
		
		ITunesPlaylist iList = (ITunesPlaylist)list;
		iList.getLibrary().setTracks(trackMap);
				
		if (!adapter.getCancelled())
		{
			try
			{
				try (FileOutputStream stream = new FileOutputStream(list.getFile()))
				{
					iList.getLibrary().getPlist().writeTo(stream, "UTF-8");
				}
				
				//FileWriter second argument is for append if its true than FileWriter will
				//write bytes at the end of File (append) rather than beginning of file
				insertStringInFile(list.getFile(), 1, HEADER);
			}
			catch (Exception ex)
			{
				_logger.error(ExStack.toString(ex), ex);
				throw ex;
			}
		}
	}
	
	private void insertStringInFile(File inFile, int lineno, String lineToBeInserted) throws Exception
	{
		// temp file
		File outFile = File.createTempFile("temp",".txt");

		// input
		FileInputStream fis = new FileInputStream(inFile);
		try (BufferedReader in = new BufferedReader(new InputStreamReader(fis)))
		{
			FileOutputStream fos = new FileOutputStream(outFile);
			try (PrintWriter out = new PrintWriter(fos))
			{
				String thisLine;
				int i = 1;
				while ((thisLine = in.readLine()) != null)
				{
					if (i == lineno)
					{
						out.println(lineToBeInserted);
					}
					out.println(thisLine);
					i++;
				}
				out.flush();
			}
		}

		inFile.delete();
		outFile.renameTo(inFile);
	}

	// Files not on UNC drives need to have file:// as the prefix. 	// UNCs need file:///.
	private URI normalizeFileUri(URI mediaURI) throws URISyntaxException
	{
		// What do we do w/ UNC?  Does iTunes even support this?
		if (!mediaURI.toString().startsWith("file://") && mediaURI.toString().startsWith("file:/"))
		{
			try
			{
				mediaURI = new URI(mediaURI.toString().replace("file:/", "file://localhost/"));
			}
			catch (URISyntaxException ex)
			{
				_logger.error(ExStack.toString(ex), ex);
			}
		}
		else if (mediaURI.toString().startsWith("file:////"))
		{
			try
			{
				mediaURI = new URI(mediaURI.toString().replace("file:////", "file://localhost//"));
			}
			catch (URISyntaxException ex)
			{
				_logger.error(ExStack.toString(ex), ex);
			}
		}
		return mediaURI;
	}
}
