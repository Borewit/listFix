package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     FileNameFilter.java
= Purpose:  Simple instance of FilenameFilter that displays only
=           files matching the input file name (used to search for
=           a file).
============================================================================
*/

import java.io.File;

public class FileNameFilter implements java.io.FilenameFilter
{
    
    private String lostFileName;
    
    public FileNameFilter(String l)
    {
        lostFileName = l;
    }
    
    public boolean accept(File dir, String name)
    {
        if (name.equalsIgnoreCase(lostFileName))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
    
    