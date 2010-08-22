
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
