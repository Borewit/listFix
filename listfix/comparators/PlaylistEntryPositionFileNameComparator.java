package listfix.comparators;

import listfix.model.*;

public class PlaylistEntryPositionFileNameComparator implements java.util.Comparator
{
    public int compare(Object a, Object b)
    {
        PlaylistEntryPosition aa = (PlaylistEntryPosition)a;
        PlaylistEntryPosition bb = (PlaylistEntryPosition)b;
        return aa.getPlaylistEntry().getFileName().compareTo(bb.getPlaylistEntry().getFileName());
    }    
}
