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
    private Vector thisDirList;
    private Vector thisFileList;
    private final String fs = System.getProperty("file.separator");
    private int recursiveCount = 0;
    
    public void createMediaLibraryDirectoryAndFileList(String[] baseDirs, Task task)
    {
        this.reset();
        for(int i = 0; i < baseDirs.length; i++)
        {
            if (new File(baseDirs[i]).exists())
            {
                thisDirList.addElement(baseDirs[i]);                
                this.recursiveDir(baseDirs[i], task);
            }
        }
    }
    
    private void recursiveDir(String baseDir, Task task)
    {
        recursiveCount++;
        task.setMessage("<html><body>Scanning Directory #" + recursiveCount + "<BR><BR>" + (baseDir.length() < 70 ? baseDir : baseDir.substring(0, 70) + "...") + "</body></html>");
        
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
            thisDirList.addElement(dir);
            recursiveDir(dir, task);
        }
    }   
    
    public void reset()
    {
        recursiveCount = 0;
        thisDirList = new Vector();
        thisFileList = new Vector();
    }
    
    public String[] getFileList()
    {
        String[] result = new String[thisFileList.size()];
        thisFileList.copyInto(result);
        return result;
    }
    
    public String[] getDirectoryList()
    {
        String[] result = new String[thisDirList.size()];
        thisDirList.copyInto(result);
        return result;
    }
}