package listfix.model;

/*
 * M3UHistory.java
 *
 * Created on December 21, 2002, 5:21 PM
 */

/**
 *
 * @author  Administrator
 */
import java.util.Vector;
import java.io.File;

public class M3UHistory {
    
    private Vector playlists = new Vector();
    private int limit = 0;
    
    /** Creates a new instance of M3UHistory */
    public M3UHistory(int x) 
    {
        limit = x;
    }

    public void setCapacity(int maxPlaylistHistoryEntries)
    {
       limit = maxPlaylistHistoryEntries;
       if (limit < playlists.size())
       {
           playlists.setSize(limit);
       }
    }
    
    protected int getLimit() // added to assist testing
    {
    	return limit;
    }
    
    protected Vector getPlaylists() // added to assist testing
    {
    	return playlists;
    }
    
    public void initHistory(String[] input)
    {
        int i = 0;
        while (i < input.length && i < limit)
        {
            File testFile = new File(input[i]);
            if (testFile.exists())
            {
                playlists.add(input[i]);
            }
            i++;
        }
    }
    
    public void add(String filename)
    {
        File testFile = new File(filename);
        if (testFile.exists())
        {
            int index = playlists.indexOf(filename);
            if (index > -1)
            {
                Object temp = playlists.remove(index);
                playlists.insertElementAt(temp, 0);
            }
            else
            {
                if (playlists.size() < limit)
                {
                    playlists.insertElementAt(filename, 0);
                }
                else
                {
                    playlists.removeElementAt(limit - 1);
                    playlists.insertElementAt(filename, 0);
                }
            }
        }
    }
    
    public String[] getM3UFilenames()
    {
        String[] result = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++)
        {
            result[i] = (String)playlists.elementAt(i);
        }
        return result;
    }
    
    public void clearHistory()
    {
        playlists.clear();
    }   
}