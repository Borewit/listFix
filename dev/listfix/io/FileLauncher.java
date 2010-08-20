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

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class FileLauncher
{
	public static void launch(File toLaunch) throws IOException, InterruptedException
	{
		// try to figure out the OS so we can issue the correct command
		// TODO: Lots of debugging on different OSes to make sure this works.
		String lowerCaseOpSysName = System.getProperty("os.name").toLowerCase();
		String cmdLine = "";
		if (lowerCaseOpSysName.contains("windows"))
		{
			File tempFile = File.createTempFile("abc", ".bat");
			tempFile.deleteOnExit();

			// write out the .bat file...
			StringBuffer buffer = new StringBuffer("chcp 1252\nset myvar=\"");
			buffer.append(toLaunch.getCanonicalPath());
			buffer.append("\"\n");
			buffer.append("start \"\" %myvar%");

			FileOutputStream outputStream = new FileOutputStream(tempFile);
			Writer osw = new OutputStreamWriter(outputStream);
			osw.write(buffer.toString());
			osw.close();
			outputStream.close();

			cmdLine = tempFile.toString();

			Process proc = Runtime.getRuntime().exec(cmdLine);
			System.out.println("command was: " + cmdLine);
			synchronized (proc)
			{
				proc.wait(100);
			}
			InputStream stream = proc.getErrorStream();
			BufferedReader streamTwo = new BufferedReader(new InputStreamReader(stream));
			String line = null;
			if (streamTwo.ready())
			{
				line = streamTwo.readLine();
			}
			while (line != null)
			{
				System.out.println(line);
				if (streamTwo.ready())
				{
					line = streamTwo.readLine();
				}
				else
				{
					line = null;
				}
			}
		}
		else
		{
			Desktop desktop = null;
			if (Desktop.isDesktopSupported())
			{
				desktop = Desktop.getDesktop();
				if (desktop.isSupported(Desktop.Action.OPEN))
				{
					desktop.open(toLaunch);
				}
			}
		}
	}
}
