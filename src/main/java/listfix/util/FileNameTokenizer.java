package listfix.util;

import listfix.io.IPlaylistOptions;

import java.io.File;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class FileNameTokenizer
{
  private static final String separators = " .-_[]{},/\\`'~!@#$%^\"&*()+=|:;";
  public List<String> ignoreList = new ArrayList<>();

  /**
   * Normalize a filename or path string to NFC form.
   *
   * @param input String to normalize.
   * @return Normalized string.
   */
  public static String normalizeFilename(String input) {
    return input == null ? null : Normalizer.normalize(input, Form.NFC);
  }

  public FileNameTokenizer(IPlaylistOptions filePathOptions)
  {
    StringTokenizer tokenMaker = new StringTokenizer(normalizeFilename(filePathOptions.getIgnoredSmallWords()), " ,;|");
    while (tokenMaker.hasMoreTokens())
    {
      ignoreList.add(tokenMaker.nextToken());
    }
  }

  public int score(String filename1, String filename2)
  {
    return scoreMatchingTokens(splitFileName(normalizeFilename(filename1)), splitFileName(normalizeFilename(filename2)));
  }

  public String removeExtensionFromFileName(String name)
  {
    name = normalizeFilename(name);
    String result = name;
    int index = name.lastIndexOf(".");
    if (index >= 0)
    {
      result = name.substring(0, index);
    }
    return result;
  }

  public static File changeExtension(File f, String newExtension)
  {
    String fileName = normalizeFilename(f.getName());
    int i = fileName.lastIndexOf('.');
    String name = fileName.substring(0, i);
    return new File(f.getParent(), name + newExtension);
  }

  public String getExtensionFromFileName(String name)
  {
    name = normalizeFilename(name);
    int ix = name.lastIndexOf('.');
    if (ix >= 0 && ix < name.length() - 1)
      return name.substring(ix + 1);
    else
      return "";
  }

  private List<String> splitFileName(String fileName)
  {
    fileName = normalizeFilename(removeExtensionFromFileName(fileName));
    StringTokenizer tokenMaker = new StringTokenizer(fileName, separators);
    int tokenCount = tokenMaker.countTokens();
    List<String> result = new ArrayList<>();
    String token;
    for (int i = 0; i < tokenCount; i++)
    {
      token = normalizeFilename(tokenMaker.nextToken());
      if (token.length() > 1 && !ignoreList.contains(token))
      {
        result.add(token);
      }
    }
    return result;
  }

  private int scoreMatchingTokens(List<String> array1, List<String> array2)
  {
    HashMap<String, Integer> array1Counts = new HashMap<>();
    HashMap<String, Integer> array2Counts = new HashMap<>();

    for (String t : array1)
    {
      if (array1Counts.containsKey(t))
      {
        array1Counts.put(t, array1Counts.get(t) + 1);
      }
      else
      {
        array1Counts.put(t, 1);
      }
    }

    for (String t : array2)
    {
      if (array1Counts.containsKey(t))
      {
        if (array2Counts.containsKey(t))
        {
          array2Counts.put(t, array2Counts.get(t) + 1);
        }
        else
        {
          array2Counts.put(t, 1);
        }
      }
    }

    int result = 0;
    if (array2Counts.keySet().size() > 0)
    {
      result = array2Counts.keySet().size() == 1 ? 1 : (int) Math.pow(3.0, array2Counts.keySet().size());
      for (String s : array2Counts.keySet())
      {
        int array1Count = array1Counts.get(s);
        int array2Count = array2Counts.get(s);
        if (array1Count < array2Count)
        {
          result += ((s.length() - 1) * array1Count);
        }
        else
        {
          result += ((s.length() - 1) * array2Count);
        }
      }
    }
    return result;
  }
}
