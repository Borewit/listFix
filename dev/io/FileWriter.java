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

/*
============================================================================
= Author:   Jeremy Caron
= File:     FileWriter.java
= Purpose:  Provides methods for writing a playlist to a file
=           and writing out the ini files for this program.
============================================================================
 */
import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;

import listfix.controller.tasks.WriteIniFileTask;
import listfix.model.AppOptions;
import listfix.model.M3UHistory;
import listfix.model.Playlist;
import listfix.model.PlaylistEntry;
import listfix.util.UnicodeUtils;

public class FileWriter
{
	private final String br = System.getProperty("line.separator");
	private final String fs = System.getProperty("file.separator");
	private final String homeDir = System.getProperty("user.home");

	public String getRelativePath(File file, File relativeTo)
	{
		try
		{
			StringTokenizer fileTizer = new StringTokenizer(file.getAbsolutePath(), fs);
			StringTokenizer relativeToTizer = new StringTokenizer(relativeTo.getAbsolutePath(), fs);
			Vector<String> fileTokens = new Vector<String>();
			Vector<String> relativeToTokens = new Vector<String>();
			while (fileTizer.hasMoreTokens())
			{
				fileTokens.add(fileTizer.nextToken());
			}
			while (relativeToTizer.hasMoreTokens())
			{
				relativeToTokens.add(relativeToTizer.nextToken());
			}

			// throw away last token from each, don't need the file names for path calculation.
			String fileName = "";
			if (file.isFile())
			{
				fileName = fileTokens.remove(fileTokens.size() - 1);
			}

			// relativeTo is the M3U we'll be writing to, we need to remove the last token regardless...
			relativeToTokens.removeElementAt(relativeToTokens.size() - 1);

			int maxSize = fileTokens.size() >= relativeToTokens.size() ? relativeToTokens.size() : fileTokens.size();
			boolean tokenMatch = false;
			for (int i = 0; i < maxSize; i++)
			{
				if (fileTokens.get(i).equals(relativeToTokens.get(i)))
				{
					tokenMatch = true;
					fileTokens.remove(i);
					relativeToTokens.remove(i);
					i--;
					maxSize--;
				}
				else if (tokenMatch == false)
				{
					// files can not be made relative to one another.
					return file.getAbsolutePath();
				}
				else
				{
					break;
				}
			}

			StringBuffer resultBuffer = new StringBuffer();
			for (int i = 0; i < relativeToTokens.size(); i++)
			{
				resultBuffer.append("..").append(fs);
			}

			for (int i = 0; i < fileTokens.size(); i++)
			{
				resultBuffer.append(fileTokens.get(i)).append(fs);
			}

			resultBuffer.append(fileName);

			return resultBuffer.toString();
		}
		catch (Exception e)
		{
			return file.getAbsolutePath();
		}
	}

	public void writeDefaultIniFilesIfNeeded()
	{
		BufferedWriter output;
		FileOutputStream outputStream;

		File testFile = new File(homeDir + fs + "dirLists.ini");
		if (!testFile.exists() || (testFile.exists() && testFile.length() == 0))
		{
			try
			{
				StringBuffer buffer = new StringBuffer();
				AppOptions options = new AppOptions();
				outputStream = new FileOutputStream(homeDir + fs + "dirLists.ini");
				Writer osw = new OutputStreamWriter(outputStream, "UTF8");
				output = new BufferedWriter(osw);
				buffer.append("[Media Directories]" + br);
				buffer.append("[Options]" + br);
				buffer.append("AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD=" + Boolean.toString(options.getAutoLocateEntriesOnPlaylistLoad()) + br);
				buffer.append("MAX_PLAYLIST_HISTORY_SIZE=" + options.getMaxPlaylistHistoryEntries() + br);
				buffer.append("SAVE_RELATIVE_REFERENCES=" + Boolean.toString(options.getSavePlaylistsWithRelativePaths()) + br);
				buffer.append("AUTO_REFRESH_MEDIA_LIBRARY_ON_LOAD=" + Boolean.toString(options.getAutoRefreshMediaLibraryOnStartup()) + br);
				buffer.append("LOOK_AND_FEEL=" + options.getLookAndFeel() + br);
				buffer.append("ALWAYS_USE_UNC_PATHS=" + Boolean.toString(options.getAlwaysUseUNCPaths()) + br);
				buffer.append("PLAYLISTS_DIRECTORY=" + options.getPlaylistsDirectory() + br);
				buffer.append("[Media Library Directories]" + br);
				buffer.append("[Media Library Files]" + br);
				output.write(buffer.toString());
				output.close();
				outputStream.close();
			}
			catch (Exception e)
			{
				// eat the error and continue
				e.printStackTrace();
			}
		}

		testFile = new File(homeDir + fs + "listFixHistory.ini");
		if (!testFile.exists() || (testFile.exists() && testFile.length() == 0))
		{
			try
			{
				outputStream = new FileOutputStream(homeDir + fs + "listFixHistory.ini");
				Writer osw = new OutputStreamWriter(outputStream, "UTF8");
				output = new BufferedWriter(osw);
				output.write(UnicodeUtils.getBOM("UTF-8") + "[Recent Playlists]" + br);
				output.close();
				outputStream.close();
			}
			catch (Exception e)
			{
				// eat the error and continue
				e.printStackTrace();
			}
		}
	}

	public Vector<PlaylistEntry> writeM3U(Playlist list, File fileName) throws Exception
	{
		Vector<PlaylistEntry> entries = list.getEntries();
		PlaylistEntry tempEntry = null;
		StringBuffer buffer = new StringBuffer();
		buffer.append("#EXTM3U" + br);
		for (int i = 0; i < entries.size(); i++)
		{
			tempEntry = entries.elementAt(i);
			if (tempEntry.isRelative() && tempEntry.getAbsoluteFile() != null)
			{
				tempEntry = new PlaylistEntry(tempEntry.getAbsoluteFile().getCanonicalFile(), tempEntry.getExtInf());
				buffer.append(tempEntry.toM3UString() + br);
				entries.remove(i);
				entries.add(i, tempEntry);
			}
			else
			{
				buffer.append(tempEntry.toM3UString() + br);
			}
		}

		File dirToSaveIn = fileName.getParentFile().getAbsoluteFile();
		if (!dirToSaveIn.exists())
		{
			dirToSaveIn.mkdirs();
		}

		if (list.isUtfFormat() || fileName.getName().toLowerCase().endsWith("m3u8"))
		{
			FileOutputStream outputStream = new FileOutputStream(fileName);
			Writer osw = new OutputStreamWriter(outputStream, "UTF8");
			BufferedWriter output = new BufferedWriter(osw);
			output.write(UnicodeUtils.getBOM("UTF-8") + buffer.toString());
			output.close();
			outputStream.close();
			list.setUtfFormat(true);
		}
		else
		{
			FileOutputStream outputStream = new FileOutputStream(fileName);
			BufferedOutputStream output = new BufferedOutputStream(outputStream);
			output.write(buffer.toString().getBytes());
			output.close();
			outputStream.close();
			list.setUtfFormat(false);
		}
		return entries;
	}

	public Vector<PlaylistEntry> writeRelativeM3U(Playlist list, File fileName) throws Exception
	{
		Vector<PlaylistEntry> entries = list.getEntries();
		PlaylistEntry tempEntry = null;
		StringBuffer buffer = new StringBuffer();
		buffer.append("#EXTM3U" + br);
		for (int i = 0; i < entries.size(); i++)
		{
			tempEntry = entries.elementAt(i);
			if (!tempEntry.isRelative() && !tempEntry.isURL())
			{
				if (!tempEntry.getExtInf().isEmpty())
				{
					buffer.append(tempEntry.getExtInf() + br);
				}
				String relPath = getRelativePath(tempEntry.getFile().getAbsoluteFile(), fileName);
				buffer.append(relPath + br);
				// replace the existing entry with a new relative one...
				entries.remove(i);
				entries.add(i, new PlaylistEntry(new File(relPath), tempEntry.getExtInf()));
			}
			else
			{
				buffer.append(tempEntry.toM3UString() + br);
			}
		}

		if (list.isUtfFormat() || fileName.getName().toLowerCase().endsWith("m3u8"))
		{
			FileOutputStream outputStream = new FileOutputStream(fileName);
			Writer osw = new OutputStreamWriter(outputStream, "UTF8");
			BufferedWriter output = new BufferedWriter(osw);
			output.write(UnicodeUtils.getBOM("UTF-8") + buffer.toString());
			output.close();
			outputStream.close();
			list.setUtfFormat(true);
		}
		else
		{
			FileOutputStream outputStream = new FileOutputStream(fileName);
			BufferedOutputStream output = new BufferedOutputStream(outputStream);
			output.write(buffer.toString().getBytes());
			output.close();
			outputStream.close();
			list.setUtfFormat(false);
		}
		return entries;
	}


	public Vector<PlaylistEntry> writePLS(Playlist list, File fileName) throws Exception
	{
		Vector<PlaylistEntry> entries = list.getEntries();
		PlaylistEntry tempEntry = null;
		StringBuffer buffer = new StringBuffer();
		buffer.append("[playlist]" + br);
		for (int i = 0; i < entries.size(); i++)
		{
			tempEntry = entries.elementAt(i);
			if (tempEntry.isRelative() && tempEntry.getAbsoluteFile() != null)
			{
				tempEntry = new PlaylistEntry(tempEntry.getAbsoluteFile().getCanonicalFile(), tempEntry.getTitle(), tempEntry.getLength());
				buffer.append(tempEntry.toPLSString(i + 1));
				entries.remove(i);
				entries.add(i, tempEntry);
			}
			else
			{
				buffer.append(tempEntry.toPLSString(i + 1));
			}
		}

		buffer.append("NumberOfEntries=" + list.getEntryCount() + br);
		buffer.append("Version=2");

		File dirToSaveIn = fileName.getParentFile().getAbsoluteFile();
		if (!dirToSaveIn.exists())
		{
			dirToSaveIn.mkdirs();
		}

		if (list.isUtfFormat())
		{
			FileOutputStream outputStream = new FileOutputStream(fileName);
			Writer osw = new OutputStreamWriter(outputStream, "UTF8");
			BufferedWriter output = new BufferedWriter(osw);
			output.write(UnicodeUtils.getBOM("UTF-8") + buffer.toString());
			output.close();
			outputStream.close();
			list.setUtfFormat(true);
		}
		else
		{
			FileOutputStream outputStream = new FileOutputStream(fileName);
			BufferedOutputStream output = new BufferedOutputStream(outputStream);
			output.write(buffer.toString().getBytes());
			output.close();
			outputStream.close();
			list.setUtfFormat(false);
		}
		return entries;
	}

	public Vector<PlaylistEntry> writeRelativePLS(Playlist list, File fileName) throws Exception
	{
		Vector<PlaylistEntry> entries = list.getEntries();
		PlaylistEntry tempEntry = null;
		StringBuffer buffer = new StringBuffer();
		buffer.append("[playlist]" + br);
		for (int i = 0; i < entries.size(); i++)
		{
			tempEntry = entries.elementAt(i);
			if (!tempEntry.isRelative() && !tempEntry.isURL())
			{
				String relPath = getRelativePath(tempEntry.getFile().getAbsoluteFile(), fileName);
				buffer.append("File" + (i + 1) + "=" + relPath + br);
				buffer.append("Title" + (i + 1) + "=" + tempEntry.getTitle() + br);
				buffer.append("Length" + (i + 1) + "=" + tempEntry.getLength() + br);
				// replace the existing entry with a new relative one...
				entries.remove(i);
				entries.add(i, new PlaylistEntry(new File(relPath), tempEntry.getTitle(), tempEntry.getLength()));
			}
			else
			{
				buffer.append(tempEntry.toPLSString(i + 1));
			}
		}

		buffer.append("NumberOfEntries=" + list.getEntryCount() + br);
		buffer.append("Version=2");

		if (list.isUtfFormat())
		{
			FileOutputStream outputStream = new FileOutputStream(fileName);
			Writer osw = new OutputStreamWriter(outputStream, "UTF8");
			BufferedWriter output = new BufferedWriter(osw);
			output.write(UnicodeUtils.getBOM("UTF-8") + buffer.toString());
			output.close();
			outputStream.close();
			list.setUtfFormat(true);
		}
		else
		{
			FileOutputStream outputStream = new FileOutputStream(fileName);
			BufferedOutputStream output = new BufferedOutputStream(outputStream);
			output.write(buffer.toString().getBytes());
			output.close();
			outputStream.close();
			list.setUtfFormat(false);
		}
		return entries;
	}

	public void writeMruPlaylists(M3UHistory history)
	{
		try
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append("[Recent Playlists]" + br);
			String[] filenames = history.getM3UFilenames();
			for (int i = 0; i < filenames.length; i++)
			{
				buffer.append(filenames[i] + br);
			}
			FileOutputStream outputStream = new FileOutputStream(homeDir + fs + "listFixHistory.ini");
			Writer osw = new OutputStreamWriter(outputStream, "UTF8");
			BufferedWriter output = new BufferedWriter(osw);
			output.write(UnicodeUtils.getBOM("UTF-8") + buffer.toString());
			output.close();
			outputStream.close();
		}
		catch (IOException e)
		{
			// eat the error and continue
			e.printStackTrace();
		}
	}

	public void writeIni(String[] mediaDir, String[] mediaLibraryDirList, String[] mediaLibraryFileList, AppOptions options)
	{
		try
		{
			WriteIniFileTask thisTask = new WriteIniFileTask(mediaDir, mediaLibraryDirList, mediaLibraryFileList, options);
			thisTask.start();
		}
		catch (Exception e)
		{
			// eat the error and continue
			e.printStackTrace();
		}
	}
}
        