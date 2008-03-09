package listfix.util;

import java.util.StringTokenizer;
import java.util.*;

public class FileNameTokenizer
{
    public static final Vector ignoreList = new Vector();
    static
    {
        ignoreList.add("an");
        ignoreList.add("of");
        ignoreList.add("the");
        ignoreList.add("in");
        ignoreList.add("dsp");
        ignoreList.add("my");
        ignoreList.add("and");
        ignoreList.add("to");
    }
    
    public static String[] splitFileName(String fileName)
    {
        String[] result = new String[0];
        if (fileName.toLowerCase().endsWith(".mp3"))
        {
            fileName = fileName.substring(0, fileName.length() - 4);
        }
        StringTokenizer tokenMaker = new StringTokenizer(fileName, " .-_[]{},/\\`'~!@#$%^\"&*()+=|:;");
        int tokenCount = tokenMaker.countTokens();
        Vector tempResult = new Vector();
        for (int i = 0; i < tokenCount; i++)
        {
            String token = tokenMaker.nextToken();
            if (token.length() > 1 && !ignoreList.contains(token.toLowerCase()))
            {
                tempResult.add(token);
            }
        }
        result = new String[tempResult.size()];
        tempResult.copyInto(result);
        return result;
    }
    
    public static int countMatchingTokens(String[] array1, String[] array2)
    {
        int result = 0; int array1Size = array1.length; int array2Size = array2.length;
        for (int i = 0; i < array1Size; i++)
        {
            for (int j = 0; j < array2Size; j++)
            {
                if (array1[i].equalsIgnoreCase(array2[j]))
                {
                    result++;
                }
            }
        }
        return result;
    }  
    
}