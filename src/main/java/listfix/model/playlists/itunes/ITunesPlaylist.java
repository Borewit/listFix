

package listfix.model.playlists.itunes;

import listfix.io.IPlaylistOptions;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;

import java.io.File;
import java.util.List;


public class ITunesPlaylist extends Playlist
{
  private final ITunesMediaLibrary _library;

  public ITunesPlaylist(File listFile, List<PlaylistEntry> entries, ITunesMediaLibrary library, IPlaylistOptions filePathOptions)
  {
    super(filePathOptions, listFile, PlaylistType.ITUNES, entries);
    _library = library;
  }

  /**
   * @return the _library
   */
  public ITunesMediaLibrary getLibrary()
  {
    return _library;
  }
}
