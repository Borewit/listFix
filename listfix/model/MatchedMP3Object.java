package listfix.model;

import listfix.model.MP3Object;
import java.io.*;

public class MatchedMP3Object
{
    private MP3Object thisMP3 = null;
    private int count = 0;
    
    public MatchedMP3Object(String path, String fileName, int c)
    {
        thisMP3 = new MP3Object(path, fileName, "");
        count = c;
    }
    
    public MatchedMP3Object(File f, int c)
    {
        thisMP3 = new MP3Object(f);
        count = c;
    }
    
    public int getCount()
    {
        return count;
    }
    
    public MP3Object getMP3()
    {
        return thisMP3;
    }    
}
