package listfix.tasks;

import listfix.io.*;
import listfix.model.*;
import listfix.util.*;
import listfix.controller.*;

public class AddMediaDirectoryTask extends listfix.controller.Task 
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
            DirectoryScanner.createMediaLibraryDirectoryAndFileList(mediaDir, this); 
            this.setMessage("Finishing...");
            mediaLibraryDirectoryList = DirectoryScanner.getDirectoryList();
            mediaLibraryFileList = DirectoryScanner.getFileList();
            java.util.Arrays.sort(mediaDir);
            guiDriver.setMediaDirs(mediaDir);
            java.util.Arrays.sort(mediaLibraryDirectoryList);
            guiDriver.setMediaLibraryDirectoryList(mediaLibraryDirectoryList);
            java.util.Arrays.sort(mediaLibraryFileList);
            guiDriver.setMediaLibraryFileList(mediaLibraryFileList);
            FileWriter.writeIni(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList);
            this.notifyObservers(100);
        }
        else
        {
            String[] tempMediaDir = new String[1];
            tempMediaDir[0] = dir;
            mediaDir = ArrayFunctions.copyArrayAddOneValue(guiDriver.getMediaDirs(), dir);
            DirectoryScanner.createMediaLibraryDirectoryAndFileList(tempMediaDir, this);
            this.setMessage("Finishing...");
            mediaLibraryDirectoryList = ArrayFunctions.mergeArray(guiDriver.getMediaLibraryDirectoryList(), DirectoryScanner.getDirectoryList());  
            mediaLibraryFileList = ArrayFunctions.mergeArray(guiDriver.getMediaLibraryFileList(), DirectoryScanner.getFileList());
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