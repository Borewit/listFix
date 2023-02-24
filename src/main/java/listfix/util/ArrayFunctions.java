

package listfix.util;

/*
============================================================================
= Author:   Jeremy Caron
= File:     ArrayFunctions.java
= Purpose:  A compilation of commonly used array functions, including
=           several which mimic Vector functionality.
============================================================================
 */

import java.util.Collection;
import java.util.List;


public class ArrayFunctions
{
  /**
   *
   * @param array1
   * @param constant
   * @return
   */
  public static float[] multByConstant(int[] array1, float constant)
  {
    float[] temp = new float[array1.length];
    for (int i = 0; i < array1.length; i++)
    {
      temp[i] = (float) array1[i] * constant;
    }
    return temp;
  }

  /**
   *
   * @param array1
   * @param constant
   * @return
   */
  public static float[] multByConstant(float[] array1, int constant)
  {
    float[] temp = new float[array1.length];
    for (int i = 0; i < array1.length; i++)
    {
      temp[i] = array1[i] * (float) constant;
    }
    return temp;
  }

  /**
   *
   * @param array1
   * @param array2
   * @return
   * @throws Exception
   */
  public static float[] addTwoArrays(float[] array1, float[] array2) throws Exception
  {
    if (array1.length == array2.length)
    {
      float[] temp = new float[array1.length];
      for (int i = 0; i < array1.length; i++)
      {
        temp[i] = array1[i] + array2[i];
      }
      return temp;
    }
    else
    {
      throw new Exception("You are a monkey... add two arrays that have the same size!!!");
    }
  }

  /**
   *
   * @param array1
   * @param array2
   * @return
   * @throws Exception
   */
  public static int[] addTwoArrays(int[] array1, int[] array2) throws Exception
  {
    if (array1.length == array2.length)
    {
      int[] temp = new int[array1.length];
      for (int i = 0; i < array1.length; i++)
      {
        temp[i] = array1[i] + array2[i];
      }
      return temp;
    }
    else
    {
      throw new Exception("You are a monkey... add two arrays that have the same size!!!");
    }
  }

  /**
   *
   * @param array1
   * @param value
   * @return
   */
  public static int[] copyArrayAddOneValue(int[] array1, int value)
  {
    int[] copied = new int[array1.length + 1];
    System.arraycopy(array1, 0, copied, 0, array1.length);
    copied[copied.length - 1] = value;
    return copied;
  }

  /**
   *
   * @param array1
   * @param value
   * @return
   */
  public static String[] copyArrayAddOneValue(String[] array1, String value)
  {
    String[] copied = new String[array1.length + 1];
    System.arraycopy(array1, 0, copied, 0, array1.length);
    copied[copied.length - 1] = value;
    return copied;
  }

  /**
   *
   * @param array1
   * @param location
   * @return
   */
  public static String[] removeItem(String[] array1, int location)
  {
    String[] result = new String[array1.length - 1];
    System.arraycopy(array1, 0, result, 0, location);
    for (int i = location + 1; i < array1.length; i++)
    {
      result[i - 1] = array1[i];
    }
    return result;
  }

  /**
   *
   * @param a
   * @param b
   * @return
   */
  public static String[] mergeArray(String[] a, String[] b)
  {
    int c = a.length + b.length;
    String[] cArray = new String[c];
    System.arraycopy(a, 0, cArray, 0, a.length);
    for (int i = a.length; i < c; i++)
    {
      cArray[i] = b[i - a.length];
    }
    return cArray;
  }

  /**
   *
   * @param a
   * @param b
   * @param ignoreCase
   * @return
   */
  public static boolean containsStringPrefixingAnotherString(String[] a, String b, boolean ignoreCase)
  {
    boolean result = false;
    if (a != null && a.length > 0)
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

  /**
   *
   * @param a
   * @param b
   * @param ignoreCase
   * @return
   */
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

  /**
   *
   * @param v
   * @return
   */
  public static String[] convertVectorToStringArray(List<Object> v)
  {
    int size = v.size();
    String[] result = new String[size];
    for (int i = 0; i < size; i++)
    {
      result[i] = v.get(i).toString();
    }
    return result;
  }

  /**
   *
   * @param list
   * @return
   */
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
