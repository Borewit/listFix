

package listfix.model;


public class EditFilenameResult
{
  private final int resultCode;
  private final String filename;

  /**
   *
   * @param a
   * @param b
   */
  public EditFilenameResult(int a, String b)
  {
    resultCode = a;
    filename = b;
  }

  /**
   *
   * @return
   */
  public String getFileName()
  {
    return filename;
  }

  /**
   *
   * @return
   */
  public int getResultCode()
  {
    return resultCode;
  }
}
