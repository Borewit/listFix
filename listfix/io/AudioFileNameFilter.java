package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     AudioFileNameFilter.java
= Version:  1.0
= Purpose:  Simple instance of FilenameFilter that displays only
=           audio files.
============================================================================
*/

import java.io.File;

public class AudioFileNameFilter implements java.io.FilenameFilter
{
    private boolean endsWithValidExtension(String fileName)
    {
        return (fileName.endsWith(".mp3") || 
            fileName.endsWith(".m4a") ||
            fileName.endsWith(".mp4") ||
            fileName.endsWith(".wma") ||
            fileName.endsWith(".ogg") ||
            fileName.endsWith(".flac") ||
            fileName.endsWith(".wav"));
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
        return "Audio Files";
    }
}
    
    