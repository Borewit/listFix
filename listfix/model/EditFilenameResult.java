package listfix.model;

public class EditFilenameResult
{
    private int resultCode;
    private String filename;
    
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
