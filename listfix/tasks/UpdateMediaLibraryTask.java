package listfix.tasks;

/**
 *
 * @author  jcaron
 * @version 
 */
import listfix.io.*;
import listfix.model.*;
import listfix.util.*;
import listfix.controller.*;

public class UpdateMediaLibraryTask extends listfix.controller.Task 
{
    private GUIDriver guiDriver;
    private String[] mediaDir;
    private String[] mediaLibraryDirectoryList;
    private String[] mediaLibraryFileList;  
    
    public UpdateMediaLibraryTask(GUIDriver gd) 
    {
        guiDriver = gd;
        mediaDir = gd.getMediaDirs();
    }

    /** Run the task. This method is the body of the thread for this task.  */
    public void run() 
    {
        if(mediaDir != null)
        {
            DirectoryScanner.createMediaLibraryDirectoryAndFileList(guiDriver.getMediaDirs(), this);
            this.setMessage("Finishing...");
            mediaLibraryDirectoryList = DirectoryScanner.getDirectoryList();
            mediaLibraryFileList = DirectoryScanner.getFileList();
            java.util.Arrays.sort(mediaDir);
            guiDriver.setMediaDirs(mediaDir);
            java.util.Arrays.sort(mediaLibraryDirectoryList);
            guiDriver.setMediaLibraryDirectoryList(mediaLibraryDirectoryList);
            java.util.Arrays.sort(mediaLibraryFileList);
            guiDriver.setMediaLibraryFileList(mediaLibraryFileList);
            this.notifyObservers(100);
            FileWriter.writeIni(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList);
        }
    }   
}