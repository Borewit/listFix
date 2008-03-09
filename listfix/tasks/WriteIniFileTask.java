package listfix.tasks;

import java.io.*;
import listfix.model.*;

public class WriteIniFileTask extends listfix.view.support.Task {

    private final static String fs = System.getProperty("file.separator");
    private final static String br = System.getProperty("line.separator");
    private final static String homeDir = System.getProperty("user.home");
    
    private FileOutputStream outputStream;
    private DataOutputStream output;
    private String[] mediaDir;
    private String[] mediaLibraryDirList;
    private String[] mediaLibraryFileList;
    
    /** Creates new CopyMP3sTask */
    public WriteIniFileTask(String[] m, String[] mldl, String[] mlfl) 
    {
        mediaDir = m;
        mediaLibraryDirList = mldl;
        mediaLibraryFileList = mlfl;
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
        }
    }
}
