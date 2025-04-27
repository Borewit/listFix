package listfix.util;

public class OperatingSystem {

  public static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0;
  }

  public static boolean isMac() {
    return System.getProperty("os.name").toLowerCase().indexOf("mac os") >= 0;
  }

  public static boolean isLinux() {
    return !isWindows() && !isMac();
  }
}
