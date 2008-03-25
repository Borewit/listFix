package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     ValidM3UFileRefFilenameFilter.java
= Version:  1.0
= Purpose:  Simple instance of FilenameFilter that displays only
=           audio files.
============================================================================
*/

import java.io.File;

public class ValidM3UFileRefFilenameFilter implements java.io.FilenameFilter
{
    private boolean endsWithValidExtension(String fileName)
    {
        // disallow nested playlists
        return !fileName.endsWith(".m3u");
    }
    
    public boolean accept(File dir, String name)
    {
        if (endsWithValidExtension(name.trim().toLowerCase()))
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
        return endsWithValidExtension(file.getName().toLowerCase());
    }
    
    public String getDescription() 
    {
        return "All Files Except M3Us";
    }
}    