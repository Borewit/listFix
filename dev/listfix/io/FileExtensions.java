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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import listfix.controller.GUIDriver;

/**
 *
 * @author jcaron
 */
public class FileExtensions
{
	public static File findDeepestPathToExist(File file)
	{
		if (file == null || file.exists())
		{
			return file;
		}
		return findDeepestPathToExist(file.getParentFile());
	}

	public static String getRelativePath(File file, File relativeTo)
	{
		try
		{
			UNCFile unc1 = new UNCFile(file);
			UNCFile unc2 = new UNCFile(relativeTo);
			StringTokenizer fileTizer;
			StringTokenizer relativeToTizer;
			if (unc1.onNetworkDrive())
			{
				fileTizer = new StringTokenizer(unc1.getUNCPath(), Constants.FS);
			}
			else
			{
				fileTizer = new StringTokenizer(file.getAbsolutePath(), Constants.FS);
			}
			if (unc2.onNetworkDrive())
			{
				relativeToTizer = new StringTokenizer(unc2.getUNCPath(), Constants.FS);
			}
			else
			{
				relativeToTizer = new StringTokenizer(relativeTo.getAbsolutePath(), Constants.FS);
			}
			List<String> fileTokens = new ArrayList<String>();
			List<String> relativeToTokens = new ArrayList<String>();
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

			// relativeTo is the playlist we'll be writing to, we need to remove the last token regardless...
			relativeToTokens.remove(relativeToTokens.size() - 1);

			int maxSize = fileTokens.size() >= relativeToTokens.size() ? relativeToTokens.size() : fileTokens.size();
			boolean tokenMatch = false;
			for (int i = 0; i < maxSize; i++)
			{
				if (GUIDriver.fileSystemIsCaseSensitive ? fileTokens.get(i).equals(relativeToTokens.get(i)) : fileTokens.get(i).equalsIgnoreCase(relativeToTokens.get(i)))
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

			StringBuilder resultBuffer = new StringBuilder();
			for (int i = 0; i < relativeToTokens.size(); i++)
			{
				resultBuffer.append("..").append(Constants.FS);
			}

			for (int i = 0; i < fileTokens.size(); i++)
			{
				resultBuffer.append(fileTokens.get(i)).append(Constants.FS);
			}

			resultBuffer.append(fileName);

			return resultBuffer.toString();
		}
		catch (Exception e)
		{
			// not logging anything here as this seems to be a common fallback...
			return file.getAbsolutePath();
		}
	}
	
	public static String replaceInvalidWindowsFileSystemCharsWithChar(String input, char replacement)
	{
		StringBuilder result = new StringBuilder();
		for (char x : input.toCharArray())
		{
			if (Constants.INVALID_WINDOWS_FILENAME_CHARACTERS.indexOf(x) > -1)
			{
				result.append(replacement);
			}
			else
			{
				result.append(x);
			}
		}
		return result.toString();
	}
	
	public static void deleteDirectory(File dir)
	{
		if (dir.isDirectory())
		{
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].isDirectory())
				{
					deleteDirectory(files[i]);
					files[i].delete();
				}
				else
				{
					files[i].delete();
				}
			}
			dir.delete();
		}
		if (dir.exists())
		{
			deleteDirectory(dir);
		}
	}
}
