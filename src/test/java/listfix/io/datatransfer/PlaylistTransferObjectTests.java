package listfix.io.datatransfer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlaylistTransferObjectTests
{
  @Test
  public void serializeAndDeserializeFileList() throws IOException
  {
    List<String> entryList = List.of(new String[] {
      "C:\\Users\\borewit\\Music\\track01.mp3",
      "C:\\Users\\borewit\\Music\\Artist - song.flac"
    });

    try(ByteArrayOutputStream bufferOutputStream = new ByteArrayOutputStream()) {
      // Serialize list to buffer
      PlaylistTransferObject.serialize(entryList, bufferOutputStream);

      System.out.println(String.format("M3U:\n%s", new String(bufferOutputStream.toByteArray())));

      try(ByteArrayInputStream inputStream = new ByteArrayInputStream(bufferOutputStream.toByteArray())) {
        List<String> deserializedEntryList = PlaylistTransferObject.deserialize(inputStream);
        assertEquals(entryList, deserializedEntryList);
      }
    }
  }
}

