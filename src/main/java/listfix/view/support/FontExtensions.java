/*
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

package listfix.view.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.StringTokenizer;

/**
 *
 * @author jcaron
 */
public class FontExtensions
{
  private static final Logger _logger = LogManager.getLogger(FontExtensions.class);

  /**
   *
   * @param inputFont
   * @return
   */
  public static String getStyle(Font inputFont)
  {
    if (inputFont.isPlain())
    {
      return "PLAIN";
    }
    else if (inputFont.isItalic())
    {
      return "ITALIC";
    }
    else if (inputFont.isBold())
    {
      return "BOLD";
    }
    else
    {
      return "BOLD+ITALIC";
    }
  }

  /**
   *
   * @param inputFont
   * @return
   */
  public static String serialize(Font inputFont)
  {
    return inputFont.getFamily() + "," + inputFont.getStyle() + "," + inputFont.getSize();
  }

  /**
   *
   * @param csvParams
   * @return
   */
  public static Font deserialize(String csvParams)
  {
    try
    {
      StringTokenizer tizer = new StringTokenizer(csvParams, ",");
      String tok;
      int i = 0;
      String family = null;
      String style = null;
      String size = null;
      while (tizer.hasMoreTokens())
      {
        tok = tizer.nextToken();
        if (i == 0)
        {
          family = tok;
        }
        else if (i == 1)
        {
          style = tok;
        }
        else if (i == 2)
        {
          size = tok;
        }
        i++;
      }
      return new Font(family, Integer.parseInt(style), Integer.parseInt(size));
    }
    catch (Exception ex)
    {
      _logger.info(ex);
      return null;
    }
  }

  /**
   *
   * @param chosenFont
   * @return
   */
  public static String formatFont(Font chosenFont)
  {
    return chosenFont.getFamily() + ", " + FontExtensions.getStyle(chosenFont) + ", " + chosenFont.getSize();
  }
}
