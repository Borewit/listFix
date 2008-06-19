/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2008 Jeremy Caron
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

package listfix.model;

import javax.swing.tree.*;
import java.io.*;
import java.util.*;
import listfix.io.TreeNodeFile;

public class FileTreeNodeGenerator
{	
	/** Add nodes from under "dir" into curTop. Highly recursive. */
	public static DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir)
	{
		if (dir.exists())
		{
			String curPath = dir.getPath();
			TreeNodeFile file = new TreeNodeFile(curPath);
			DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(file);
			curDir.setUserObject(file);
			if (curTop != null)
			{
				curTop.add(curDir);
			}
			Vector ol = new Vector();
			String[] tmp = dir.list();
			if (tmp != null)
			{
				for (int i = 0; i < tmp.length; i++)
				{
					ol.addElement(tmp[i]);
				}
				Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
				File f;
				Vector files = new Vector();
				// Make two passes, one for Dirs and one for Files. This is #1.
				for (int i = 0; i < ol.size(); i++)
				{
					String thisObject = (String)ol.elementAt(i);
					String newPath;
					if (curPath.equals("."))
					{
						newPath = thisObject;
					}
					else
					{
						newPath = curPath + File.separator + thisObject;
					}
					if ((f = new File(newPath)).isDirectory())
					{
						addNodes(curDir, f);
					}
					else
					{
						if (thisObject.toLowerCase().indexOf(".m3u") >= 0)
						{
							files.addElement(curPath + File.separator + thisObject);
						}
					}
				}
				// Pass two: for files.
				for (int fnum = 0; fnum < files.size(); fnum++)
				{
					curDir.add(new DefaultMutableTreeNode(new TreeNodeFile((String)files.elementAt(fnum))));
				}
				return curDir;
			}
			return null;
		}
		return null;
	}
	
	public static String TreePathToFileSystemPath(TreePath node)
	{		
		return ((TreeNodeFile)((DefaultMutableTreeNode)node.getLastPathComponent()).getUserObject()).getPath();
	}
}