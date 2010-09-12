/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2010 Jeremy Caron
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

/**
 *
 * Needed to support DnD.
 */
public class PlaylistEntryList implements Transferable
{
	private List<PlaylistEntry> _list;

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[]
			{
				new DataFlavor(PlaylistEntryList.class, "PlaylistEntryList")
			};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return flavor.equals(new DataFlavor(PlaylistEntryList.class, "PlaylistEntryList"));
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		return this;
	}

	public PlaylistEntryList(List<PlaylistEntry> list)
	{
		_list = list;
	}

	public List<PlaylistEntry> getList()
	{
		return _list;
	}
}
