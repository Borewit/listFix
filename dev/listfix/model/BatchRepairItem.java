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
import listfix.util.FileNameTokenizer;

public class BatchRepairItem
{
	public BatchRepairItem(File file)
	{
		_path = file.getPath();
		_displayName = file.getName();
		if (Playlist.isPlaylist(file))
		{
			_displayName = FileNameTokenizer.removeExtensionFromFileName(_displayName);
		}
	}

	public BatchRepairItem(String path, String displayName)
	{
		_path = path;
		_displayName = displayName;
	}
	private String _path;
	private String _displayName;
	private Playlist _playlist;

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
}
