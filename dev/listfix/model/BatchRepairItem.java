
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
