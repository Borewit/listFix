package listfix.util;

import java.util.Collection;
import java.util.List;

/**
 * A compilation of commonly used array functions, including several which mimic Vector functionality.
 */
public class ArrayFunctions
{

  public static boolean containsStringPrefixingAnotherString(String[] a, String b, boolean ignoreCase)
  {
    boolean result = false;
    if (a != null)
    {
      for (String a1 : a)
      {
        if (ignoreCase ? b.toLowerCase().startsWith(a1.toLowerCase()) : b.startsWith(a1))
        {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  public static boolean containsStringPrefixingAnotherString(List<String> a, String b, boolean ignoreCase)
  {
    boolean result = false;
    if (a != null && a.size() > 0)
    {
      for (String a1 : a)
      {
        if (ignoreCase ? b.toLowerCase().startsWith(a1.toLowerCase()) : b.startsWith(a1))
        {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  public static boolean containsStringPrefixingAnotherString(Collection<String> a, String b, boolean ignoreCase)
  {
    boolean result = false;
    for (String a1 : a)
    {
      if (ignoreCase ? b.toLowerCase().startsWith(a1.toLowerCase()) : b.startsWith(a1))
      {
        result = true;
        break;
      }
    }
    return result;
  }

  public static int[] integerListToArray(List<Integer> list)
  {
    int[] rows = new int[list.size()];
    for (int i = 0; i < list.size(); i++)
    {
      rows[i] = list.get(i);
    }
    return rows;
  }
}
