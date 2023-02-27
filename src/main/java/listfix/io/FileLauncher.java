

package listfix.io;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.awt.*;
import java.io.*;
import listfix.util.OperatingSystem;


public class FileLauncher
{
  /**
   *
   * 
   * 
   * 
   */
  public static void launch(File toLaunch) throws IOException, InterruptedException
  {
    // try to figure out the OS so we can issue the correct command
    if (OperatingSystem.isWindows())
    {
      File tempFile = File.createTempFile("abc", ".bat");
      tempFile.deleteOnExit();

      // write out the .bat file...
      StringBuilder buffer = new StringBuilder("chcp 1252\nset myvar=\"");
      buffer.append(toLaunch.getCanonicalPath());
      buffer.append("\"\n");
      buffer.append("start \"\" %myvar%");

      try (FileOutputStream outputStream = new FileOutputStream(tempFile); Writer osw = new OutputStreamWriter(outputStream, UTF_8))
      {
        osw.write(buffer.toString());
      }

      String cmdLine = tempFile.toString();

      Process proc = Runtime.getRuntime().exec(cmdLine);
      synchronized (proc)
      {
        proc.wait(100);
      }

      InputStream stream = proc.getErrorStream();
      BufferedReader streamTwo = new BufferedReader(new InputStreamReader(stream, UTF_8));
      String line = null;
      if (streamTwo.ready())
      {
        line = streamTwo.readLine();
      }
      while (line != null)
      {
        if (streamTwo.ready())
        {
          line = streamTwo.readLine();
        }
        else
        {
          line = null;
        }
      }
    }
    else
    {
      if (Desktop.isDesktopSupported())
      {
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.OPEN))
        {
          desktop.open(toLaunch);
        }
      }
    }
  }
}
