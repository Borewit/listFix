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

package listfix.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import listfix.io.UnicodeInputStream;
import org.apache.log4j.Logger;

public class UnicodeUtils
{
	private static final Logger _logger = Logger.getLogger(UnicodeUtils.class);

	public static byte[] convert(byte[] bytes, String encout) throws Exception
	{
		// Workaround for bug that will not be fixed by Sun/Oracle
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4508058
		UnicodeInputStream uis = new UnicodeInputStream(new ByteArrayInputStream(bytes), "ASCII");
		boolean unicodeOutputReqd = (getBOM(encout) != null) ? true : false;
		String enc = uis.getEncoding();
		String BOM = getBOM(enc); // get the BOM of the inputstream

		if (BOM == null)
		{
			// inputstream looks like ascii...
			// create a BOM based on the outputstream
			BOM = getBOM(encout);
		}
		uis.close();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes, uis.getBOMOffset(), bytes.length), enc));
		Writer w = new BufferedWriter(new OutputStreamWriter(out, encout));

		// dont write a BOM for ascii(out) as the OutputStreamWriter
		// will not process it correctly.
		if (BOM != null && unicodeOutputReqd)
		{
			w.write(BOM);
		}

		char[] buffer = new char[4096];
		int len;
		while ((len = br.read(buffer)) != -1)
		{
			w.write(buffer, 0, len);
		}

		br.close(); // Close the input.
		w.close(); // Flush and close output.
		return out.toByteArray();
	}

	public static String getBOM(String enc) throws UnsupportedEncodingException
	{
		if ("UTF-8".equals(enc))
		{
			byte[] bom = new byte[3];
			bom[0] = (byte) 0xEF;
			bom[1] = (byte) 0xBB;
			bom[2] = (byte) 0xBF;
			return new String(bom, enc);
		}
		else if ("UTF-16BE".equals(enc))
		{
			byte[] bom = new byte[2];
			bom[0] = (byte) 0xFE;
			bom[1] = (byte) 0xFF;
			return new String(bom, enc);
		}
		else if ("UTF-16LE".equals(enc))
		{
			byte[] bom = new byte[2];
			bom[0] = (byte) 0xFF;
			bom[1] = (byte) 0xFE;
			return new String(bom, enc);
		}
		else if ("UTF-32BE".equals(enc))
		{
			byte[] bom = new byte[4];
			bom[0] = (byte) 0x00;
			bom[1] = (byte) 0x00;
			bom[2] = (byte) 0xFE;
			bom[3] = (byte) 0xFF;
			return new String(bom, enc);
		}
		else if ("UTF-32LE".equals(enc))
		{
			byte[] bom = new byte[4];
			bom[0] = (byte) 0x00;
			bom[1] = (byte) 0x00;
			bom[2] = (byte) 0xFF;
			bom[3] = (byte) 0xFE;
			return new String(bom, enc);
		}
		else
		{
			return null;
		}
	}

	public static String getEncoding(File input)
	{
		if (input.isDirectory() || !input.canRead())
		{
			return null;
		}
		else
		{
			String encoding = "";
			UnicodeInputStream stream;
			try
			{
				stream = new UnicodeInputStream(new FileInputStream(input), "ASCII");
				encoding = stream.getEncoding();
				stream.close();
				return encoding;
			}
			catch (Exception ex)
			{
				_logger.warn(ExStack.toString(ex));
				return null;
			}
		}

	}
}
