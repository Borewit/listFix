package listfix.comparators;

import listfix.model.*;

public class DescendingStatusComparator implements java.util.Comparator
{
    public int compare(Object a, Object b)
    {
        PlaylistEntry aa = (PlaylistEntry)a;
        PlaylistEntry bb = (PlaylistEntry)b;
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

