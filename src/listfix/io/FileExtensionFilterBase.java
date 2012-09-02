/**
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

package listfix.io;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.filechooser.FileFilter;

public abstract class FileExtensionFilterBase extends FileFilter
{
    protected FileExtensionFilterBase(Set<String> extensions)
    {
        _extensions = extensions;
    }

    @Override
    public boolean accept(File file)
    {
        if (file.isDirectory())
            return true;

        String name = file.getName();
        int ix = name.lastIndexOf('.');
        if (ix >= 0 && ix < name.length() - 1)
        {
            String ext = name.substring(ix + 1).toLowerCase();
            return _extensions.contains(ext);
        }
        else
            return false;
    }
    private Set<String> _extensions;

    protected static Set<String> createExtensionSet(String... extensions)
    {
        Set<String> set = new HashSet<String>(Arrays.asList(extensions));
        return set;
    }

}
