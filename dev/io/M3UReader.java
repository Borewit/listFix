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

package listfix.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.StringTokenizer;
import java.util.Vector;

import listfix.controller.Task;
import listfix.model.PlaylistEntry;
import listfix.model.PlaylistType;
import listfix.util.ArrayFunctions;
import listfix.util.UnicodeUtils;

/*
============================================================================
= Author:   Jeremy Caron
= File:     M3UReader.java
= Purpose:  Read in the playlist file and return a Vector containing
=           PlaylistEntries that represent the files in the playlist.
============================================================================
 */
public class M3UReader implements IPlaylistReader
{
	private final static String fs = System.getProperty("file.separator");
	private final static String br = System.getProperty("line.separator");
	private BufferedReader buffer;
	private Vector<PlaylistEntry> results = new Vector<PlaylistEntry>();
	private long fileLength = 0;
	private String encoding = "";
	private static final PlaylistType ListType = PlaylistType.M3U;

	public M3UReader(File in) throws FileNotFoundException
	{
		try
		{
			encoding = UnicodeUtils.getEncoding(in);
			if (encoding.equals("UTF-8") || in.getName().toLowerCase().endsWith(".m3u8"))
			{
				buffer = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(in), "UTF-8"), "UTF8"));
				encoding = "UTF-8";
			}
			else
			{
				buffer = new BufferedReader(new InputStreamReader(new FileInputStream(in)));
			}
			fileLength = in.length();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public Vector<PlaylistEntry> readPlaylist(Task input) throws IOException
	{
		StringBuilder cache = new StringBuilder();
		String line1 = buffer.readLine();
		if (line1 != null)
		{
			cache.append(line1);
			String line2 = "";
			if (line1.contains("#EXTM3U"))
			{
				line1 = buffer.readLine();
				cache.append(line1);
			}
			if (line1 != null)
			{
				if (!line1.startsWith("#"))
				{
					line2 = line1;
					line1 = "";
				}
				else
				{
					line2 = buffer.readLine();
					cache.append(line2);
					while (line2.startsWith("#"))
					{
						line1 = line1 + br + line2;
						line2 = buffer.readLine();
						cache.append(line2);
					}
				}
				while (line1 != null)
				{
					if (!line2.equals(""))
					{
						processEntry(line1, line2);
					}
					input.notifyObservers((int) ((double) cache.toString().getBytes().length / (double) (fileLength) * 100.0));
					line1 = buffer.readLine();
					if (line1 != null)
					{
						cache.append(line1);
						if (!line1.startsWith("#"))
						{
							line2 = line1;
							line1 = "";
						}
						else
						{
							line2 = buffer.readLine();
							cache.append(line2);
							while (line2.startsWith("#"))
							{
								line1 = line1 + br + line2;
								line2 = buffer.readLine();
								cache.append(line2);
							}
						}
					}
					input.notifyObservers((int) ((double) cache.toString().getBytes().length / (double) (fileLength) * 100.0));
				}
			}
		}
		buffer.close();
		return results;
	}

	public Vector<PlaylistEntry> readPlaylist() throws IOException
	{
		String line1 = buffer.readLine();
		if (line1 != null)
		{
			String line2 = "";
			if (line1.contains("#EXTM3U"))
			{
				line1 = buffer.readLine();
			}
			if (line1 != null)
			{
				if (!line1.startsWith("#"))
				{
					line2 = line1;
					line1 = "";
				}
				else
				{
					line2 = buffer.readLine();
					while (line2.startsWith("#"))
					{
						line1 = line1 + br + line2;
						line2 = buffer.readLine();
					}
				}
				while (line1 != null)
				{
					processEntry(line1, line2);
					line1 = buffer.readLine();
					if (line1 != null)
					{
						if (!line1.startsWith("#"))
						{
							line2 = line1;
							line1 = "";
						}
						else
						{
							line2 = buffer.readLine();
							while (line2.startsWith("#"))
							{
								line1 = line1 + br + line2;
								line2 = buffer.readLine();
							}
						}
					}
				}
			}
		}
		buffer.close();
		return results;
	}

	private void processEntry(String L1, String L2) throws IOException
	{
		StringTokenizer pathTokenizer = null;
		StringBuilder path = new StringBuilder();
		if (L2.indexOf("://") >= 0)
		{
			// do nothing, leave tokenizer null
		}
		else if (fs.equalsIgnoreCase("/")) //OS Specific Hack
		{
			if (!L2.startsWith("\\\\") && !L2.startsWith("."))
			{
				path.append("/");
			}
			pathTokenizer = new StringTokenizer(L2, ":\\/");
		}
		else if (fs.equalsIgnoreCase(":")) //OS Specific Hack
		{
			pathTokenizer = new StringTokenizer(L2, ":\\/");
		}
		else if (fs.equalsIgnoreCase("\\")) //OS Specific Hack
		{
			pathTokenizer = new StringTokenizer(L2, "\\/");
			if (!L2.startsWith("\\\\") && L2.startsWith("\\"))
			{
				path.append("\\");
			}
		}

		if (pathTokenizer != null)
		{
			String fileName = "";
			String extInf = L1;
			if (L2.startsWith("\\\\"))
			{
				path.append("\\\\");
			}

			String firstToken = "";
			int tokenNumber = 0;
			File firstPathToExist = null;
			while (pathTokenizer.hasMoreTokens())
			{
				String word = pathTokenizer.nextToken();
				String tempPath = path.toString() + word + fs;
				if (tokenNumber == 0)
				{
					firstToken = word;
				}
				if (tokenNumber == 0 && !L2.startsWith("\\\\") && !PlaylistEntry.nonExistentDirectories.contains(word + fs))
				{
					// This token is the closest thing we have to the notion of a 'drive' on any OS...
					// make a file out of this and see if it has any files.
					File testFile = new File(tempPath);
					if (!(testFile.exists() && testFile.isDirectory() && testFile.list().length > 0) && testFile.isAbsolute())
					{
						PlaylistEntry.nonExistentDirectories.add(tempPath);
					}
				}
				else if (L2.startsWith("\\\\") && pathTokenizer.countTokens() >= 1
					&& !PlaylistEntry.nonExistentDirectories.contains("\\\\" + firstToken)
					&& !ArrayFunctions.ContainsStringWithPrefix(PlaylistEntry.existingDirectories, tempPath, true)
					&& !ArrayFunctions.ContainsStringWithPrefix(PlaylistEntry.nonExistentDirectories, tempPath, true))
				{
					// Handle UNC paths specially
					File testFile = new File(tempPath);
					boolean exists = testFile.exists();
					if (exists)
					{
						PlaylistEntry.existingDirectories.add(tempPath);
						if (firstPathToExist == null)
						{
							firstPathToExist = testFile;
						}
					}
					if (!exists && pathTokenizer.countTokens() == 1)
					{
						PlaylistEntry.nonExistentDirectories.add(tempPath);
					}
					if (pathTokenizer.countTokens() == 1 && firstPathToExist == null)
					{
						PlaylistEntry.nonExistentDirectories.add("\\\\" + firstToken);
					}
				}
				if (pathTokenizer.hasMoreTokens())
				{
					path.append(word);
					path.append(fs);
				}
				else
				{
					fileName = word;
				}
				tokenNumber++;
			}
			results.addElement(new PlaylistEntry(path.toString(), fileName, extInf));
		}
		else
		{
			try
			{
				results.addElement(new PlaylistEntry(new URI(L2.trim()), L1));
			}
			catch (Exception e)
			{
				// eat the error for now.
				e.printStackTrace();
			}
		}
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}
}
