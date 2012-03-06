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

package listfix.io;

import java.util.Set;

public class AudioFileFilter extends FileExtensionFilterBase
{
    public AudioFileFilter()
    {
        super(_extensions);
    }

    @Override
    public String getDescription()
    {
        return "Audio Files and Playlists (*.m3u, *.m3u8, *.pls, *.wpl, *.mp3, *.flac, *.aac, *.ogg, *.aiff, *.au, *.wma)";
    }
    
    private static final Set<String> _extensions;

    static
    {
        _extensions = createExtensionSet("m3u", "m3u8", "pls", "wpl", "mp3", "flac", "aac", "ogg", "aiff", "au", "wma");
    }

	@Override
	// Fixes display in linux
	public String toString()
	{
		return getDescription();
	}
}
