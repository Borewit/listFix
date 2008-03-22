package listfix.comparators;

import listfix.model.*;

public class MatchedPlaylistEntryComparator implements java.util.Comparator
{
    public int compare(Object a, Object b)
    {
        MatchedPlaylistEntry aa = (MatchedPlaylistEntry)a;
        MatchedPlaylistEntry bb = (MatchedPlaylistEntry)b;
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