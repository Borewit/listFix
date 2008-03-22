package listfix.comparators;

import listfix.model.PlaylistEntryPosition;
import listfix.model.*;

public class PlaylistEntryPositionIndexComparator implements java.util.Comparator
{
    public int compare(Object a, Object b)
    {
        PlaylistEntryPosition aa = (PlaylistEntryPosition)a;
        PlaylistEntryPosition bb = (PlaylistEntryPosition)b;
        if (aa.getPosition() < bb.getPosition())
        {
            return -1;
        }
        else if (aa.getPosition() == bb.getPosition())
        {
            return 0;
        }
        return 1;
    }    
}
