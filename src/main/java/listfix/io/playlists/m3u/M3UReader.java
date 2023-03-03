package listfix.io.playlists.m3u;

import listfix.io.BufferedProgressReader;
import listfix.io.IPlaylistOptions;
import listfix.io.playlists.PlaylistReader;
import listfix.model.enums.PlaylistType;
import listfix.model.playlists.PlaylistEntry;
import listfix.view.support.IProgressObserver;
import listfix.view.support.ProgressAdapter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads in a M3U/M3U8 file and returns a List containing PlaylistEntries that represent the files &amp; URIs in the playlist.
 */
public class M3UReader extends PlaylistReader
{
  private final List<PlaylistEntry> results = new ArrayList<>();
  private static final PlaylistType type = PlaylistType.M3U;

  public M3UReader(IPlaylistOptions playListOptions, Path m3uPath) throws IOException
  {
    super(playListOptions, m3uPath);
  }

  @Override
  public Charset getEncoding()
  {
    return encoding;
  }

  @Override
  public PlaylistType getPlaylistType()
  {
    return type;
  }

  @Override
  public List<PlaylistEntry> readPlaylist(IProgressObserver<String> observer) throws IOException
  {
    long fileLength = Files.size(this.playlistPath);

    try (BufferedProgressReader buffer = openBufferedReader(this.playlistPath))
    {
      // See http://gonze.com/playlists/playlist-format-survey.html#M3U for the format of an M3U file.
      // Line1 holds the metadata about the file that we just hang on to, line2 represents the file reference.

      //Initialize the progress adapter if we're given an observer.
      ProgressAdapter<String> progress = ProgressAdapter.make(observer);
      progress.setTotal(fileLength);

      String line1 = buffer.readLine();
      String line2;
      if (line1 != null)
      {
        // Ignore the standard M3U header and random mediamonkey crap.
        while (line1.contains("#EXTM3U") || line1.startsWith("#EXTINFUTF8") || line1.isEmpty())
        {
          line1 = buffer.readLine();
          if (line1 == null)
          {
            // needed to handle empty playlists
            return results;
          }
        }

        // If after skipping that line the line doesn't start w/ a #, then we already have the file reference.  Stuff that into line2.
        if (!line1.startsWith("#"))
        {
          line2 = line1;
          line1 = "";
        }
        else
        {
          // Otherwise, read in the next line which should be our file reference.
          line2 = buffer.readLine();
          while (line2.startsWith("#"))
          {
            // throw away non-standard metadata added by mediamonkey...
            line2 = buffer.readLine();
          }
        }

        while (line1 != null)
        {
          // If we have an observer and the user cancelled, bail out.
          if (observer != null)
          {
            if (observer.getCancelled())
            {
              return null;
            }
          }

          // Process the two strings we have into a playlist entry
          processEntry(line2, line1);

          // We just processed an entry, update the progress bar w/ the % of the file we've read if we have an observer.
          long bytesRead = Math.min(fileLength, buffer.getCharactersRead());
          progress.setCompleted(bytesRead);

          // Start processing the next entry.
          line1 = buffer.readLine();
          if (line1 != null)
          {
            // WMP produces M3Us with spaces between entries... have to read in an extra line to avoid this if line1 is empty.
            // Let's also handle an arbitrary number of spaces between the entries while we're at it.
            while (line1.isEmpty())
            {
              line1 = buffer.readLine();

              // And of course WMP ends the file w/ several blank lines, so if we find a null here return what we have...
              if (line1 == null)
              {
                // Fill the progress bar
                progress.setCompleted(fileLength);

                return results;
              }
            }

            if (!line1.startsWith("#"))
            {
              line2 = line1;
              line1 = "";
            }
            else
            {
              line2 = buffer.readLine();
              while (line2.startsWith("#"))
              {
                // throw away non-standard metadata added by mediamonkey...
                line2 = buffer.readLine();
              }
            }
          }
        }
      }
      progress.setCompleted(fileLength);
      return results;
    }
  }

  @Override
  public List<PlaylistEntry> readPlaylist() throws IOException
  {
    readPlaylist(null);
    return results;
  }


  private void processEntry(String L2, String extInf)
  {
    super.processEntry(this.results, L2, extInf, null);
  }
}
