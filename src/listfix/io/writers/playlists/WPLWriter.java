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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import listfix.controller.GUIDriver;
import listfix.io.Constants;
import listfix.io.FileUtils;
import listfix.io.UNCFile;
import listfix.io.UnicodeInputStream;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;
import listfix.util.OperatingSystem;
import listfix.view.support.ProgressAdapter;

/**
 * A playlist writer capable of saving to WPL format.
 * @author jcaron & jpeterson
 */
public class WPLWriter implements IPlaylistWriter
{
	/**
	 * Saves the list to disk.  Always writes in UTF-8.
	 * @param list The list to persist to disk.
	 * @param saveRelative Specifies if the playlist should be written out relatively or not.
	 * @param adapter An optionally null progress adapter which lets other code monitor the progress of this operation.
	 * @throws IOException  
	 */
	@Override
	public void save(Playlist list, boolean saveRelative, ProgressAdapter adapter) throws IOException
	{
		boolean track = adapter != null;
		List<PlaylistEntry> entries = list.getEntries();
		File listFile = list.getFile();
		StringBuilder buffer = new StringBuilder();
		buffer.append(getWPLHead(listFile));
		PlaylistEntry entry;
		String relativePath;
		for (int i = 0; i < entries.size(); i++)
		{
			if (!track || !adapter.getCancelled())
			{
				if (track)
				{
					adapter.stepCompleted();
				}
				entry = entries.get(i);
				entry.setPlaylist(listFile);
				if (!entry.isURL())
				{
					if (!saveRelative && entry.isRelative() && entry.getAbsoluteFile() != null)
					{
						// replace existing relative entry with a new absolute one
						File absolute = entry.getAbsoluteFile().getCanonicalFile();

						// Switch to UNC representation if selected in the options
						if (GUIDriver.getInstance().getAppOptions().getAlwaysUseUNCPaths())
						{
							UNCFile temp = new UNCFile(absolute);
							absolute = new File(temp.getUNCPath());
						}
						entry = new PlaylistEntry(absolute, entry.getExtInf(), listFile, entry.getCID(), entry.getTID());
						entries.set(i, entry);
					}
					else
					{
						if (saveRelative && entry.isFound())
						{
							// replace existing entry with a new relative one
							relativePath = FileUtils.getRelativePath(entry.getAbsoluteFile().getCanonicalFile(), listFile);
							if (!OperatingSystem.isWindows() && relativePath.indexOf(Constants.FS) < 0)
							{
								relativePath = "." + Constants.FS + relativePath;
							}

							// make a new file out of this relative path, and see if it's really relative...
							// if it's absolute, we have to perform the UNC check and convert if necessary.
							File temp = new File(relativePath);
							if (temp.isAbsolute())
							{
								// Switch to UNC representation if selected in the options
								if (GUIDriver.getInstance().getAppOptions().getAlwaysUseUNCPaths())
								{
									UNCFile uncd = new UNCFile(temp);
									temp = new File(uncd.getUNCPath());
								}
							}

							// make the entry and addAt it						
							entry = new PlaylistEntry(temp, entry.getExtInf(), listFile, entry.getCID(), entry.getTID());
							entries.set(i, entry);
						}
					}
				}

				String media = "\t\t\t<media src=\"" + XMLEncode(serializeEntry(entry)) + "\"";
				if (!entry.getCID().isEmpty())
				{
					media += " cid=\"" + entry.getCID() + "\"";
				}
				if (!entry.getTID().isEmpty())
				{
					media += " tid=\"" + entry.getTID() + "\"";
				}
				media += "/>" + Constants.BR;
				buffer.append(media);
			}
			else
			{
				return;
			}
		}
		buffer.append(getWPLFoot());

		if (!track || !adapter.getCancelled())
		{
			File dirToSaveIn = listFile.getParentFile().getAbsoluteFile();
			if (!dirToSaveIn.exists())
			{
				dirToSaveIn.mkdirs();
			}
			try (FileOutputStream outputStream = new FileOutputStream(listFile))
			{
				Writer osw = new OutputStreamWriter(outputStream, "UTF8");
				try (BufferedWriter output = new BufferedWriter(osw))
				{
					output.write(buffer.toString());
				}
			}
			list.setUtfFormat(true);
		}
	}

	// WPL Helper Method
	private String XMLEncode(String s)
	{
		s = s.replaceAll("&", "&amp;");
		s = s.replaceAll("'", "&apos;");
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		return s;
	}

	// WPL Helper Method
	private String getWPLHead(File listFile) throws IOException
	{
		String head = "";
		boolean newHead = false;
		try
		{
			try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(listFile), "UTF-8"), "UTF8")))
			{
				String line = buffer.readLine();
				while (line != null)
				{
					if (line.trim().startsWith("<media"))
					{
						break;
					}
					head += line + Constants.BR;
					line = buffer.readLine();
				}
			}
			// determine if a head was read
			if (!head.contains("<?wpl"))
			{
				newHead = true;
			}
		}
		catch (Exception ex)
		{
			// Don't bother logging here, it's expected when saving out a new file
			// _logger.error(ExStack.toString(ex));
			newHead = true;
		}
		if (newHead)
		{
			head = "<?wpl version=\"1.0\"?>\r\n<smil>\r\n\t<body>\r\n\t\t<sec>\r\n";
		}
		return head;
	}

	// WPL Helper Method
	private String getWPLFoot() throws IOException
	{
		return "\t\t</sec>\r\n\t</body>\r\n</smil>";
	}
	
	private String serializeEntry(PlaylistEntry entry)
	{
		StringBuilder result = new StringBuilder();
		if (!entry.isURL())
		{
			if (!entry.isRelative())
			{
				if (entry.getPath().endsWith(Constants.FS))
				{
					result.append(entry.getPath());
					result.append(entry.getFileName());
				}
				else
				{
					result.append(entry.getPath());
					result.append(Constants.FS);
					result.append(entry.getFileName());
				}
			}
			else
			{
				String tempPath = entry.getFile().getPath();
				if (tempPath.substring(0, tempPath.indexOf(entry.getFileName())).equals(Constants.FS))
				{
					result.append(entry.getFileName());
				}
				else
				{
					result.append(entry.getFile().getPath());
				}
			}
		}
		else
		{
			result.append(entry.getURI().toString());
		}
		return result.toString();
	}
}
