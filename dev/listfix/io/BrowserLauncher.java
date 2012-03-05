/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2010 Jeremy Caron
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

import java.lang.reflect.Method;
import javax.swing.JOptionPane;
import listfix.util.OperatingSystem;
import listfix.view.controls.JTransparentTextArea;

public class BrowserLauncher
{
	private static final String errMsg = "Error attempting to launch web browser";

	public static void launch(String url)
	{
		try
		{
			if (OperatingSystem.isMac())
			{
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]
					{
						String.class
					});
				openURL.invoke(null, new Object[]
					{
						url
					});
			}
			else if (OperatingSystem.isWindows())
			{
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			}
			else
			{ //assume Unix or Linux
				String[] browsers =
				{
					"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"
				};
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++)
				{
					if (Runtime.getRuntime().exec(new String[]
						{
							"which", browsers[count]
						}).waitFor() == 0)
					{
						browser = browsers[count];
					}
				}
				if (browser == null)
				{
					throw new Exception("Could not find web browser");
				}
				else
				{
					Runtime.getRuntime().exec(new String[]
						{
							browser, url
						});
				}
			}
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, new JTransparentTextArea(errMsg + ": " + e.getLocalizedMessage()));
		}
	}
}
