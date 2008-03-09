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

public class UpdateMediaLibraryTask extends listfix.view.support.Task 
{
    private GUIDriver guiDriver;
    private String[] mediaDir;
    private String[] mediaLibraryDirectoryList;
    private String[] mediaLibraryFileList;  
    
    /** Creates new LocateMP3sTask */
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
            mediaLibraryDirectoryList = DirectoryScanner.createMediaLibraryDirectoryList(guiDriver.getMediaDirs());
            this.notifyObservers((int)(1.0/6.0 * 100.0));
            mediaLibraryFileList = DirectoryScanner.createMediaLibraryFileList(mediaLibraryDirectoryList);
            this.notifyObservers((int)(2.0/6.0 * 100.0));
            java.util.Arrays.sort(mediaDir);
            guiDriver.setMediaDirs(mediaDir);
            this.notifyObservers((int)(3.0/6.0 * 100.0));
            java.util.Arrays.sort(mediaLibraryDirectoryList);
            guiDriver.setMediaLibraryDirectoryList(mediaLibraryDirectoryList);
            this.notifyObservers((int)(4.0/6.0 * 100.0));
            java.util.Arrays.sort(mediaLibraryFileList);
            guiDriver.setMediaLibraryFileList(mediaLibraryFileList);
            this.notifyObservers((int)(5.0/6.0 * 100.0));
            FileWriter.writeIni(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList); 
            this.notifyObservers(100);
        }
    }   
}