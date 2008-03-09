package listfix.comparators;

import listfix.model.*;

public class MP3PositionObjectFileNameComparator implements java.util.Comparator
{
    public int compare(Object a, Object b)
    {
        MP3PositionObject aa = (MP3PositionObject)a;
        MP3PositionObject bb = (MP3PositionObject)b;
        return aa.getMp3().getFileName().compareTo(bb.getMp3().getFileName());
    }    
}
