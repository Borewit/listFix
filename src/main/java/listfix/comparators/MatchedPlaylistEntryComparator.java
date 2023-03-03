package listfix.comparators;

import listfix.model.playlists.PotentialPlaylistEntryMatch;

import java.util.Comparator;

public class MatchedPlaylistEntryComparator implements Comparator<PotentialPlaylistEntryMatch>
{
  @Override
  public int compare(PotentialPlaylistEntryMatch aa, PotentialPlaylistEntryMatch bb)
  {
    if (aa.getScore() < bb.getScore())
    {
      return 1;
    }
    return aa.getScore() == bb.getScore() ? 0 : -1;
  }
}
