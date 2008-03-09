package listfix.comparators;

import listfix.model.MP3PositionObject;
import listfix.model.*;

public class MP3PositionObjectPositionComparator implements java.util.Comparator
{
    public int compare(Object a, Object b)
    {
        MP3PositionObject aa = (MP3PositionObject)a;
        MP3PositionObject bb = (MP3PositionObject)b;
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
