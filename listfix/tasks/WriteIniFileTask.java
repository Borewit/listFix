package listfix.tasks;

import java.io.*;
import listfix.model.*;

public class WriteIniFileTask extends listfix.controller.Task 
{
    private final static String fs = System.getProperty("file.separator");
    private final static String br = System.getProperty("line.separator");
    private final static String homeDir = System.getProperty("user.home");
    
    private FileOutputStream outputStream;
    private DataOutputStream output;
    private String[] mediaDir;
    private String[] mediaLibraryDirList;
    private String[] mediaLibraryFileList;
    private AppOptions options;

    public WriteIniFileTask(String[] m, String[] mldl, String[] mlfl, AppOptions opts)
    {
        mediaDir = m;
        mediaLibraryDirList = mldl;
        mediaLibraryFileList = mlfl;
        options = opts;
    }

    /** Run the task. This method is the body of the thread for this task.  */
    public void run() 
    {
        try
        {
            outputStream = new FileOutputStream(homeDir + fs + "dirLists.ini");
            output = new DataOutputStream(outputStream);
            if (mediaDir != null)
            {
                output.writeBytes("[Media Directories]" + br);
                for(int i = 0; i < mediaDir.length; i++)
                {
                    output.writeBytes(mediaDir[i] + br);
                }
            }
            if (options != null)
            {
                output.writeBytes("[Options]" + br);
                output.writeBytes("AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD=" 
                        + Boolean.toString(options.getAutoLocateEntriesOnPlaylistLoad()) + br);
                output.writeBytes("MAX_PLAYLIST_HISTORY_SIZE=" 
                        + options.getMaxPlaylistHistoryEntries() + br);
                output.writeBytes("SAVE_RELATIVE_REFERENCES=" 
                        + Boolean.toString(options.getSavePlaylistsWithRelativePaths()) + br);
            }
            if (mediaLibraryDirList != null)
            {
                output.writeBytes("[Media Library Directories]" + br);
                for(int i = 0; i < mediaLibraryDirList.length; i++)
                {
                    output.writeBytes(mediaLibraryDirList[i] + br);
                }
            }
            if (mediaLibraryDirList != null)
            {
                output.writeBytes("[Media Library Files]" + br);
                for(int i = 0; i < mediaLibraryFileList.length; i++)
                {
                    output.writeBytes(mediaLibraryFileList[i] + br);
                }
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
}
