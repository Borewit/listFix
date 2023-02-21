

package listfix.io.playlists;

import listfix.model.enums.PlaylistType;
import listfix.model.playlists.PlaylistEntry;
import listfix.view.support.IProgressObserver;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Represents an entity capable of reading in a playlist file and returning
 * a List containing PlaylistEntries that represent the files &amp; URIs in that playlist.
 * @author jcaron
 */
public interface IPlaylistReader
{
  Charset getEncoding();

  PlaylistType getPlaylistType();

  List<PlaylistEntry> readPlaylist(IProgressObserver<String> input) throws IOException;

  List<PlaylistEntry> readPlaylist() throws IOException;
}
