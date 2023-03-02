

package listfix.model.playlists.itunes;

import java.util.List;

/**
 * A thin wrapper meant to represent an internal iTunes "playlist".
 * iTunes XML files can contain one or more playlists.  As such, the name of the playlist
 * is not the name of the file it's found in, so the name must be tracked here.
 * Also maintains a list of the ITunesTracks that make up this list.
 *
 * In the iTunes model, the tracks are stored in a separate section of the XML file, each
 * with an ID, and the playlist is then just a list of the track ids making up the list.
 * Our internal representation denormalizes this so to speak, as ITunesTrack actually has
 * information about the track from the tracks section of the XML.
 * @author jcaron
 * @see ITunesTrack
 */
public class ITunesTrackList
{
  private String _name;
  private List<ITunesTrack> _tracks;

  /**
   * Constructs a new ITunesTrackList object.
   * 
   * 
   */
  public ITunesTrackList(String name, List<ITunesTrack> tracks)
  {
    _name = name;
    _tracks = tracks;
  }

  /**
   *Returns the _name.
 
   */
  public String getName()
  {
    return _name;
  }

  /**
   * @param name the _name to set
   */
  public void setName(String name)
  {
    this._name = name;
  }

  /**
   *Returns the _tracks.
 
   */
  public List<ITunesTrack> getTracks()
  {
    return _tracks;
  }

  /**
   * @param tracks the _tracks to set
   */
  public void setTracks(List<ITunesTrack> tracks)
  {
    this._tracks = tracks;
  }

}
