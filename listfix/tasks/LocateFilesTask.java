package listfix.tasks;

/*
 * LocateFilesTask.java
 *
 * Created on April 30, 2002, 2:07 PM
 */

/**
 *
 * @author  jcaron
 * @version 
 */
import listfix.model.PlaylistEntry;
import java.util.Vector;

public class LocateFilesTask extends listfix.view.support.Task {

    private Vector mp3s;
    private String[] mediaLibraryFileList;
    
    /** Creates new LocateFilesTask */
    public LocateFilesTask(Vector x, String[] y) {
        mp3s = x;
        mediaLibraryFileList = y;
    }

    /** Run the task. This method is the body of the thread for this task.  */
    public void run() 
    {
        PlaylistEntry tempMP3 = null;
        for (int i = 0; i < mp3s.size(); i++)
        {
            tempMP3 = (PlaylistEntry) mp3s.elementAt(i);
            if (tempMP3.exists())
            {
                tempMP3.setMessage("Found!");
            }
            else
            {
                tempMP3.findNewLocationFromFileList(mediaLibraryFileList);
            }            
            this.notifyObservers((int)((double)i/(double)(mp3s.size()-1) * 100.0));
        }
        this.notifyObservers(100);
    }
    
    public Vector locateMP3s()
    {
        // this.run();
        return mp3s;
    }
}
