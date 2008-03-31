package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     FileWriter.java
= Purpose:  Provides static methods for writing out a playlist
=           to a file and writing out the ini files for this
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
import listfix.model.AppOptions;

public class FileWriter 
{    
    private final static String br = System.getProperty("line.separator");
    private final static String fs = System.getProperty("file.separator");
    private final static String homeDir = System.getProperty("user.home");
    private static FileOutputStream outputStream;
    private static DataOutputStream output;

    public static void writeDefaultIniFilesIfNeeded()
    {
        File testFile = new File(homeDir + fs + "dirLists.ini");
        if (!testFile.exists() || (testFile.exists() && testFile.length() == 0))
        {
            try
            {
                AppOptions options = new AppOptions();
                outputStream = new FileOutputStream(homeDir + fs + "dirLists.ini");
                output = new DataOutputStream(outputStream);
                output.writeBytes("[Media Directories]" + br);
                output.writeBytes("[Options]" + br);
                output.writeBytes("AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD=" + Boolean.toString(options.getAutoLocateEntriesOnPlaylistLoad()) + br);
                output.writeBytes("MAX_PLAYLIST_HISTORY_SIZE=" + options.getMaxPlaylistHistoryEntries() + br);
                output.writeBytes("SAVE_RELATIVE_REFERENCES=" + Boolean.toString(options.getSavePlaylistsWithRelativePaths()) + br);
                output.writeBytes("[Media Library Directories]" + br);
                output.writeBytes("[Media Library Files]" + br);
                output.close();
                outputStream.close();
            }
            catch (IOException e)
            {
                // eat the error and continue
                e.printStackTrace();
            }
        }
        
        testFile = new File(homeDir + fs + "listFixHistory.ini");
        if (!testFile.exists() || (testFile.exists() && testFile.length() == 0))
        {
            try
            {
                outputStream = new FileOutputStream(homeDir + fs + "listFixHistory.ini");
                output = new DataOutputStream(outputStream);
                output.writeBytes("[Recent M3Us]" + br);
                output.close();
                outputStream.close();
            }
            catch (IOException e)
            {
                // eat the error and continue
                e.printStackTrace();
            }
        }
    }
    
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
                output.writeBytes(tempEntry.toM3UString() + br);
            }        
            output.close();
            outputStream.close();
        }
        catch(IOException e)
        {
            // eat the error and continue
            e.printStackTrace();
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
            // eat the error and continue
            e.printStackTrace();
        }
    }
    
    public static void writeIni(String[] mediaDir, String[] mediaLibraryDirList, String[] mediaLibraryFileList, AppOptions options)
    {
        try
        {
            WriteIniFileTask thisTask = new WriteIniFileTask(mediaDir, mediaLibraryDirList, mediaLibraryFileList, options);
            thisTask.start();
        }
        catch(Exception e)
        {
            // eat the error and continue
            e.printStackTrace();
        }
    }
}
        