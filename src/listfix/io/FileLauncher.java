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

import java.awt.Desktop;
import java.io.*;
import listfix.util.OperatingSystem;

public class FileLauncher
{
	public static void launch(File toLaunch) throws IOException, InterruptedException
	{
		// try to figure out the OS so we can issue the correct command
		if (OperatingSystem.isWindows())
		{
			File tempFile = File.createTempFile("abc", ".bat");
			tempFile.deleteOnExit();

			// write out the .bat file...
			StringBuilder buffer = new StringBuilder("chcp 1252\nset myvar=\"");
			buffer.append(toLaunch.getCanonicalPath());
			buffer.append("\"\n");
			buffer.append("start \"\" %myvar%");

			FileOutputStream outputStream = new FileOutputStream(tempFile);
			Writer osw = new OutputStreamWriter(outputStream);
			osw.write(buffer.toString());
			osw.close();
			outputStream.close();

			String cmdLine = tempFile.toString();

			Process proc = Runtime.getRuntime().exec(cmdLine);			
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
			if (Desktop.isDesktopSupported())
			{
				Desktop desktop = Desktop.getDesktop();
				if (desktop.isSupported(Desktop.Action.OPEN))
				{
					desktop.open(toLaunch);
				}
			}
		}
	}
}
