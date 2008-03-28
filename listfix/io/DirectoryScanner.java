package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     DirectoryScanner.java
= Purpose:  To create and return a String array containing the absolute
=           paths to all of the subdirectories in a list of input
=           directories.
============================================================================
*/

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import listfix.controller.Task;

public class DirectoryScanner
{    
    private static Vector thisList = new Vector();
    private static Vector thisFileList = new Vector();
    private static String fs = System.getProperty("file.separator");
    
    public static void createMediaLibraryDirectoryAndFileList(String[] baseDirs, Task task)
    {
        thisList.clear();
        thisFileList.clear();
        for(int i = 0; i < baseDirs.length; i++)
        {
            if (new File(baseDirs[i]).exists())
            {
                thisList.addElement(baseDirs[i]);
                
                recursiveDir(baseDirs[i], task);
            }
        }
    }
    
    private static void recursiveDir(String baseDir, Task task)
    {
        task.setMessage("Scanning \"" + baseDir + "\"");
        
        File mediaDir = new File(baseDir);
        String[] entryList = mediaDir.list();
        List<String> fileList = new Vector<String>();
        List<String> dirList = new Vector<String>();
        StringBuilder s = new StringBuilder();
        
        if (entryList != null)
        {
            for (int i = 0; i < entryList.length; i++)
            {
                s.append(baseDir);
                s.append(fs);
                s.append(entryList[i]);
                File tempFile = new File(s.toString());
                if (tempFile.isDirectory())
                {
                    dirList.add(s.toString());
                }
                else
                {
                    fileList.add(s.toString());
                }
                s.setLength(0);
            }
        }
        
        Collections.sort(fileList);
        Collections.sort(dirList);
        
        for (String file: fileList)
        {
            thisFileList.add(file);
        }
        
        for (String dir: dirList)
        {
            thisList.addElement(dir);
            recursiveDir(dir, task);
        }
    }   
    
    public static String[] getFileList()
    {
        String[] result = new String[thisFileList.size()];
        thisFileList.copyInto(result);
        return result;
    }
    
    public static String[] getDirectoryList()
    {
        String[] result = new String[thisList.size()];
        thisList.copyInto(result);
        return result;
    }
}