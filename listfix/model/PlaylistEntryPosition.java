package listfix.model;

/*
 * PlaylistEntryPosition.java
 *
 * Created on May 20, 2002, 12:46 PM
 */

/**
 *
 * @author  jcaron
 * @version 
 */
import listfix.model.*;

public class PlaylistEntryPosition {

    private int position = -1;
    private PlaylistEntry mp3 = null;
    
    /** Creates new PlaylistEntryPosition */
    public PlaylistEntryPosition(PlaylistEntry m, int pp) {
        mp3 = m;
        position = pp;
    }
    
    public int getPosition()
    {
        return position;
    }
    
    public void setPosition(int input)
    {
        position = input;
    }   
    
    public PlaylistEntry getMp3()
    {
        return mp3;
    }

}
