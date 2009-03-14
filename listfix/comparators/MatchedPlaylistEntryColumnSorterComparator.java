package listfix.comparators;

import listfix.model.MatchedPlaylistEntry;

public class MatchedPlaylistEntryColumnSorterComparator implements java.util.Comparator
{
    protected boolean isSortAsc;
    protected int sortCol;

    public MatchedPlaylistEntryColumnSorterComparator(boolean sortAsc, int sortByColumn)
    {
        sortCol = sortByColumn;
        isSortAsc = sortAsc;
    }

    public int compare(Object o1, Object o2)
    {
        if (!(o1 instanceof MatchedPlaylistEntry) || !(o2 instanceof MatchedPlaylistEntry))
        {
            return 0;
        }

        if (sortCol == 0)
        {
            if (isSortAsc)
            {
                return ((MatchedPlaylistEntry)o1).getPlaylistFile().getFileName().compareTo(((MatchedPlaylistEntry)o2).getPlaylistFile().getFileName());
            }
            else
            {
                return ((MatchedPlaylistEntry)o2).getPlaylistFile().getFileName().compareTo(((MatchedPlaylistEntry)o1).getPlaylistFile().getFileName());
            }
        }
        else
        {
            int o1Count = ((MatchedPlaylistEntry)o1).getCount();
            int o2Count = ((MatchedPlaylistEntry)o2).getCount();
            if (o1Count == o2Count)
                return 0;
            else if (o1Count > o2Count)
                return (isSortAsc ? 1 : -1);
            else return (isSortAsc ? -1 : 1);
        }
    }
}