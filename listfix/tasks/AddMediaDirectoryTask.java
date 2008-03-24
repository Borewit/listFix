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

public class AddMediaDirectoryTask extends listfix.view.support.Task 
{
    private GUIDriver guiDriver;
    private String dir;
    private String[] mediaDir;
    private String[] mediaLibraryDirectoryList;
    private String[] mediaLibraryFileList;  
    
    public AddMediaDirectoryTask(String d, GUIDriver gd) 
    {
        dir = d;
        guiDriver = gd;
    }
    
    public String getAddedDirectory()
    {
        return dir;
    }
    
    public String[] getMediaDirectories()
    {
        return guiDriver.getMediaDirs();
    }

    /** Run the task. This method is the body of the thread for this task.  */
    public void run() 
    {
        if(guiDriver.getMediaDirs() == null)
        {
            mediaDir = new String[1];
            mediaDir[0] = dir;
            mediaLibraryDirectoryList = DirectoryScanner.createMediaLibraryDirectoryList(mediaDir);                    
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
        else
        {
            String[] tempMediaDir = new String[1];
            tempMediaDir[0] = dir;
            mediaDir = ArrayFunctions.copyArrayAddOneValue(guiDriver.getMediaDirs(), dir);
            String[] tempMediaLibraryDirs = DirectoryScanner.createMediaLibraryDirectoryList(tempMediaDir);
            this.notifyObservers((int)(1.0/7.0 * 100.0));
            mediaLibraryDirectoryList = ArrayFunctions.mergeArray(guiDriver.getMediaLibraryDirectoryList(), tempMediaLibraryDirs);     
            this.notifyObservers((int)(2.0/7.0 * 100.0));
            mediaLibraryFileList = ArrayFunctions.mergeArray(guiDriver.getMediaLibraryFileList(), DirectoryScanner.createMediaLibraryFileList(tempMediaLibraryDirs));
            this.notifyObservers((int)(3.0/7.0 * 100.0));
            java.util.Arrays.sort(mediaDir);
            guiDriver.setMediaDirs(mediaDir);
            this.notifyObservers((int)(4.0/7.0 * 100.0));
            java.util.Arrays.sort(mediaLibraryDirectoryList);
            guiDriver.setMediaLibraryDirectoryList(mediaLibraryDirectoryList);
            this.notifyObservers((int)(5.0/7.0 * 100.0));
            java.util.Arrays.sort(mediaLibraryFileList);
            guiDriver.setMediaLibraryFileList(mediaLibraryFileList);
            this.notifyObservers((int)(6.0/7.0 * 100.0));
            FileWriter.writeIni(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList);
            this.notifyObservers(100);
        }
    }   
}