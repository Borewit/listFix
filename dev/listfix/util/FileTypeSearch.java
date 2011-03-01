/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package listfix.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import listfix.comparators.FileComparator;

/**
 *
 * @author jcaron
 */
public class FileTypeSearch
{
	public List<File> findFiles(File directoryToSearch, FileFilter filter)
	{
		if (directoryToSearch.exists())
		{
			String curPath = directoryToSearch.getPath();
			File file = new File(curPath);

			List<File> ol = new ArrayList<File>();
			File[] inodes = directoryToSearch.listFiles(filter);

			if (inodes != null && inodes.length > 0)
			{
                ol.addAll(Arrays.asList(inodes));
				Collections.sort(ol, new FileComparator());
				File f;
				List<File> files = new ArrayList<File>();
				for (int i = 0; i < ol.size(); i++)
				{
					f = ol.get(i);
					if (f.isDirectory())
					{
						files.addAll(findFiles(f, filter));
					}
					else
					{
						files.add(f);
					}
				}

				return files;
			}
			return null;
		}
		return null;
	}
}
