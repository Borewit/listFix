package listfix.model;

public class EditFilenameResultObject
{
    private int resultCode;
    private String filename;
    
    public EditFilenameResultObject(int a, String b)
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
