/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2012 Jeremy Caron, 2012 John Peterson
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

package listfix.io.readers.playlists;

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

import listfix.io.Constants;
import listfix.io.UnicodeInputStream;
import listfix.model.PlaylistEntry;
import listfix.model.enums.PlaylistType;
import listfix.util.ArrayFunctions;
import listfix.util.ExStack;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;

import org.apache.log4j.Logger;

/**
 * Reads in a WPL file and returns a List containing PlaylistEntries that represent the files & URIs in the playlist.
 * @author jcaron, jpeterson
 */
public class WPLReader implements IPlaylistReader
{
	private BufferedReader _buffer;
	private List<PlaylistEntry> results = new ArrayList<>();
	private long fileLength = 0;
	private String encoding = "";
	private File _listFile;
	private static final PlaylistType type = PlaylistType.WPL;
	private static final Logger _logger = Logger.getLogger(WPLReader.class);

	/**
	 * 
	 */
	StringBuilder _cache;

	/**
	 * 
	 * @param in
	 * @throws FileNotFoundException
	 */
	public WPLReader(File in) throws FileNotFoundException
	{
		_listFile = in;
		try
		{
			_buffer = new BufferedReader(new InputStreamReader(new UnicodeInputStream(new FileInputStream(in), "UTF-8"), "UTF8"));
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
	
	/**
	 * 
	 * @return
	 */
	@Override
	public String getEncoding()
	{
		return encoding;
	}

	/**
	 * 
	 * @param encoding
	 */
	@Override
	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public PlaylistType getPlaylistType()
	{
		return type;
	}

	/**
	 * 
	 * @param observer
	 * @return
	 * @throws IOException
	 */
	@Override
	public List<PlaylistEntry> readPlaylist(IProgressObserver observer) throws IOException
	{
		ProgressAdapter progress = null;
		if (observer != null)
		{
			progress = ProgressAdapter.wrap(observer);
		}
		if (progress != null)
		{
			progress.setTotal((int) fileLength);
		}

		_cache = new StringBuilder();
		String line = readLine();
		String path = "";
		String cid = "";
		String tid = "";

		int cacheSize = _cache.toString().getBytes().length;
		if (cacheSize < fileLength)
		{
			if (progress != null)
			{
				progress.setCompleted(cacheSize);
			}
		}

		while (line != null)
		{
			if (observer != null)
			{
				if (observer.getCancelled())
				{
					return null;
				}
			}
			cacheSize = _cache.toString().getBytes().length;
			if (cacheSize < fileLength)
			{
				if (progress != null)
				{
					progress.setCompleted(cacheSize);
				}
			}
			line = readLine();
			if (line == null)
			{
				break;
			}
			line = XMLDecode(line);

			if (line.trim().startsWith("<media"))
			{
				path = line.substring(line.indexOf("\"") + 1, line.indexOf("\"", line.indexOf("\"") + 1));
				if (line.contains("cid=\""))
				{
					cid = line.substring(line.indexOf("cid=\"") + 5, line.indexOf("\"", line.indexOf("cid=\"") + 5));
				}
				if (line.contains("tid=\""))
				{
					tid = line.substring(line.indexOf("tid=\"") + 5, line.indexOf("\"", line.indexOf("tid=\"") + 5));
				}
			}
			if (!path.isEmpty())
			{
				processEntry(path, cid, tid);
			}
			path = "";
			cid = "";
			tid = "";

			cacheSize = _cache.toString().getBytes().length;
			if (cacheSize < fileLength)
			{
				if (progress != null)
				{
					progress.setCompleted(cacheSize);
				}
			}
		}
		_buffer.close();
		return results;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	@Override
	public List<PlaylistEntry> readPlaylist() throws IOException
	{
		readPlaylist(null);
		return results;
	}

	private String readLine() throws IOException
	{
		String line = _buffer.readLine();
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
			if (!L2.startsWith("\\\\") && !L2.startsWith(".") && !L2.startsWith(Constants.FS))
			{
				// Need to append ./ on relative entries to load them properly
				path.append("./");
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
			else if (L2.startsWith(Constants.FS))
			{
				// We're about to lose this when we parse, so add it back...
				path.append(Constants.FS);
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
