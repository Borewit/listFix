package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     FileWriter.java
= Version:  1.0
= Purpose:  Provides 2 static methods, one for writing out a playlist
=           to a file and one for writing out the ini file for this
=           program.
============================================================================
*/

import listfix.model.M3UHistory;
import listfix.tasks.WriteIniFileTask;
import listfix.model.PlaylistEntry;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public class FileWriter 
{    
    private final static String br = System.getProperty("line.separator");
    private final static String fs = System.getProperty("file.separator");
    private final static String homeDir = System.getProperty("user.home");
    private static FileOutputStream outputStream;
    private static DataOutputStream output;
    
    public static void writeM3U(Vector entries, File fileName)
    {
        PlaylistEntry tempEntry = null;
        try
        {
            outputStream = new FileOutputStream(fileName);
            output = new DataOutputStream(outputStream);
            output.writeBytes("#EXTM3U" + br);
            for(int i = 0; i < entries.size(); i++)
            {
                tempEntry = (PlaylistEntry)entries.elementAt(i);
                output.writeBytes(tempEntry.toString() + br);
            }        
            output.close();
            outputStream.close();
        }
        catch(IOException e)
        {
        }
    }
    
    public static void writeMruM3Us(M3UHistory history)
    {        
        try
        {
            outputStream = new FileOutputStream(homeDir + fs + "listFixHistory.ini");
            output = new DataOutputStream(outputStream);
            output.writeBytes("[Recent M3Us]" + br);
            String[] filenames = history.getM3UFilenames();
            for(int i = 0; i < filenames.length; i++)
            {
                output.writeBytes(filenames[i] + br);
            }
            output.close();
            outputStream.close();
        }
        catch (IOException e)
        {            
        }
    }
    
    public static void writeIni(String[] mediaDir, String[] mediaLibraryDirList, String[] mediaLibraryFileList)
    {
        try
        {
            WriteIniFileTask thisTask = new WriteIniFileTask(mediaDir, mediaLibraryDirList, mediaLibraryFileList);
            thisTask.start();
        }
        catch(Exception e)
        {
        }
    }
}
        