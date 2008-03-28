package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     ValidM3UFileRefFileChooserFilter.java
= Purpose:  Simple instance of FilenameFilter that displays only
=           audio files or directories.
============================================================================
*/

public class ValidM3UFileRefFileChooserFilter extends javax.swing.filechooser.FileFilter
{
    private boolean endsWithValidExtension(String fileName)
    {
        // disallow nested playlists
        return !fileName.endsWith(".m3u");
    }
    
    public ValidM3UFileRefFileChooserFilter()
    {

    }
    
    public boolean accept(java.io.File file) 
    {
        return (endsWithValidExtension(file.getName().toLowerCase()) || file.isDirectory());
    }
    
    public String getDescription() 
    {
        return "All Files (except M3Us)";
    }    
}