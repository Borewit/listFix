package listfix.comparators;

import listfix.model.MP3Object;

public class AscendingFilenameComparator implements java.util.Comparator
{
    public int compare(Object a, Object b)
    {
        MP3Object aa = (MP3Object)a;
        MP3Object bb = (MP3Object)b;
        if (aa.getFileName().compareToIgnoreCase(bb.getFileName()) < 0)
        {
            return -1;
        }
        else if (aa.getFileName().equals(bb.getFileName()))
        {
            return 0;
        }
        return 1;
    }    
}
