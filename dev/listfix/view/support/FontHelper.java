/*
 * listFix() - Fix Broken Playlists!
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

package listfix.view.support;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

/**
 *
 * @author jcaron
 */
public class FontHelper
{
	public static void setFileChooserFont(Component[] comp)
	{
		for (int x = 0; x < comp.length; x++)
		{
			if (comp[x] instanceof Container)
			{
				setFileChooserFont(((Container) comp[x]).getComponents());
			}
			try
			{
				comp[x].setFont(new Font("Verdana", 0, 9));
			}
			catch (Exception e)
			{
				// keep going...
			}
		}
	}
}
