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

package listfix.io.writers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import listfix.util.ExStack;

import org.apache.log4j.Logger;

/** Copies a source file to the destination file. */
public class FileCopier
{
  /** The block size to read in one shot. */
  private static final int blockSize = 1024;
  private static final Logger _logger = Logger.getLogger(FileCopier.class);

  /** Copies from source to destination.
   * @param input The source file.
   * @param output The destination file.
   * @exception IOException
   */
  public static void copy(File input, File output) throws IOException
  {
    FileInputStream src = new FileInputStream(input);
    FileOutputStream dest = new FileOutputStream(output);

    BufferedInputStream in = null;
    BufferedOutputStream out = null;

    try
    {
      in = new BufferedInputStream(src);
      out = new BufferedOutputStream(dest);
      int numRead;
      byte[] buf = new byte[blockSize];
      while ((numRead = in.read(buf, 0, buf.length)) != -1)
      {
        out.write(buf, 0, numRead);
      }
    }
    finally
    {
      try
      {
        if (in != null)
        {
          in.close();
        }
        if (out != null)
        {
          out.close();
        }
        if (src != null)
        {
          src.close();
        }
        if (dest != null)
        {
          dest.close();
        }
      }
      catch (Exception ex)
      {
        _logger.error(ExStack.toString(ex));
      }
    }
  }
}
