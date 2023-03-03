package listfix.io.playlists.m3u;

import listfix.io.Constants;
import listfix.io.IPlaylistOptions;
import listfix.io.playlists.PlaylistWriter;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;
import listfix.util.OperatingSystem;
import listfix.util.UnicodeUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A playlist writer capable of saving to M3U or M3U8 format.
 */
public class M3UWriter extends PlaylistWriter<StringBuilder>
{
  public M3UWriter(IPlaylistOptions options)
  {
    super(options);
  }

  @Override
  protected StringBuilder initCollector()
  {
    return new StringBuilder();
  }

  @Override
  protected void writeHeader(StringBuilder buffer, Playlist playlist)
  {
    buffer.append("#EXTM3U").append(Constants.BR);
  }

  @Override
  protected void writeEntry(StringBuilder buffer, PlaylistEntry entry, int index)
  {
    buffer.append(serializeEntry(entry)).append(Constants.BR);
  }

  private String serializeEntry(PlaylistEntry entry)
  {
    StringBuilder result = new StringBuilder();
    if (!(entry.getExtInf() == null) && !entry.getExtInf().equals(""))
    {
      result.append(entry.getExtInf());
      result.append(Constants.BR);
    }
    result.append(entry.trackPathToString());
    return result.toString();
  }

  @Override
  protected void finalize(StringBuilder buffer, Playlist playlist) throws IOException
  {
    final File playListFile = playlist.getFile();

    final File dirToSaveIn = playListFile.getParentFile().getAbsoluteFile();
    if (!dirToSaveIn.exists())
    {
      dirToSaveIn.mkdirs();
    }

    FileOutputStream outputStream = new FileOutputStream(playListFile);
    if (playlist.isUtfFormat() || playListFile.getName().toLowerCase().endsWith("m3u8"))
    {
      Writer osw = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
      try (BufferedWriter output = new BufferedWriter(osw))
      {
        if (OperatingSystem.isWindows())
        {
          // For some reason, linux players seem to choke on this header when I addAt it... perhaps the stream classes do it automatically.
          output.write(UnicodeUtils.getBOM("UTF-8"));
        }
        output.write(buffer.toString());
      }
      outputStream.close();
      playlist.setUtfFormat(true);
    }
    else
    {
      try (BufferedOutputStream output = new BufferedOutputStream(outputStream))
      {
        output.write(buffer.toString().getBytes(UTF_8));
      }
      outputStream.close();
      playlist.setUtfFormat(false);
    }
  }
}
