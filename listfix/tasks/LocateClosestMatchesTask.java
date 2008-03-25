package listfix.tasks;

/**
 *
 * @author  jcaron
 * @version 
 */
import listfix.model.*;
import listfix.util.*;
import java.util.Vector;
import java.io.*;

public class LocateClosestMatchesTask extends listfix.view.support.Task 
{
    private PlaylistEntry entry;
    private String[] mediaLibraryFileList;
    private Vector results = new Vector();
    
    public LocateClosestMatchesTask(PlaylistEntry x, String[] y) 
    {
        entry = x;
        mediaLibraryFileList = y;
    }

    /** Run the task. This method is the body of the thread for this task.  */
    public void run() 
    {
        String[] fileToFindTokens = FileNameTokenizer.splitFileName(entry.getFileName().replaceAll("\'", ""));
        // implement tokenized file name matching procedure here...
        for (int i = 0; i < mediaLibraryFileList.length; i++)
        {
            File mediaFile = new File(mediaLibraryFileList[i]);
            String[] currentFileTokens = FileNameTokenizer.splitFileName(mediaFile.getName().replaceAll("\'", ""));             
            int matchedTokens = FileNameTokenizer.countMatchingTokens(fileToFindTokens, currentFileTokens);
            if (matchedTokens > 0)
            {
                results.add(new MatchedPlaylistEntry(mediaFile, matchedTokens));
            }
            this.notifyObservers((int)((double)i/(double)(mediaLibraryFileList.length-1) * 100.0));
        }
    }
    
    public Vector locateClosestMatches()
    {
        return results;
    }
}
