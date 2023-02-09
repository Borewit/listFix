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

package listfix.util;

import listfix.io.UnicodeInputStream;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * @author jcaron
 */
public class UnicodeUtils
{
  public static String getBOM(String enc) throws UnsupportedEncodingException
  {
    switch (enc)
    {
      case "UTF-8" ->
      {
        byte[] bom = new byte[3];
        bom[0] = (byte) 0xEF;
        bom[1] = (byte) 0xBB;
        bom[2] = (byte) 0xBF;
        return new String(bom, enc);
      }
      case "UTF-16BE" ->
      {
        byte[] bom = new byte[2];
        bom[0] = (byte) 0xFE;
        bom[1] = (byte) 0xFF;
        return new String(bom, enc);
      }
      case "UTF-16LE" ->
      {
        byte[] bom = new byte[2];
        bom[0] = (byte) 0xFF;
        bom[1] = (byte) 0xFE;
        return new String(bom, enc);
      }
      case "UTF-32BE" ->
      {
        byte[] bom = new byte[4];
        bom[0] = (byte) 0x00;
        bom[1] = (byte) 0x00;
        bom[2] = (byte) 0xFE;
        bom[3] = (byte) 0xFF;
        return new String(bom, enc);
      }
      case "UTF-32LE" ->
      {
        byte[] bom = new byte[4];
        bom[0] = (byte) 0x00;
        bom[1] = (byte) 0x00;
        bom[2] = (byte) 0xFF;
        bom[3] = (byte) 0xFE;
        return new String(bom, enc);
      }
    }
    throw new UnsupportedEncodingException("");
  }

  public static Charset getEncoding(Path path)
  {
    return getEncoding(path.toFile());
  }

  public static Charset getEncoding(File input)
  {
    try (UnicodeInputStream stream = new UnicodeInputStream(new FileInputStream(input), "ASCII"))
    {
      return Charset.forName(stream.getEncoding());
    }
    catch (IOException ioException)
    {
      throw new RuntimeException("Failed to determine encoding", ioException);
    }
  }
}
