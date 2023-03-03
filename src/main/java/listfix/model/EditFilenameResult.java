package listfix.model;


public class EditFilenameResult
{
  private final int resultCode;
  private final String filename;


  public EditFilenameResult(int a, String b)
  {
    resultCode = a;
    filename = b;
  }


  public String getFileName()
  {
    return filename;
  }


  public int getResultCode()
  {
    return resultCode;
  }
}
