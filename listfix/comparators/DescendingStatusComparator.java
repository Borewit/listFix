package listfix.comparators;

import listfix.model.*;

public class DescendingStatusComparator implements java.util.Comparator
{
    public int compare(Object a, Object b)
    {
        MP3Object aa = (MP3Object)a;
        MP3Object bb = (MP3Object)b;
        if (aa.getMessage().compareToIgnoreCase(bb.getMessage()) < 0)
        {
            return 1;
        }
        else if (aa.getMessage().equals(bb.getMessage()))
        {
            return 0;
        }
        return -1;
    }    
}

