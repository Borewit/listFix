
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
package listfix.util;

public class Log
{
	static private String _indentTxt = "";
	static private int _indentLevel;

	static public void write(String fmt, Object... args)
	{
		System.out.print(_indentTxt);
		System.out.printf(fmt, args);
		System.out.println();
	}	

	static public void indent()
	{
		_indentLevel++;
		refreshIndent();
	}

	static public void unindent()
	{
		_indentLevel--;
		refreshIndent();
	}

	static private void refreshIndent()
	{
		if (_indentLevel > 0)
		{
			StringBuilder sb = new StringBuilder();
			for (int ix = 0; ix < _indentLevel; ix++)
			{
				sb.append("    ");
			}
			_indentTxt = sb.toString();
		}
		else
		{
			_indentTxt = "";
		}
	}
}
