

package listfix.model.playlists.itunes;

import christophedelory.plist.Dict;

import java.util.Hashtable;

/**
 * Wrapper around a christophedelory.plist.Dict to provide easy access to the information about a track
 * in an iTunes library/playlist file for the purposes of conversion to a listFix() PlaylistEntry object.
 * @author jcaron
 */
public class ITunesTrack
{
  private final Dict _trackDict;

  public static final String FILE = "File";
  public static final String URL = "URL";

  /**
   * Constructor that takes a christophedelory.plist.Dict object.
   * @param trackDict The christophedelory.plist.Dict object containing information about the track.
   */
  public ITunesTrack(Dict trackDict)
  {
    _trackDict = trackDict;
  }

  /**
   *Returns the _location.
 
   */
  public String getLocation()
  {
    return DictionaryParser.getKeyValueAsString(_trackDict, "Location");
  }

  public void setLocation(String location)
  {
    DictionaryParser.setKeyValue(_trackDict, "Location", new christophedelory.plist.String(location));
  }

  /**
   *Returns the _artist.
 
   */
  public String getArtist()
  {
    return DictionaryParser.getKeyValueAsString(_trackDict, "Artist");
  }

  /**
   *Returns the _name.
 
   */
  public String getName()
  {
    return DictionaryParser.getKeyValueAsString(_trackDict, "Name");
  }

  /**
   *Returns the _album.
 
   */
  public String getAlbum()
  {
    return DictionaryParser.getKeyValueAsString(_trackDict, "Album");
  }

  /**
   *Returns the _albumArtist.
 
   */
  public String getAlbumArtist()
  {
    return DictionaryParser.getKeyValueAsString(_trackDict, "Album Artist");
  }

  public String getTrackType()
  {
    return DictionaryParser.getKeyValueAsString(_trackDict, "Track Type");
  }

  /**
   *Returns the _duration.
 
   */
  public long getDuration()
  {
    long result = -1;
    christophedelory.plist.Integer timeInt = ((christophedelory.plist.Integer)((Hashtable)getTrackDict().getDictionary()).get(new christophedelory.plist.Key("Total Time")));
    if (timeInt != null)
    {
      String timeText = timeInt.getValue();
      result = Long.parseLong(timeText);
    }
    return result;
  }

  /**
   *Returns the _trackId.
 
   */
  public String getTrackId()
  {
    return DictionaryParser.getKeyValueAsInteger(_trackDict, "Track ID").getValue();
  }

  /**
   *Returns the _trackDict.
 
   */
  public Dict getTrackDict()
  {
    return _trackDict;
  }
}
