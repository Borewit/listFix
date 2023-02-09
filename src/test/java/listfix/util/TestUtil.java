package listfix.util;

import java.io.File;
import java.net.URL;

public class TestUtil
{
  public static File createFileFromResource(Object testObject, String resourcePath)
  {
    URL url = testObject.getClass().getResource(resourcePath);
    assert url != null;
    return new File(url.getFile());
  }
}
