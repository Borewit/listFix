
package listfix.view.dialogs;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import listfix.model.BatchRepair;
import listfix.model.BatchRepairItem;
import listfix.model.Playlist;
import listfix.view.support.ImageIcons;

public class PlaylistsTableModel extends AbstractTableModel
{
	public PlaylistsTableModel(BatchRepair items)
	{
		_items = items;
	}

	@Override
	public int getRowCount()
	{
		return _items == null ? 0 : _items.getItems().size();
	}

	@Override
	public int getColumnCount()
	{
		return 4;
	}

	@Override
	public String getColumnName(int column)
	{
		switch (column)
		{
			case 0:
				return "";
			case 1:
				return "Name";
			case 2:
				return "Location";
			default:
				return null;
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		if (columnIndex == 0)
		{
			return ImageIcon.class;
		}
		else
		{
			return Object.class;
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		if (_items == null)
		{
			return null;
		}
		else
		{
			BatchRepairItem item = _items.getItem(rowIndex);
			Playlist list = item.getPlaylist();
			switch (columnIndex)
			{
				case 0:
					if (list.getMissingCount() > 0)
					{
						return ImageIcons.IMG_MISSING; // red
					}
					else if (list.getFixedCount() > 0)
					{
						return ImageIcons.ING_FIXED; // light green
					}
					else
					{
						return ImageIcons.IMG_FOUND; // dark green
					}
				case 1:
					return item.getDisplayName();
				case 2:
					return item.getPath();
				default:
					return null;
			}
		}
	}

	private BatchRepair _items;
}
