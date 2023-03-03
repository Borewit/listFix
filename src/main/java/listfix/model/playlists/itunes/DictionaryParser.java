package listfix.model.playlists.itunes;

import christophedelory.plist.Array;
import christophedelory.plist.Dict;
import christophedelory.plist.PlistObject;

/**
 * The ghetto-tastic format used by iTunes (plist in lizzy-speak) has
 * an awkward in-memory structure; this class provides helper methods
 * for reading the data out of one of these objects based on the
 * datatype stored under the given key.
 *
 * @author jcaron
 */
public class DictionaryParser
{
  /**
   * Wraps
   * @throws ClassCastException Thrown when the datatype of the given key is not a christophedelory.plist.Dict.
   */
  public static Dict getKeyValueAsDict(Dict dict, String key) throws ClassCastException
  {
    return (Dict) dict.getDictionary().get(new christophedelory.plist.Key(key));
  }

  public static void setKeyValue(Dict dict, String key, PlistObject value) throws ClassCastException
  {
    dict.getDictionary().put(new christophedelory.plist.Key(key), value);
  }

  /**
   * Read an Array from the key-value-pair dict
   * @throws ClassCastException Thrown when the datatype of the given key is not a christophedelory.plist.Array.
   */
  public static Array getKeyValueAsArray(Dict dict, String key) throws ClassCastException
  {
    return (Array) dict.getDictionary().get(new christophedelory.plist.Key(key));
  }

  /**
   * Read an Integer from the key-value-pair dict
   * @throws ClassCastException Thrown when the datatype of the given key is not a christophedelory.plist.Integer.
   */
  public static christophedelory.plist.Integer getKeyValueAsInteger(Dict dict, String key) throws ClassCastException
  {
    return (christophedelory.plist.Integer) dict.getDictionary().get(new christophedelory.plist.Key(key));
  }

  /**
   * Read a String from the key-value-pair dict
   * @throws ClassCastException Thrown when the datatype of the given key is not a String.
   */
  public static String getKeyValueAsString(Dict dict, String keyName) throws ClassCastException
  {
    Object value = dict.getDictionary().get(new christophedelory.plist.Key(keyName));
    return value == null ? null : ((christophedelory.plist.String) value).getValue();
  }
}
