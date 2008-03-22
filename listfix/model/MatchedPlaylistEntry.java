package listfix.model;

import java.io.*;

public class MatchedPlaylistEntry
{
    private PlaylistEntry thisFile = null;
    private int count = 0;
    
    public MatchedPlaylistEntry(String path, String fileName, int c)
    {
        thisFile = new PlaylistEntry(path, fileName, "");
        count = c;
    }
    
    public MatchedPlaylistEntry(File f, int c)
    {
        thisFile = new PlaylistEntry(f);
        count = c;
    }
    
    public int getCount()
    {
        return count;
    }
    
    public PlaylistEntry getPlaylistFile()
    {
        return thisFile;
    }    
}
