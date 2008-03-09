package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     MP3FileChooserFilter.java
= Version:  1.0
= Purpose:  Simple instance of FilenameFilter that displays only
=           MP3 files or directories.
============================================================================
*/


public class MP3FileChooserFilter extends javax.swing.filechooser.FileFilter
{
    
    public MP3FileChooserFilter()
    {

    }
    
    public boolean accept(java.io.File file) {
        return (file.getName().endsWith(".mp3") || file.isDirectory());
    }
    
    public String getDescription() {
        return "MP3 Files (.mp3)";
    }
    
}
