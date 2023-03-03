package listfix.io;

import listfix.util.OperatingSystem;
import listfix.view.controls.JTransparentTextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.lang.reflect.Method;

/**
 * A class who's sole responsibility is to launch the default web browser on various OSes.
 */
public class BrowserLauncher
{
  private static final String _errMsg = "Error attempting to launch default web browser";
  private static final Logger _logger = LogManager.getLogger(BrowserLauncher.class);

  /**
   * Attempts to open the given url in the system's default web browser.
   * Logs failure details and silently fails if a browser can't be opened.
   *
   * @param url The URL to display in the browser, if successfully launched.
   */
  public static void launch(String url)
  {
    try
    {
      if (OperatingSystem.isMac())
      {
        Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
        Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
        openURL.invoke(null, url);
      }
      else if (OperatingSystem.isWindows())
      {
        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
      }
      else
      {
        //assume Unix or Linux
        String[] possibleBrowsers =
          {
            "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"
          };
        String browser = null;
        for (int count = 0; count < possibleBrowsers.length && browser == null; count++)
        {
          if (Runtime.getRuntime().exec(new String[]{"which", possibleBrowsers[count]}).waitFor() == 0)
          {
            browser = possibleBrowsers[count];
          }
        }
        if (browser == null)
        {
          throw new Exception("Could not find web browser");
        }
        else
        {
          Runtime.getRuntime().exec(new String[]{browser, url});
        }
      }
    }
    catch (Exception e)
    {
      JOptionPane.showMessageDialog(null, new JTransparentTextArea(_errMsg + ": " + e.getLocalizedMessage()));
      _logger.warn(e);
    }
  }
}
