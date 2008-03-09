package listfix.comparators;

import listfix.model.*;

public class AscendingPathComparator implements java.util.Comparator
{
    public int compare(Object a, Object b)
    {
        MP3Object aa = (MP3Object)a;
        MP3Object bb = (MP3Object)b;
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
