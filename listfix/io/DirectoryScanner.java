package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     DirectoryScanner.java
= Version:  1.0
= Purpose:  To create and return a String array containing the absolute
=           paths to all of the subdirectories in a list of input
=           directories.
============================================================================
*/

import java.io.File;
import java.util.Vector;

public class DirectoryScanner
{    
    private static Vector thisList = new Vector();
    private static Vector thisFileList = new Vector();
    private static String fs = System.getProperty("file.separator");
    
    public static String[] createMediaLibraryDirectoryList(String[] baseDir)
    {
        thisList.clear();
        for(int i = 0; i < baseDir.length; i++)
        {
            if (new File(baseDir[i]).exists())
            {
                thisList.addElement(baseDir[i]);
                recursiveDir(baseDir[i]);
            }
        }
        String[] result = new String[thisList.size()];
        thisList.copyInto(result);
        return result;
    }
    
    public static String[] createMediaLibraryFileList(String[] dirs)
    {
        thisFileList.clear();        
        for (int i = 0; i < dirs.length; i++)
        {
            File mediaDir = new File(dirs[i]);
            try
            {
                String[] files = mediaDir.list(new AudioFileNameFilter());
                if (files != null & files.length > 0)
                {
                    StringBuilder s = new StringBuilder();
                    for (int j = 0; j < files.length; j++)
                    {
                        s.append(dirs[i]);
                        s.append(fs);
                        s.append(files[j]);
                        thisFileList.add(s.toString());
                        s.setLength(0);
                    }
                }
            }
            catch (Exception e)
            {
                // eat the error and continue... faster than an exists check.
            }
        }
        String[] result = new String[thisFileList.size()];
        thisFileList.copyInto(result);
        return result;
    }
    
    private static void recursiveDir(String baseDir)
    {
        File mediaDir = new File(baseDir);
        String[] subDirs = mediaDir.list(new DirectoryFilter());
        if (subDirs != null)
        {
            if (subDirs.length != 0)
            {
                copyDirsToVector(baseDir, subDirs);
            }
            for (int i = 0; i < subDirs.length; i++)
            {
                StringBuilder s = new StringBuilder(baseDir);
                s.append(fs);
                s.append(subDirs[i]);
                recursiveDir(s.toString());
            }
        }
    }
    
    private static void copyDirsToVector(String b, String[] dirs)
    {
        for(int i = 0; i < dirs.length; i++)
        {
            StringBuilder s = new StringBuilder(b);
            s.append(fs);
            s.append(dirs[i]);
            thisList.addElement(s.toString());
        }
    }    
}