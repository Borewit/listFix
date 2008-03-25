package listfix.model;

import java.io.*;

public class MatchedPlaylistEntry
{
    private PlaylistEntry thisEntry = null;
    private int count = 0;
    
    public MatchedPlaylistEntry(String path, String fileName, int c)
    {
        thisEntry = new PlaylistEntry(path, fileName, "");
        count = c;
    }
    
    public MatchedPlaylistEntry(File f, int c)
    {
        thisEntry = new PlaylistEntry(f, "");
        count = c;
    }
    
    public int getCount()
    {
        return count;
    }
    
    public PlaylistEntry getPlaylistFile()
    {
        return thisEntry;
    }    
}
