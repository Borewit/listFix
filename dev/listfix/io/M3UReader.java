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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.net.URI;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import listfix.model.PlaylistEntry;
import listfix.model.enums.PlaylistType;

import listfix.util.ArrayFunctions;
import listfix.util.ExStack;
import listfix.util.UnicodeUtils;

import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;
import org.apache.log4j.Logger;

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
	private BufferedReader buffer;
	private List<PlaylistEntry> results = new ArrayList<PlaylistEntry>();
	private long fileLength = 0;
	private String encoding = "";
	private File _listFile;
	private static final PlaylistType type = PlaylistType.M3U;
	private static final Logger _logger = Logger.getLogger(M3UReader.class);

	StringBuilder _cache;

	public M3UReader(File in) throws FileNotFoundException
	{
		_listFile = in;
		encoding = UnicodeUtils.getEncoding(in);
		if (encoding.equals("UTF-8") || in.getName().toLowerCase().endsWith(".m3u8"))
		{
			try
			{
				buffer = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(in), "UTF-8"), "UTF8"));
			}
			catch (UnsupportedEncodingException ex)
			{
				// this should never happen (utf-8 must be supported) - rethrow as runtime exception
				throw new RuntimeException("Unexpected runtime error: utf-8 not supported", ex);
			}
			encoding = "UTF-8";
		}
		else
		{
			buffer = new BufferedReader(new InputStreamReader(new FileInputStream(in)));
		}
		fileLength = in.length();
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

	@Override
	public PlaylistType getPlaylistType()
	{
		return type;
	}

	@Override
	public List<PlaylistEntry> readPlaylist(IProgressObserver observer) throws IOException
	{
		ProgressAdapter progress = ProgressAdapter.wrap(observer);
		progress.setTotal((int) fileLength);

		_cache = new StringBuilder();
		String line1 = readLine();
		if (line1 != null)
		{
			String line2 = "";
			if (line1 != null)
			{
				while (line1.contains("#EXTM3U") || line1.startsWith("#EXTINFUTF8"))
				{
					line1 = readLine();
				}
				if (!line1.startsWith("#"))
				{
					line2 = line1;
					line1 = "";
				}
				else
				{
					line2 = readLine();
					while (line2.startsWith("#"))
					{
						// throw away non-standard metadata added by mediamonkey...
						if (!line2.startsWith("#UTF8"))
						{
							line1 = line1 + Constants.BR + line2;
						}
						line2 = readLine();
					}
				}

				int cacheSize = _cache.toString().getBytes().length;
				if (cacheSize < fileLength)
				{
					progress.setCompleted(cacheSize);
				}

				while (line1 != null)
				{
					if (!observer.getCancelled())
					{
						cacheSize = _cache.toString().getBytes().length;
						if (cacheSize < fileLength)
						{
							progress.setCompleted(cacheSize);
						}
						processEntry(line1, line2);
						line1 = readLine();
						if (line1 != null)
						{
							if (line1.startsWith("#EXTINFUTF8"))
							{
								line1 = readLine();
							}
							if (!line1.startsWith("#"))
							{
								line2 = line1;
								line1 = "";
							}
							else
							{
								line2 = readLine();
								while (line2.startsWith("#"))
								{
									// throw away non-standard metadata added by mediamonkey...
									if (!line2.startsWith("#UTF8"))
									{
										line1 = line1 + Constants.BR + line2;
									}
									line2 = readLine();
								}
							}
						}
						cacheSize = _cache.toString().getBytes().length;
						if (cacheSize < fileLength)
						{
							progress.setCompleted(cacheSize);
						}
					}
					else
					{
						return null;
					}
				}
			}
		}
		buffer.close();
		return results;
	}

	@Override
	public List<PlaylistEntry> readPlaylist() throws IOException
	{
		String line1 = readLine();
		if (line1 != null)
		{
			String line2 = "";
			if (line1 != null)
			{
				while (line1.contains("#EXTM3U") || line1.startsWith("#EXTINFUTF8"))
				{
					line1 = readLine();
				}
				if (!line1.startsWith("#"))
				{
					line2 = line1;
					line1 = "";
				}
				else
				{
					line2 = readLine();
					while (line2.startsWith("#"))
					{
						// throw away non-standard metadata added by mediamonkey...
						if (!line2.startsWith("#UTF8"))
						{
							line1 = line1 + Constants.BR + line2;
						}
						line2 = readLine();
					}
				}

				while (line1 != null)
				{
					processEntry(line1, line2);
					line1 = readLine();
					if (line1 != null)
					{
						if (line1.startsWith("#EXTINFUTF8"))
						{
							line1 = readLine();
						}
						if (!line1.startsWith("#"))
						{
							line2 = line1;
							line1 = "";
						}
						else
						{
							line2 = readLine();
							while (line2.startsWith("#"))
							{
								// throw away non-standard metadata added by mediamonkey...
								if (!line2.startsWith("#UTF8"))
								{
									line1 = line1 + Constants.BR + line2;
								}
								line2 = readLine();
							}
						}
					}
				}
			}
		}
		buffer.close();
		return results;
	}

	private String readLine() throws IOException
	{
		String line = buffer.readLine();
		if (_cache != null)
		{
			_cache.append(line);
		}
		return line;
	}

	private void processEntry(String L1, String L2) throws IOException
	{
		StringTokenizer pathTokenizer = null;
		StringBuilder path = new StringBuilder();
		if (L2.indexOf("://") >= 0)
		{
			// do nothing, leave tokenizer null
		}
		else if (Constants.FS.equalsIgnoreCase("/")) // OS Specific Hack
		{
			if (!L2.startsWith("\\\\") && !L2.startsWith("."))
			{
				path.append("/");
			}
			pathTokenizer = new StringTokenizer(L2, ":\\/");
		}
		else if (Constants.FS.equalsIgnoreCase(":")) // OS Specific Hack
		{
			pathTokenizer = new StringTokenizer(L2, ":\\/");
		}
		else if (Constants.FS.equalsIgnoreCase("\\")) // OS Specific Hack
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
			String secondToken = "";
			int tokenNumber = 0;
			File firstPathToExist = null;
			while (pathTokenizer.hasMoreTokens())
			{
				String word = pathTokenizer.nextToken();
				String tempPath = path.toString() + word + Constants.FS;
				if (tokenNumber == 0)
				{
					firstToken = word;
				}
				if (tokenNumber == 1)
				{
					secondToken = word;
				}
				if (tokenNumber == 0 && !L2.startsWith("\\\\") && !PlaylistEntry.NonExistentDirectories.contains(word + Constants.FS))
				{
					// This token is the closest thing we have to the notion of a 'drive' on any OS...
					// make a file out of this and see if it has any files.
					File testFile = new File(tempPath);
					if (!(testFile.exists() && testFile.isDirectory() && testFile.list().length > 0) && testFile.isAbsolute())
					{
						PlaylistEntry.NonExistentDirectories.add(tempPath);
					}
				}
				else if (L2.startsWith("\\\\") && pathTokenizer.countTokens() >= 1
					&& !PlaylistEntry.NonExistentDirectories.contains("\\\\" + firstToken + Constants.FS)
					&& !ArrayFunctions.ContainsStringPrefixingAnotherString(PlaylistEntry.ExistingDirectories, tempPath, true)
					&& !ArrayFunctions.ContainsStringPrefixingAnotherString(PlaylistEntry.NonExistentDirectories, tempPath, true))
				{
					// Handle UNC paths specially
					File testFile = new File(tempPath);
					boolean exists = testFile.exists();
					if (exists)
					{
						PlaylistEntry.ExistingDirectories.add(tempPath);
						if (firstPathToExist == null)
						{
							firstPathToExist = testFile;
						}
					}
					if (!exists && pathTokenizer.countTokens() == 1)
					{
						PlaylistEntry.NonExistentDirectories.add(tempPath);
					}
					if (pathTokenizer.countTokens() == 1 && firstPathToExist == null)
					{
						// don't want to knock out the whole drive, as other folders might be accessible there...
						PlaylistEntry.NonExistentDirectories.add("\\\\" + firstToken + Constants.FS + secondToken + Constants.FS);
					}
				}
				if (pathTokenizer.hasMoreTokens())
				{
					path.append(word);
					path.append(Constants.FS);
				}
				else
				{
					fileName = word;
				}
				tokenNumber++;
			}
			results.add(new PlaylistEntry(path.toString(), fileName, extInf, _listFile));
		}
		else
		{
			try
			{
				results.add(new PlaylistEntry(new URI(L2.trim()), L1));
			}
			catch (Exception e)
			{
				// eat the error for now.
				_logger.warn(ExStack.toString(e));
			}
		}
	}
}
