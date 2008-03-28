package listfix.tasks;

/**
 *
 * @author  jcaron
 * @version 
 */
import listfix.model.PlaylistEntry;
import java.util.Vector;

public class LocateFilesTask extends listfix.controller.Task 
{
    private Vector entries;
    private String[] mediaLibraryFileList;
    
    /** Creates new LocateFilesTask */
    public LocateFilesTask(Vector x, String[] y) 
    {
        entries = x;
        mediaLibraryFileList = y;
    }

    /** Run the task. This method is the body of the thread for this task.  */
    public void run() 
    {
        PlaylistEntry tempEntry = null;
        for (int i = 0; i < entries.size(); i++)
        {
            tempEntry = (PlaylistEntry) entries.elementAt(i);
            if (!tempEntry.isURL())
            {
                if (tempEntry.exists())
                {
                    tempEntry.setMessage("Found!");
                }
                else
                {
                    tempEntry.findNewLocationFromFileList(mediaLibraryFileList);
                }            
            }
            this.notifyObservers((int)((double)i/(double)(entries.size()-1) * 100.0));
        }
        this.notifyObservers(100);
    }
    
    public Vector locateFiles()
    {
        return entries;
    }
}
