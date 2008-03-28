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
import java.io.File;
import java.util.*;

public class OpenM3UTask extends listfix.controller.Task 
{
    private GUIDriver guiDriver;
    private File input; 
    private Vector entries = new Vector();
    
    public OpenM3UTask(GUIDriver gd, File f) 
    {
        guiDriver = gd;
        input = f;
    }
    
    /** Run the task. This method is the body of the thread for this task.  */
    public void run() 
    {
        try
        {
            guiDriver.setCurrentPlaylist(input);
            guiDriver.getHistory().add(input.getCanonicalPath());
            M3UFileReader playlistProcessor = new M3UFileReader(input);
            entries = playlistProcessor.readM3U(this);
            guiDriver.setEntries(entries);
            playlistProcessor.closeFile();
            FileWriter.writeMruM3Us(guiDriver.getHistory());
            this.notifyObservers(100);    
        }
        catch(Exception e)
        {
            this.notifyObservers(100);
        }
    }   
}