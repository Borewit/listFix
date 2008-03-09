package listfix.model;

/*
 * MP3PositionObject.java
 *
 * Created on May 20, 2002, 12:46 PM
 */

/**
 *
 * @author  jcaron
 * @version 
 */
import listfix.model.*;

public class MP3PositionObject {

    private int position = -1;
    private MP3Object mp3 = null;
    
    /** Creates new MP3PositionObject */
    public MP3PositionObject(MP3Object m, int pp) {
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
    
    public MP3Object getMp3()
    {
        return mp3;
    }

}
