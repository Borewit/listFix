package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     M3UFilter.java
= Version:  1.0
= Purpose:  Simple instance of FilenameFilter that displays only
=           M3U files or directories.
============================================================================
*/


public class M3UFilter extends javax.swing.filechooser.FileFilter
{
    
    public M3UFilter()
    {

    }
    
    public boolean accept(java.io.File file) {
        return (file.getName().endsWith(".m3u") || file.isDirectory());
    }
    
    public String getDescription() {
        return "Playlist Files (.m3u)";
    }
    
}
