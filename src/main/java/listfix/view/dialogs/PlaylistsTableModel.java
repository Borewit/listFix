
package listfix.view.dialogs;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import listfix.model.BatchRepair;
import listfix.model.BatchRepairItem;
import listfix.model.playlists.Playlist;
import listfix.view.support.ImageIcons;

/**
 *
 * @author jcaron
 */
public class PlaylistsTableModel extends AbstractTableModel
{
	/**
	 *
	 * @param items
	 */
	public PlaylistsTableModel(BatchRepair items)
	{
		_items = items;
	}

	/**
	 *
	 * @return
	 */
	@Override
	public int getRowCount()
	{
		return _items == null ? 0 : _items.getItems().size();
	}

	/**
	 *
	 * @return
	 */
	@Override
	public int getColumnCount()
	{
		return 2;
	}

	/**
	 *
	 * @param column
	 * @return
	 */
	@Override
	public String getColumnName(int column)
	{
		switch (column)
		{
			case 0:
				return "";
			case 1:
				return "Name";
			default:
				return null;
		}
	}

	/**
	 *
	 * @param columnIndex
	 * @return
	 */
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

	/**
	 *
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
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
						return ImageIcons.IMG_MISSING; 
					}
					else if (list.getFixedCount() > 0)
					{
						return ImageIcons.IMG_FIXED; 
					}
					else
					{
						return ImageIcons.IMG_FOUND; 
					}
				case 1:
					return item.getDisplayName();
				default:
					return null;
			}
		}
	}

	private BatchRepair _items;
}
