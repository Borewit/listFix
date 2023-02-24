package listfix.io;

import java.io.*;
import java.util.ArrayList;


public class StringArrayListSerializer
{
  public static String serialize(ArrayList<String> input) throws IOException
  {
    String result;
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream os = new ObjectOutputStream(bos))
    {
      os.writeObject(input);
      result = new String(Base64Coder.encode(bos.toByteArray()));
    }
    return result;
  }

  public static ArrayList<String> deserialize(String input) throws IOException, ClassNotFoundException
  {
    ArrayList<String> result;
    try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64Coder.decode(input)); ObjectInputStream oInputStream = new ObjectInputStream(bis))
    {
      result = (ArrayList<String>) oInputStream.readObject();
    }
    return result;
  }
}
