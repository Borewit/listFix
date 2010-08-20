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

import java.io.*;
import java.util.*;
import javax.swing.tree.*;

import listfix.comparators.FileComparator;

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

			// if we're creating the root node here, use a regular file to get the full path to show.
			if (curTop == null)
			{
				curDir.setUserObject(new File(curPath));
			}

			Vector<File> ol = new Vector<File>();
			File[] inodes = dir.listFiles(new PlaylistFileFilter());

			if (inodes != null && inodes.length > 0)
			{
				for (int i = 0; i < inodes.length; i++)
				{
					ol.addElement(inodes[i]);
				}
				Collections.sort(ol, new FileComparator());
				File f;
				Vector<File> files = new Vector<File>();
				// Make two passes, one for Dirs and one for Files. This is #1.
				for (int i = 0; i < ol.size(); i++)
				{
					f = ol.elementAt(i);
					if (f.isDirectory())
					{
						File[] tmp = f.listFiles(new PlaylistFileFilter());
						if (tmp != null && tmp.length > 0)
						{
							addNodes(curDir, f);
						}
					}
					else
					{
						files.addElement(f);
					}
				}
				// Pass two: for files.
				for (int fnum = 0; fnum < files.size(); fnum++)
				{
					curDir.add(new DefaultMutableTreeNode(new TreeNodeFile(files.elementAt(fnum).getPath())));
				}
				if (curDir.children().hasMoreElements() || !((File) curDir.getUserObject()).isDirectory())
				{
					if (curTop != null)
					{
						curTop.add(curDir);
					}
				}
				return curDir;
			}
			return null;
		}
		return null;
	}

	public static String TreePathToFileSystemPath(TreePath node)
	{
		return ((File) ((DefaultMutableTreeNode) node.getLastPathComponent()).getUserObject()).getPath();
	}
}
