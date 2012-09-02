/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2012 Jeremy Caron
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

package listfix.view.support;

import javax.swing.ImageIcon;

/**
 *
 * @author jcaron
 */
public class ImageIcons
{
	public static final ImageIcon IMG_MISSING	= new ImageIcon(ImageIcons.class.getResource("/images/missing_text_icon.png")); //icon-missing.png"));
	public static final ImageIcon IMG_FOUND		= new ImageIcon(ImageIcons.class.getResource("/images/found_text_icon.png")); // icon-found.png"));
	public static final ImageIcon IMG_FIXED		= new ImageIcon(ImageIcons.class.getResource("/images/fixed_text_icon.png")); //icon-fixed.png"));
	public static final ImageIcon IMG_URL		= new ImageIcon(ImageIcons.class.getResource("/images/url_text_icon.png")); //icon-url.png"));
}
