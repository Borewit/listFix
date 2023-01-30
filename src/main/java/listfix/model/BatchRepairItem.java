/**
 * listFix() - Fix Broken Playlists!
 *
 * This file is part of listFix().
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.model;

import java.io.File;
import java.util.List;

import listfix.io.IPlayListOptions;
import listfix.model.playlists.Playlist;
import listfix.util.FileNameTokenizer;

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

  public BatchRepairItem(File file, IPlayListOptions filePathOptions)
  {
    _path = file.getPath();
    _displayName = file.getName();
    _playlistFile = file;
    if (Playlist.isPlaylist(file, filePathOptions))
    {
      _displayName = (new FileNameTokenizer(filePathOptions)).removeExtensionFromFileName(_displayName);
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
