/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2010 Jeremy Caron
 *
 *  This file is part of listFix().
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.model.playlists.itunes;

import listfix.model.playlists.FilePlaylistEntry;

import java.nio.file.Path;

/**
 *
 * @author jcaron
 */
public class ITunesFilePlaylistEntry extends FilePlaylistEntry implements  IITunesPlaylistEntry
{
  private final ITunesTrack _track;

  public ITunesFilePlaylistEntry(Path input, String title, long length, Path playlistPath, ITunesTrack track)
  {
    super(input, title, length, playlistPath);
    _track = track;
  }

  /**
   * @return the _track
   */
  public ITunesTrack getTrack()
  {
    return _track;
  }

  /**
   *
   * @return
   */
  @Override
  public ITunesFilePlaylistEntry clone()
  {
    ITunesFilePlaylistEntry clone = new ITunesFilePlaylistEntry(this.trackPath, this._title, this._length, this.playlistPath, _track);
    super.copyTo(clone);
    return clone;
  }
}
