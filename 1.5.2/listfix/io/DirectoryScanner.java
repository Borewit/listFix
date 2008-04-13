/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2008 Jeremy Caron
 * 
 * This file is part of listFix().
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, please see http://www.gnu.org/licenses/
 */

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
    private Vector<String> thisDirList;
    private Vector<String> thisFileList;
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

    public static boolean endsWithIndexedExtension(String input)
    {
        input = input.toLowerCase();
        return (input.endsWith(".mp3")  || input.endsWith(".wma")  ||
                input.endsWith(".flac") || input.endsWith(".ogg")  ||
                input.endsWith(".wav")  || input.endsWith(".midi") ||
                input.endsWith(".cda")  || input.endsWith(".mpg")  ||
                input.endsWith(".mpeg") || input.endsWith(".m2v")  ||
                input.endsWith(".avi")  || input.endsWith(".m4v")  ||
                input.endsWith(".flv")  || input.endsWith(".mid")  ||
                input.endsWith(".mp2")  || input.endsWith(".mp1")  ||
                input.endsWith(".aac")  || input.endsWith(".asx")  ||
                input.endsWith(".m4a")  || input.endsWith(".mp4")  ||
                input.endsWith(".m4v")  || input.endsWith(".nsv")  ||
                input.endsWith(".aiff") || input.endsWith(".au")   ||
                input.endsWith(".wmv")  || input.endsWith(".asf"));
//                input.endsWith(".vlb")  || input.endsWith(".b4s")  ||
//                input.endsWith(".rmi")  || input.endsWith(".kar")  ||
//                input.endsWith(".miz")  || input.endsWith(".mod")  ||
//                input.endsWith(".mdz")  || input.endsWith(".nst")  ||
//                input.endsWith(".stm")  || input.endsWith(".stz")  ||           
//                input.endsWith(".s3m")  || input.endsWith(".s3z")  ||  
//                input.endsWith(".it")   || input.endsWith(".itz")  ||  
//                input.endsWith(".xm")   || input.endsWith(".xmz")  ||  
//                input.endsWith(".mtm")  || input.endsWith(".ult")  ||  
//                input.endsWith(".669")  || input.endsWith(".far")  ||
//                input.endsWith(".okt")  || input.endsWith(".ptm")  ||
//                input.endsWith(".avr")  || input.endsWith(".caf")  ||
//                input.endsWith(".htk")  || input.endsWith(".iff")  ||
//                input.endsWith(".mat")  || input.endsWith(".paf")  ||
//                input.endsWith(".pvf")  || input.endsWith(".raw")  ||
//                input.endsWith(".sd2")  || input.endsWith(".sds")  ||
//                input.endsWith(".sf")   || input.endsWith(".voc")  ||
//                input.endsWith(".w64")  || input.endsWith(".xi")   ||
//                input.endsWith(".amf"));            
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
                if (!baseDir.endsWith(fs))
                {
                    s.append(fs);
                }
                s.append(entryList[i]);
                File tempFile = new File(s.toString());
                if (tempFile.isDirectory())
                {
                    dirList.add(s.toString());
                }
                else
                {
                    if (endsWithIndexedExtension(s.toString()))
                    {
                        fileList.add(s.toString());
                    }
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
        thisDirList = new Vector<String>();
        thisFileList = new Vector<String>();
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