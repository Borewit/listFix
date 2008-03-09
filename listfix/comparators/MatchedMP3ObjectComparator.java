package listfix.comparators;

import listfix.model.*;

public class MatchedMP3ObjectComparator implements java.util.Comparator
{
    public int compare(Object a, Object b)
    {
        MatchedMP3Object aa = (MatchedMP3Object)a;
        MatchedMP3Object bb = (MatchedMP3Object)b;
        if (aa.getCount() < bb.getCount())
        {
            return 1;
        }
        else if (aa.getCount() == bb.getCount())
        {
            return 0;
        }
        return -1;
    }    
}