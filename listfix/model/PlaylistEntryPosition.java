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

public class PlaylistEntryPosition {

    private int position = -1;
    private PlaylistEntry entry = null;
    
    /** Creates new PlaylistEntryPosition */
    public PlaylistEntryPosition(PlaylistEntry e, int pp) {
        entry = e;
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
    
    public PlaylistEntry getPlaylistEntry()
    {
        return entry;
    }

}
