package listfix.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import listfix.io.IPlaylistOptions;

public class FileNameTokenizer {
  private static final String separators = " .-_[]{},/\\`'~!@#$%^\"&*()+=|:;";
  public List<String> ignoreList = new ArrayList<>();

  public FileNameTokenizer(IPlaylistOptions filePathOptions) {
    StringTokenizer tokenMaker =
        new StringTokenizer(
            UnicodeUtils.normalizeNfc(filePathOptions.getIgnoredSmallWords()), " ,;|");
    while (tokenMaker.hasMoreTokens()) {
      ignoreList.add(tokenMaker.nextToken());
    }
  }

  public int score(String filename1, String filename2) {
    return scoreMatchingTokens(
        splitFileName(UnicodeUtils.normalizeNfc(filename1)),
        splitFileName(UnicodeUtils.normalizeNfc(filename2)));
  }

  public String removeExtensionFromFileName(String name) {
    name = UnicodeUtils.normalizeNfc(name);
    String result = name;
    int index = name.lastIndexOf(".");
    if (index >= 0) {
      result = name.substring(0, index);
    }
    return result;
  }

  public static File changeExtension(File f, String newExtension) {
    String fileName = UnicodeUtils.normalizeNfc(f.getName());
    int i = fileName.lastIndexOf('.');
    String name = fileName.substring(0, i);
    return new File(f.getParent(), name + newExtension);
  }

  public String getExtensionFromFileName(String name) {
    name = UnicodeUtils.normalizeNfc(name);
    int ix = name.lastIndexOf('.');
    if (ix >= 0 && ix < name.length() - 1) return name.substring(ix + 1);
    else return "";
  }

  private List<String> splitFileName(String fileName) {
    fileName = UnicodeUtils.normalizeNfc(removeExtensionFromFileName(fileName));
    StringTokenizer tokenMaker = new StringTokenizer(fileName, separators);
    int tokenCount = tokenMaker.countTokens();
    List<String> result = new ArrayList<>();
    String token;
    for (int i = 0; i < tokenCount; i++) {
      token = UnicodeUtils.normalizeNfc(tokenMaker.nextToken());
      if (token.length() > 1 && !ignoreList.contains(token)) {
        result.add(token);
      }
    }
    return result;
  }

  private int scoreMatchingTokens(List<String> array1, List<String> array2) {
    HashMap<String, Integer> array1Counts = new HashMap<>();
    HashMap<String, Integer> array2Counts = new HashMap<>();

    for (String t : array1) {
      if (array1Counts.containsKey(t)) {
        array1Counts.put(t, array1Counts.get(t) + 1);
      } else {
        array1Counts.put(t, 1);
      }
    }

    for (String t : array2) {
      if (array1Counts.containsKey(t)) {
        if (array2Counts.containsKey(t)) {
          array2Counts.put(t, array2Counts.get(t) + 1);
        } else {
          array2Counts.put(t, 1);
        }
      }
    }

    int result = 0;
    if (array2Counts.keySet().size() > 0) {
      result =
          array2Counts.keySet().size() == 1 ? 1 : (int) Math.pow(3.0, array2Counts.keySet().size());
      for (String s : array2Counts.keySet()) {
        int array1Count = array1Counts.get(s);
        int array2Count = array2Counts.get(s);
        if (array1Count < array2Count) {
          result += ((s.length() - 1) * array1Count);
        } else {
          result += ((s.length() - 1) * array2Count);
        }
      }
    }
    return result;
  }
}
