

package listfix.util;


public class OperatingSystem
{
  /**
   *
   * @return
   */
  public static boolean isWindows()
  {
    return System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0;
  }

  /**
   *
   * @return
   */
  public static boolean isMac()
  {
    return System.getProperty("os.name").toLowerCase().indexOf("mac os") >= 0;
  }

  /**
   *
   * @return
   */
  public static boolean isLinux()
  {
    return !isWindows() && !isMac();
  }
}
