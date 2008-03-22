package listfix.comparators;

import listfix.model.*;

public class AscendingPathComparator implements java.util.Comparator
{
    public int compare(Object a, Object b)
    {
        PlaylistEntry aa = (PlaylistEntry)a;
        PlaylistEntry bb = (PlaylistEntry)b;
        if (aa.getPath().compareToIgnoreCase(bb.getPath()) < 0)
        {
            return -1;
        }
        else if (aa.getPath().equals(bb.getPath()))
        {
            return 0;
        }
        return 1;
    }    
}
