

package listfix.view.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.StringTokenizer;

public class FontExtensions
{
  private static final Logger _logger = LogManager.getLogger(FontExtensions.class);

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
