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

package listfix.io.writers.playlists;

import christophedelory.content.Content;
import christophedelory.playlist.AbstractPlaylistComponent;
import christophedelory.playlist.Media;
import christophedelory.playlist.SpecificPlaylist;
import christophedelory.playlist.SpecificPlaylistFactory;
import christophedelory.playlist.SpecificPlaylistProvider;
import christophedelory.playlist.xspf.Track;

import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import listfix.model.Playlist;
import listfix.model.PlaylistEntry;
import listfix.util.ExStack;
import listfix.view.support.ProgressAdapter;

import org.apache.log4j.Logger;

/**
 * A playlist writer capable of saving to XSPF ("spiff") format.
 * @author jcaron
 */
public class XSPFWriter implements IPlaylistWriter
{
	private static final Logger _logger = Logger.getLogger(XSPFWriter.class);
	
	/**
	 * Saves the list to disk.  Always writes in UTF-8.
	 * @param list The list to persist to disk.
	 * @param saveRelative Currently ignored due to lack of player support on Windows.
	 * @param adapter An optionally null progress adapter which lets other code monitor the progress of this operation.
	 * @throws Exception
	 */
	@Override
	public void save(Playlist list, boolean saveRelative, ProgressAdapter adapter) throws Exception
	{
		// TODO: Support relative saving!
		boolean track = adapter != null;
		
		// First, construct a generic lizzy playlist
		christophedelory.playlist.Playlist lizzyList = new christophedelory.playlist.Playlist();
		for(PlaylistEntry entry : list.getEntries())
		{
			if (!track || !adapter.getCancelled())
			{
				lizzyList.getRootSequence().addComponent(getMedia(entry));
				if (track)
				{
					adapter.stepCompleted();
				}
			}
		}
		if (!track || !adapter.getCancelled())
		{
			SpecificPlaylistProvider spp = SpecificPlaylistFactory.getInstance().findProviderByExtension(list.getFilename());
			try
			{
				SpecificPlaylist specificPlaylist = spp.toSpecificPlaylist(lizzyList);
				addXspfMetadata(specificPlaylist, list);
				try (FileOutputStream stream = new FileOutputStream(list.getFile()))
				{
					specificPlaylist.writeTo(stream, "UTF8");
				}
			}
			catch (Exception ex)
			{
				_logger.error(ExStack.toString(ex), ex);
				throw ex;
			}
		}
	}	

	private AbstractPlaylistComponent getMedia(PlaylistEntry entry) throws Exception
	{
		Media trackToAdd = new Media();		
		trackToAdd.setSource(getContent(entry));
		return trackToAdd;
	}

	private Content getContent(PlaylistEntry entry) throws Exception
	{		
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
		return new Content(mediaURI);
	}

	private void addXspfMetadata(SpecificPlaylist specificPlaylist, Playlist list)
	{
		if (specificPlaylist.toPlaylist().getRootSequence().getComponentsNumber() == list.size())
		{
			christophedelory.playlist.xspf.Playlist castedList = (christophedelory.playlist.xspf.Playlist) specificPlaylist;
			Track track;
			for(int i = 0; i < castedList.getTracks().size(); i++)
			{
				track = castedList.getTracks().get(i);
				track.setTitle(list.getEntries().get(i).getTitle());
				long duration = list.getEntries().get(i).getLength();
				if (duration > 0)
				{
					track.setDuration((int)duration);
				}
			}
		}
	}

	// Files not on UNC drives need to have file:// as the prefix. 	// UNCs need file:///.
	private URI normalizeFileUri(URI mediaURI) throws URISyntaxException
	{
		// By default, UNC URIs have file:/ - need to convert to file:///.
		if (!mediaURI.toString().startsWith("file://") && mediaURI.toString().startsWith("file:/"))
		{
			try
			{
				mediaURI = new URI(mediaURI.toString().replace("file:/", "file:///"));
			}
			catch (URISyntaxException ex)
			{
				_logger.error(ExStack.toString(ex), ex);
			}
		}
		
		// Non-UNCs have file:////, two of the slashes need to be removed.
		if (mediaURI.toString().contains("////"))
		{
			try
			{
				mediaURI = new URI(mediaURI.toString().replace("////", "//"));
			}
			catch (URISyntaxException ex)
			{
				_logger.error(ExStack.toString(ex), ex);
				throw ex;
			}
		}
		return mediaURI;
	}
}
