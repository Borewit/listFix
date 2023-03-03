package listfix.io.playlists;

import listfix.model.playlists.Playlist;
import listfix.view.support.ProgressAdapter;

/**
 * Generic contract for a class that can save a playlist to disk.
 */
public interface IPlaylistWriter
{
  /**
   * The primary method a playlist writer needs, save.
   *
   * @param list         The list to persist to disk.
   * @param saveRelative Specifies if the playlist should be written out relatively or not.
   * @param adapter      An optionally null progress adapter which lets other code monitor the progress of this operation.
   */
  void save(Playlist list, boolean saveRelative, ProgressAdapter<String> adapter) throws Exception;
}
