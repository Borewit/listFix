package listfix.tasks;

import java.io.*;
import listfix.model.*;

public class WriteIniFileTask extends listfix.controller.Task 
{
    private final static String fs = System.getProperty("file.separator");
    private final static String br = System.getProperty("line.separator");
    private final static String homeDir = System.getProperty("user.home");
    
    private FileOutputStream outputStream;
    private BufferedOutputStream output;
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
            StringBuffer buffer = new StringBuffer();
            if (mediaDir != null)
            {
                buffer.append("[Media Directories]").append(br);
                for(int i = 0; i < mediaDir.length; i++)
                {
                    buffer.append(mediaDir[i]).append(br);
                }
            }
            if (options != null)
            {
                buffer.append("[Options]").append(br);
                buffer.append("AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD=" 
                    + Boolean.toString(options.getAutoLocateEntriesOnPlaylistLoad())).append(br);
                buffer.append("MAX_PLAYLIST_HISTORY_SIZE=" 
                    + options.getMaxPlaylistHistoryEntries() + br);
                buffer.append("SAVE_RELATIVE_REFERENCES=" 
					+ Boolean.toString(options.getSavePlaylistsWithRelativePaths())).append(br);
				buffer.append("AUTO_REFRESH_MEDIA_LIBRARY_ON_LOAD=" 
					+ Boolean.toString(options.getAutoRefreshMediaLibraryOnStartup())).append(br);
				buffer.append("LOOK_AND_FEEL=" + options.getLookAndFeel()).append(br);
            }
            if (mediaLibraryDirList != null)
            {
                buffer.append("[Media Library Directories]").append(br);
                for(int i = 0; i < mediaLibraryDirList.length; i++)
                {
                    buffer.append(mediaLibraryDirList[i]).append(br);
                }
            }
            if (mediaLibraryDirList != null)
            {
                buffer.append("[Media Library Files]").append(br);
                for(int i = 0; i < mediaLibraryFileList.length; i++)
                {
                    buffer.append(mediaLibraryFileList[i]).append(br);
                }
            }
            outputStream = new FileOutputStream(homeDir + fs + "dirLists.ini");
            output = new BufferedOutputStream(outputStream);
            output.write(buffer.toString().getBytes());
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
