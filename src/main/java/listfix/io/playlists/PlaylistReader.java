package listfix.io.playlists;

import listfix.io.BufferedProgressReader;
import listfix.io.Constants;
import listfix.io.IPlaylistOptions;
import listfix.model.playlists.FilePlaylistEntry;
import listfix.model.playlists.PlaylistEntry;
import listfix.model.playlists.UriPlaylistEntry;
import listfix.util.ArrayFunctions;
import listfix.util.OperatingSystem;
import listfix.view.GUIScreen;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.StringTokenizer;

public abstract class PlaylistReader implements IPlaylistReader
{
  protected final IPlaylistOptions playListOptions;
  protected final Path playlistPath;
  protected Charset encoding;

  private final Logger logger = LogManager.getLogger(GUIScreen.class);

  public PlaylistReader(IPlaylistOptions playListOptions, Path playlistPath) {
    this.playListOptions = playListOptions;
    this.playlistPath = playlistPath;
  }

  public Path getPath()
  {
    return this.playlistPath;
  }

  @Override
  public Charset getEncoding()
  {
    return this.encoding;
  }

  public BufferedProgressReader openBufferedReader(Path textFile) throws IOException
  {
    final BOMInputStream bomInputStream = new BOMInputStream(new FileInputStream(textFile.toFile()),
      ByteOrderMark.UTF_8,
      ByteOrderMark.UTF_16BE,
      ByteOrderMark.UTF_16LE,
      ByteOrderMark.UTF_32BE,
      ByteOrderMark.UTF_32LE
    );
    final String charsetName = bomInputStream.getBOMCharsetName();
    this.encoding = charsetName == null ? StandardCharsets.UTF_8 : Charset.forName(charsetName);

    this.logger.info(String.format("Detected playlist file encoding for \"%s\": %s", textFile.getFileName().toString(), this.encoding.name()));
    return new BufferedProgressReader(new InputStreamReader(bomInputStream, this.encoding));
  }

  protected void processEntry(List<PlaylistEntry> results, String L2, String cid, String tid)
  {
    StringTokenizer pathTokenizer = null;
    StringBuilder path = new StringBuilder();
     if (OperatingSystem.isLinux()) // OS Specific Hack
    {
      if (!L2.startsWith("\\\\") && !L2.startsWith(".") && !L2.startsWith(Constants.FS))
      {
        // Need to append ./ on relative entries to load them properly
        path.append("./");
      }
      pathTokenizer = new StringTokenizer(L2, ":\\/");
    }
    else if (Constants.FS.equalsIgnoreCase(":")) // OS Specific Hack
    {
      pathTokenizer = new StringTokenizer(L2, ":\\/");
    }
    else if (Constants.FS.equalsIgnoreCase("\\")) // OS Specific Hack
    {
      pathTokenizer = new StringTokenizer(L2, "\\/");
      if (!L2.startsWith("\\\\") && L2.startsWith("\\"))
      {
        path.append("\\");
      }
    }

    if (pathTokenizer != null)
    {
      String fileName = "";
      String extInf = "";
      if (L2.startsWith("\\\\"))
      {
        path.append("\\\\");
      }
      else if (L2.startsWith(Constants.FS))
      {
        // We're about to lose this when we parse, so add it back...
        path.append(Constants.FS);
      }

      String firstToken = "";
      String secondToken = "";
      int tokenNumber = 0;
      File firstPathToExist = null;
      while (pathTokenizer.hasMoreTokens())
      {
        String word = pathTokenizer.nextToken();
        String tempPath = path + word + Constants.FS;
        if (tokenNumber == 0)
        {
          firstToken = word;
        }
        if (tokenNumber == 1)
        {
          secondToken = word;
        }
        if (tokenNumber == 0 && !L2.startsWith("\\\\") && !PlaylistEntry.NonExistentDirectories.contains(word + Constants.FS))
        {
          // This token is the closest thing we have to the notion of a 'drive' on any OS...
          // make a file out of this and see if it has any files.
          File testFile = new File(tempPath);
          if (!(testFile.exists() && testFile.isDirectory() && testFile.list().length > 0) && testFile.isAbsolute())
          {
            PlaylistEntry.NonExistentDirectories.add(tempPath);
          }
        }
        else if (L2.startsWith("\\\\") && pathTokenizer.countTokens() >= 1
          && !PlaylistEntry.NonExistentDirectories.contains("\\\\" + firstToken + Constants.FS)
          && !ArrayFunctions.containsStringPrefixingAnotherString(PlaylistEntry.ExistingDirectories, tempPath, true)
          && !ArrayFunctions.containsStringPrefixingAnotherString(PlaylistEntry.NonExistentDirectories, tempPath, true))
        {
          // Handle UNC paths specially
          File testFile = new File(tempPath);
          boolean exists = testFile.exists();
          if (exists)
          {
            PlaylistEntry.ExistingDirectories.add(tempPath);
            if (firstPathToExist == null)
            {
              firstPathToExist = testFile;
            }
          }
          if (!exists && pathTokenizer.countTokens() == 1)
          {
            PlaylistEntry.NonExistentDirectories.add(tempPath);
          }
          if (pathTokenizer.countTokens() == 1 && firstPathToExist == null)
          {
            // don't want to knock out the whole drive, as other folders might be accessible there...
            PlaylistEntry.NonExistentDirectories.add("\\\\" + firstToken + Constants.FS + secondToken + Constants.FS);
          }
        }
        if (pathTokenizer.hasMoreTokens())
        {
          path.append(word);
          path.append(Constants.FS);
        }
        else
        {
          fileName = word;
        }
        tokenNumber++;
      }
      results.add(new FilePlaylistEntry(Path.of(path.toString(), fileName), extInf, this.playlistPath, cid, tid));
    }
    else
    {
      try
      {
        results.add(new UriPlaylistEntry(new URI(L2.trim()), "", cid, tid));
      }
      catch (URISyntaxException e)
      {
        this.logger.warn("While adding URI entry to playlist", e);
        throw new RuntimeException(e);
      }
    }
  }
}
