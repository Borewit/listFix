package listfix.comparators;

import listfix.model.playlists.PotentialPlaylistEntryMatch;


public class MatchedPlaylistEntryComparator implements java.util.Comparator<PotentialPlaylistEntryMatch>
{
  /**
   *
   * @param aa
   * @param bb
   * @return
   */
  @Override
  public int compare(PotentialPlaylistEntryMatch aa, PotentialPlaylistEntryMatch bb)
  {
    if (aa.getScore() < bb.getScore())
    {
      return 1;
    }
    else if (aa.getScore() == bb.getScore())
    {
      return 0;
    }
    return -1;
  }
}
