package listfix.comparators;

import listfix.model.*;

public class DescendingFilenameComparator implements java.util.Comparator
{
    public int compare(Object a, Object b)
    {
        PlaylistEntry aa = (PlaylistEntry)a;
        PlaylistEntry bb = (PlaylistEntry)b;
        if (aa.getFileName().compareToIgnoreCase(bb.getFileName()) < 0)
        {
            return 1;
        }
        else if (aa.getFileName().equals(bb.getFileName()))
        {
            return 0;
        }
        return -1;
    }    
}
