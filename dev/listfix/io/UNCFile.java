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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import pspdash.NetworkDriveList;

/**
 *
 * @author jcaron
 */
public class UNCFile extends File
{
	private static NetworkDriveList driveLister = new NetworkDriveList();
	private static final List<UNCFile> networkDrives = new ArrayList<UNCFile>();

	static
	{
		File[] roots = File.listRoots();
		for (int i = 0; i < roots.length; i++)
		{
			try
			{
				UNCFile file = new UNCFile(roots[i]);
				if (file.onNetworkDrive())
				{
					networkDrives.add(file);
				}
			}
			catch (Exception e)
			{
				// eat the error and continue
				e.printStackTrace();
			}
		}
	}

	public UNCFile(String pathname)
	{
		super(pathname);
	}

	public UNCFile(File parent, String child)
	{
		super(parent, child);
	}

	public UNCFile(String parent, String child)
	{
		super(parent, child);
	}

	public UNCFile(URI uri)
	{
		super(uri);
	}

	public UNCFile(File file)
	{
		super(file.getPath());
	}

	public String getDrivePath()
	{
		String result = this.getPath();
		if (this.isInUNCFormat())
		{
			result = driveLister.fromUNCName(this.getAbsolutePath());
		}
		return result;
	}

	public String getUNCPath()
	{
		String result = this.getPath();
		if (this.onNetworkDrive())
		{
			result = driveLister.toUNCName(this.getAbsolutePath());
		}
		return result;
	}

	public boolean isInUNCFormat()
	{
		return this.getAbsolutePath().startsWith("\\\\");
	}

	public static List<UNCFile> listMappedRoots()
	{
		return networkDrives;
	}

	public boolean onNetworkDrive()
	{
		return driveLister.onNetworkDrive(this.getAbsolutePath());
	}
}
