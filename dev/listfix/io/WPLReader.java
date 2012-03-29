/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2010 Jeremy Caron, 2012 John Peterson
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
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;

import org.apache.log4j.Logger;

/*
============================================================================
= Author:   Jeremy Caron, John Peterson
= File:     WPLReader.java
= Purpose:  Read in the playlist file and return a Vector containing
=           PlaylistEntries that represent the files in the playlist.
============================================================================
 */
public class WPLReader implements IPlaylistReader
{
	private BufferedReader buffer;
	private List<PlaylistEntry> results = new ArrayList<PlaylistEntry>();
	private long fileLength = 0;
	private String encoding = "";
	private File _listFile;
	private static final PlaylistType type = PlaylistType.WPL;
	private static final Logger _logger = Logger.getLogger(WPLReader.class);

	StringBuilder _cache;

	public WPLReader(File in) throws FileNotFoundException
	{
		_listFile = in;
		try
		{
			buffer = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(in), "UTF-8"), "UTF8"));
		}
		catch (UnsupportedEncodingException ex)
		{
			// this should never happen (utf-8 must be supported) - rethrow as runtime exception
			_logger.error("Holy shit, ther'e no UTF-8 support on this machine!! " + ExStack.toString(ex));
			throw new RuntimeException("Unexpected runtime error: utf-8 not supported", ex);
		}
		encoding = "UTF-8";
		fileLength = in.length();
	}

	private String XMLDecode(String s)
	{
		s = s.replaceAll("&apos;", "'");		
		s = s.replaceAll("&lt;", "<");
		s = s.replaceAll("&gt;", ">");
		s = s.replaceAll("&amp;", "&");
		return s;
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
		ProgressAdapter progress;
		if (observer != null) progress = ProgressAdapter.wrap(observer); else progress = null;
		if (progress != null) progress.setTotal((int) fileLength);
		
		_cache = new StringBuilder();
		String line = readLine();
		String path = "";
		String cid = "";
		String tid = "";
		
		int cacheSize = _cache.toString().getBytes().length;
		if (cacheSize < fileLength)
		{
			if (progress != null) progress.setCompleted(cacheSize);
		}
		
		while (line != null)
		{
			if (observer != null) if (observer.getCancelled()) return null;
			cacheSize = _cache.toString().getBytes().length;
			if (cacheSize < fileLength)
			{
				if (progress != null) progress.setCompleted(cacheSize);
			}				
			line = readLine();
			if (line == null) break;
			line = XMLDecode(line);
			
			if (line.trim().startsWith("<media")) {
				path = line.substring(line.indexOf("\"")+1, line.indexOf("\"", line.indexOf("\"")+1));
				if (line.contains("cid=\"")) cid = line.substring(line.indexOf("cid=\"")+5, line.indexOf("\"", line.indexOf("cid=\"")+5));
				if (line.contains("tid=\"")) tid = line.substring(line.indexOf("tid=\"")+5, line.indexOf("\"", line.indexOf("tid=\"")+5));
			}
			if (!path.isEmpty()) processEntry(path, cid, tid);
			path = ""; cid = ""; tid = "";
			
			cacheSize = _cache.toString().getBytes().length;
			if (cacheSize < fileLength)
			{
				if (progress != null) progress.setCompleted(cacheSize);
			}
		}
		buffer.close();
		return results;
	}

	@Override
	public List<PlaylistEntry> readPlaylist() throws IOException
	{
		readPlaylist(null);
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

	private void processEntry(String L2, String cid, String tid) throws IOException
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
			String extInf = "";
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
			results.add(new PlaylistEntry(path.toString(), fileName, extInf, _listFile, cid, tid));
		}
		else
		{
			try
			{
				results.add(new PlaylistEntry(new URI(L2.trim()), "", cid, tid));
			}
			catch (Exception e)
			{
				// eat the error for now.
				_logger.warn(ExStack.toString(e));
			}
		}
	}
}
