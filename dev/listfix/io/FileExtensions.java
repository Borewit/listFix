/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package listfix.io;

import java.io.File;

/**
 *
 * @author jcaron
 */
public class FileExtensions
{
	public static File findDeepestPathToExist(File file)
	{
		if (file == null || file.exists())
		{
			return file;
		}
		return findDeepestPathToExist(file.getParentFile());
	}
}
