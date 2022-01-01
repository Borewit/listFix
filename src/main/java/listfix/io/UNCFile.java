/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2014 Jeremy Caron
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
import listfix.util.ExStack;
import org.apache.log4j.Logger;
import pspdash.NetworkDriveList;

/**
 *
 * @author jcaron
 */
public class UNCFile extends File
{
  private static NetworkDriveList driveLister = new NetworkDriveList();
  private static final List<UNCFile> networkDrives = new ArrayList<>();
  private static final Logger _logger = Logger.getLogger(UNCFile.class);

  static
  {
    File[] roots = File.listRoots();
    for (File root : roots)
    {
      try
      {
        UNCFile file = new UNCFile(root);
        if (file.onNetworkDrive())
        {
          networkDrives.add(file);
        }
      }
      catch (Exception e)
      {
        // eat the error and continue
        _logger.error(ExStack.toString(e));
      }
    }
  }

  /**
   *
   * @param pathname
   */
  public UNCFile(String pathname)
  {
    super(pathname);
  }

  /**
   *
   * @param parent
   * @param child
   */
  public UNCFile(File parent, String child)
  {
    super(parent, child);
  }

  /**
   *
   * @param parent
   * @param child
   */
  public UNCFile(String parent, String child)
  {
    super(parent, child);
  }

  /**
   *
   * @param uri
   */
  public UNCFile(URI uri)
  {
    super(uri);
  }

  /**
   *
   * @param file
   */
  public UNCFile(File file)
  {
    super(file.getPath());
  }

  /**
   *
   * @return
   */
  public String getDrivePath()
  {
    String result = this.getPath();
    if (this.isInUNCFormat())
    {
      result = driveLister.fromUNCName(this.getAbsolutePath());
    }
    return result;
  }

  /**
   *
   * @return
   */
  public String getUNCPath()
  {
    String result = this.getPath();
    if (this.onNetworkDrive())
    {
      result = driveLister.toUNCName(this.getAbsolutePath());
    }
    return result;
  }

  /**
   *
   * @return
   */
  public boolean isInUNCFormat()
  {
    return this.getAbsolutePath().startsWith("\\\\");
  }

  /**
   *
   * @return
   */
  public static List<UNCFile> listMappedRoots()
  {
    return networkDrives;
  }

  /**
   * Return true if this file is not on a local hard drive.
   * @return True if the file is on a network drive, false otherwise.
   */
  public boolean onNetworkDrive()
  {
    return driveLister.onNetworkDrive(this.getAbsolutePath());
  }
}
