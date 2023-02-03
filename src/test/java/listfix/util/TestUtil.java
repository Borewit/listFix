package listfix.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class TestUtil
{
  public static File createFileFromResource(Object testObject, String resourcePath) throws IOException
  {
    URL url = testObject.getClass().getResource(resourcePath);
    return new File(url.getFile());
  }
}
