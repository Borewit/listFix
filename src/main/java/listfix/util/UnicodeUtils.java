package listfix.util;

import listfix.io.UnicodeInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;


public class UnicodeUtils
{
  public static String getBOM(String enc) throws UnsupportedEncodingException
  {
    switch (enc)
    {
      case "UTF-8" ->
      {
        byte[] bom = new byte[3];
        bom[0] = (byte) 0xEF;
        bom[1] = (byte) 0xBB;
        bom[2] = (byte) 0xBF;
        return new String(bom, enc);
      }
      case "UTF-16BE" ->
      {
        byte[] bom = new byte[2];
        bom[0] = (byte) 0xFE;
        bom[1] = (byte) 0xFF;
        return new String(bom, enc);
      }
      case "UTF-16LE" ->
      {
        byte[] bom = new byte[2];
        bom[0] = (byte) 0xFF;
        bom[1] = (byte) 0xFE;
        return new String(bom, enc);
      }
      case "UTF-32BE" ->
      {
        byte[] bom = new byte[4];
        bom[0] = (byte) 0x00;
        bom[1] = (byte) 0x00;
        bom[2] = (byte) 0xFE;
        bom[3] = (byte) 0xFF;
        return new String(bom, enc);
      }
      case "UTF-32LE" ->
      {
        byte[] bom = new byte[4];
        bom[0] = (byte) 0x00;
        bom[1] = (byte) 0x00;
        bom[2] = (byte) 0xFF;
        bom[3] = (byte) 0xFE;
        return new String(bom, enc);
      }
    }
    throw new UnsupportedEncodingException("");
  }

  public static Charset getEncoding(Path path)
  {
    return getEncoding(path.toFile());
  }

  public static Charset getEncoding(File input)
  {
    try (UnicodeInputStream stream = new UnicodeInputStream(new FileInputStream(input), StandardCharsets.UTF_8))
    {
      return Charset.forName(stream.getEncoding());
    }
    catch (IOException ioException)
    {
      throw new RuntimeException("Failed to determine encoding", ioException);
    }
  }
}
