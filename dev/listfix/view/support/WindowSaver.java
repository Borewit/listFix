/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2010 Jeremy Caron
 * 
 *  This file is part of listFix().
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.view.support;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.swing.JFrame;
import listfix.io.Constants;
import listfix.util.ExStack;
import org.apache.log4j.Logger;

/**
 *
 * @author jcaron
 */
public class WindowSaver implements AWTEventListener
{
	private static final String PROP_FILE = Constants.DATA_DIR + "position.ini";
	private static WindowSaver saver;
	private Map framemap;
	private static final Logger _logger = Logger.getLogger(WindowSaver.class);

	private WindowSaver()
	{
		framemap = new HashMap();
	}

	public static WindowSaver getInstance()
	{
		if (saver == null)
		{
			saver = new WindowSaver();
		}
		return saver;
	}

	@Override
	public void eventDispatched(AWTEvent evt)
	{
		try
		{
			if (evt.getID() == WindowEvent.WINDOW_OPENED)
			{
				ComponentEvent cev = (ComponentEvent) evt;
				if (cev.getComponent() instanceof JFrame)
				{
					JFrame frame = (JFrame) cev.getComponent();
					loadSettings(frame);
				}
			}
		}
		catch (Exception ex)
		{
			_logger.warn(ExStack.toString(ex));
		}
	}

	public void loadSettings(JFrame frame)
	{
		Properties settings = new Properties();
		String name = frame.getName();
		try
		{
			settings.load(new FileInputStream(PROP_FILE));
			int x = getInt(settings, name + ".x", 100);
			int y = getInt(settings, name + ".y", 100);
			int w = getInt(settings, name + ".w", 500);
			int h = getInt(settings, name + ".h", 500);
			frame.setLocation(x, y);
			frame.setSize(new Dimension(w, h));
		}
		catch (IOException ex)
		{
			_logger.info(ExStack.toString(ex));
		}
		saver.framemap.put(name, frame);
		frame.validate();
	}

	public int getInt(Properties props, String name, int value)
	{
		String v = props.getProperty(name);
		if (v == null)
		{
			return value;
		}
		return Integer.parseInt(v);
	}

	public void saveSettings()
	{
		Properties settings = new Properties();
		try
		{
			settings.load(new FileInputStream(PROP_FILE));
		}
		catch (IOException ex)
		{
			_logger.info(ExStack.toString(ex));
		}

		Iterator it = saver.framemap.keySet().iterator();
		while (it.hasNext())
		{
			String name = (String) it.next();
			JFrame frame = (JFrame) saver.framemap.get(name);
			settings.setProperty(name + ".x", "" + frame.getX());
			settings.setProperty(name + ".y", "" + frame.getY());
			settings.setProperty(name + ".w", "" + frame.getWidth());
			settings.setProperty(name + ".h", "" + frame.getHeight());
		}
		try
		{
			settings.store(new FileOutputStream(PROP_FILE), null);
		}
		catch (IOException ex)
		{
			_logger.error(ExStack.toString(ex));
		}
	}

}
