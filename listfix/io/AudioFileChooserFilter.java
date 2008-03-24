package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     AudioFileChooserFilter.java
= Version:  1.0
= Purpose:  Simple instance of FilenameFilter that displays only
=           audio files or directories.
============================================================================
*/


public class AudioFileChooserFilter extends javax.swing.filechooser.FileFilter
{
    private boolean endsWithValidExtension(String fileName)
    {
        return (fileName.endsWith(".mp3") || 
            fileName.endsWith(".m4a") ||
            fileName.endsWith(".wma") ||
            fileName.endsWith(".ogg") ||
            fileName.endsWith(".flac") ||
            fileName.endsWith(".wav"));
    }
    
    public AudioFileChooserFilter()
    {

    }
    
    public boolean accept(java.io.File file) {
        return (endsWithValidExtension(file.getName().toLowerCase()) || file.isDirectory());
    }
    
    public String getDescription() {
        return "Audio Files (.mp3, .m4a, .wma, .ogg, .flac, .wav)";
    }
    
}
