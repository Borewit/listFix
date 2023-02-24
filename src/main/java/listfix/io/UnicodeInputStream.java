

package listfix.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;

public class UnicodeInputStream extends InputStream
{
  private final PushbackInputStream internalIn;
  private boolean isInited = false;
  private int BOMOffset = -1;
  private final String defaultEnc;
  private String encoding;

  public static final int BOM_SIZE = 4;

  public UnicodeInputStream(InputStream in, String defaultEnc)
  {
    internalIn = new PushbackInputStream(in, BOM_SIZE);
    this.defaultEnc = defaultEnc;
  }

  public UnicodeInputStream(InputStream in, Charset defaultEnc)
  {
    this(in, defaultEnc.name());
  }

  public String getDefaultEncoding()
  {
    return defaultEnc;
  }

  public String getEncoding()
  {
    if (!isInited)
    {
      try
      {
        init();
      }
      catch (IOException ex)
      {
        throw new IllegalStateException("Init method failed.", ex);
      }
    }
    return encoding;
  }

  /**
   * Read-ahead four bytes and check for BOM marks. Extra bytes are unread
   * back to the stream, only BOM bytes are skipped.
   */
  protected void init() throws IOException
  {
    if (isInited)
    {
      return;
    }

    byte[] bom = new byte[BOM_SIZE];
    int n, unread;
    n = internalIn.read(bom, 0, bom.length);

    if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) && (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF))
    {
      encoding = "UTF-32BE";
      unread = n - 4;
    }
    else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE) && (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00))
    {
      encoding = "UTF-32LE";
      unread = n - 4;
    }
    else if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF))
    {
      encoding = "UTF-8";
      unread = n - 3;
    }
    else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF))
    {
      encoding = "UTF-16BE";
      unread = n - 2;
    }
    else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE))
    {
      encoding = "UTF-16LE";
      unread = n - 2;
    }
    else
    {
      // Unicode BOM mark not found, unread all bytes
      encoding = defaultEnc;
      unread = n;
    }
    BOMOffset = BOM_SIZE - unread;
    if (unread > 0)
    {
      internalIn.unread(bom, (n - unread), unread);
    }

    isInited = true;
  }

  @Override
  public void close() throws IOException
  {
    // init();
    isInited = true;
    internalIn.close();
  }

  @Override
  public int read() throws IOException
  {
    init();
    isInited = true;
    return internalIn.read();
  }

  public int getBOMOffset()
  {
    return BOMOffset;
  }
}
