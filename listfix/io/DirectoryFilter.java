package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     DirectoryFilter.java
= Version:  1.0
= Purpose:  Simple instance of FilenameFilter that displays only
=           directories.
============================================================================
*/

import java.io.File;

public class DirectoryFilter implements java.io.FilenameFilter
{
    
    public DirectoryFilter()
    {
    
    }
    
    public boolean accept(File dir, String name)
    {
        File tempFile = new File(dir, name);
        if (tempFile.isDirectory())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
    
    