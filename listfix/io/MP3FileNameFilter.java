package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     MP3FileNameFilter.java
= Version:  1.0
= Purpose:  Simple instance of FilenameFilter that displays only
=           MP3 files.
============================================================================
*/

import java.io.File;

public class MP3FileNameFilter implements java.io.FilenameFilter
{    
    public boolean accept(File dir, String name)
    {
        if (name.trim().toLowerCase().endsWith(".mp3") )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public boolean accept(java.io.File file) 
    {
        return file.getName().toLowerCase().endsWith(".mp3");
    }
    
    public String getDescription() 
    {
        return "MP3 Files";
    }
}
    
    