package listfix.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class BufferedProgressReader extends BufferedReader
{

  // Used to keep track how much of the file we've read.
  private long charsRead = 0;

  public BufferedProgressReader(Reader in, int sz)
  {
    super(in, sz);
  }

  public BufferedProgressReader(Reader in)
  {
    super(in);
  }

  // Custom readLine implementation that appends to the internal cache so we know how much of the file we've read.
  @Override
  public String readLine() throws IOException
  {
    String line = super.readLine();
    if (line != null)
    {
      // Not the End-Of-File yet, count bytes
      charsRead += line.length() + 1;
    }
    return line;
  }

  /**
   * Returns number of characters read.
   */
  public long getCharactersRead()
  {
    return this.charsRead;
  }
}
