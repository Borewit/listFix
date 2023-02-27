package listfix.model;

import listfix.io.IPlaylistOptions;
import listfix.model.playlists.Playlist;
import listfix.util.FileNameTokenizer;

import java.io.File;
import java.util.List;

/**
 * Serves to model a batch repair operation on a single playlist, for both exact and closest matches repairs.
 * @author jcaron
 */

public class BatchRepairItem
{
  private String _path;
  private String _displayName;
  private Playlist _playlist;
  private File _playlistFile;
  private List<BatchMatchItem> _closestMatches;

  public BatchRepairItem(File file, IPlaylistOptions filePathOptions)
  {
    _path = file.getPath();
    _displayName = file.getName();
    _playlistFile = file;
    if (Playlist.isPlaylist(file.toPath()))
    {
      _displayName =  new FileNameTokenizer(filePathOptions).removeExtensionFromFileName(_displayName);
    }
  }

  public String getPath()
  {
    return _path;
  }

  public void setPath(String path)
  {
    this._path = path;
  }

   public String getDisplayName()
  {
    return _displayName;
  }

  public void setDisplayName(String displayName)
  {
    this._displayName = displayName;
  }

  public Playlist getPlaylist()
  {
    return _playlist;
  }

  public void setPlaylist(Playlist playlist)
  {
    this._playlist = playlist;
  }

  public File getPlaylistFile()
  {
    return _playlistFile;
  }

  public void setPlaylistFile(File playlistFile)
  {
    this._playlistFile = playlistFile;
  }

  void setClosestMatches(List<BatchMatchItem> findClosestMatches)
  {
    _closestMatches = findClosestMatches;
  }

  public List<BatchMatchItem> getClosestMatches()
  {
    return _closestMatches;
  }
}
