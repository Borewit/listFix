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

import java.lang.reflect.Method;
import javax.swing.JOptionPane;

import listfix.util.ExStack;
import listfix.util.OperatingSystem;
import listfix.view.controls.JTransparentTextArea;

import org.apache.log4j.Logger;

/**
 * A class who's sole responsibility is to launch the default web browser on various OSes.
 * @author jcaron
 */
public class BrowserLauncher
{
	private static final String _errMsg = "Error attempting to launch web browser";
	private static final Logger _logger = Logger.getLogger(BrowserLauncher.class);

	/**
	 * Attempts to open the given url in the system's default web browser.
	 * Logs failure details and silently fails if a browser can't be opened.
	 * @param url The URL to display in the browser, if successfully launched.
	 */
	public static void launch(String url)
	{
		try
		{
			if (OperatingSystem.isMac())
			{
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
				openURL.invoke(null, new Object[] { url });
			}
			else if (OperatingSystem.isWindows())
			{
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			}
			else
			{ 
				//assume Unix or Linux
				String[] possibleBrowsers =
				{
					"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"
				};
				String browser = null;
				for (int count = 0; count < possibleBrowsers.length && browser == null; count++)
				{
					if (Runtime.getRuntime().exec(new String[] {"which", possibleBrowsers[count]}).waitFor() == 0)
					{
						browser = possibleBrowsers[count];
					}
				}
				if (browser == null)
				{
					throw new Exception("Could not find web browser");
				}
				else
				{
					Runtime.getRuntime().exec(new String[] { browser, url });
				}
			}
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, new JTransparentTextArea(_errMsg + ": " + e.getLocalizedMessage()));
			_logger.info(ExStack.toString(e));
		}
	}
}
