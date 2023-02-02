package listfix.io.writers.playlists;

import listfix.io.Constants;
import listfix.io.FileUtils;
import listfix.io.IPlaylistOptions;
import listfix.io.UNCFile;
import listfix.model.playlists.FilePlaylistEntry;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;
import listfix.model.playlists.UriPlaylistEntry;
import listfix.util.OperatingSystem;
import listfix.view.support.ProgressAdapter;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public abstract class PlaylistWriter<C> implements IPlaylistWriter
{
  protected final IPlaylistOptions playListOptions;

  public PlaylistWriter(IPlaylistOptions playListOptions)
  {
    this.playListOptions = playListOptions;
  }

  protected static String normalizeTrackPath(boolean saveRelative, PlaylistEntry entry)
  {
    if (entry instanceof FilePlaylistEntry)
    {
      FilePlaylistEntry filePlaylistEntry = (FilePlaylistEntry) entry;
      Path trackPath = saveRelative ?
        filePlaylistEntry.getPlaylistPath().relativize(filePlaylistEntry.getAbsolutePath()) :
        filePlaylistEntry.getAbsolutePath();

      return trackPath.toString();
    }
    else if (entry instanceof UriPlaylistEntry)
    {
      ((UriPlaylistEntry) entry).getURI().toString();
    }
    throw new IllegalArgumentException("entry of unsupported type");
  }

  protected static void normalizeEntry(IPlaylistOptions playListOptions, Playlist playlist, PlaylistEntry entry) throws IOException
  {
    final boolean saveRelative = playListOptions.getSavePlaylistsWithRelativePaths();
    final File playlistFile = playlist.getFile();

    if (entry instanceof FilePlaylistEntry)
    {
      FilePlaylistEntry filePlaylistEntry = (FilePlaylistEntry) entry;
      if (!saveRelative && entry.isRelative())
      {
        // replace existing relative entry with a new absolute one
        File absolute = filePlaylistEntry.getAbsolutePath().toFile().getCanonicalFile();

        // Switch to UNC representation if selected in the options
        if (playListOptions.getAlwaysUseUNCPaths())
        {
          UNCFile temp = new UNCFile(absolute);
          absolute = new File(temp.getUNCPath());
        }
        ((FilePlaylistEntry) entry).setTrackPath(absolute.toPath());
      }
      else
      {
        if (saveRelative && entry.isFound())
        {
          // replace existing entry with a new relative one
          String relativePath = FileUtils.getRelativePath(filePlaylistEntry.getAbsolutePath().toFile().getCanonicalFile(), playlistFile);
          if (!OperatingSystem.isWindows() && relativePath.indexOf(Constants.FS) < 0)
          {
            relativePath = Path.of(".", relativePath).toString();
          }

          // make a new file out of this relative path, and see if it's really relative...
          // if it's absolute, we have to perform the UNC check and convert if necessary.
          File temp = new File(relativePath);
          if (temp.isAbsolute())
          {
            // Switch to UNC representation if selected in the options
            if (playListOptions.getAlwaysUseUNCPaths())
            {
              UNCFile uncd = new UNCFile(temp);
              temp = new File(uncd.getUNCPath());
            }
          }
          // make the entry and addAt it
          ((FilePlaylistEntry) entry).setTrackPath(temp.toPath());
        }
      }
    }

  }

  /**
   * Saves the playlist to disk.
   *
   * @param playlist     The list to persist to disk.
   * @param saveRelative Specifies if the playlist should be written out relatively or not.
   * @param adapter      An optionally null progress adapter which lets other code monitor the progress of this operation.
   * @throws Exception   If the playlist failed to save
   */
  @Override
  public void save(Playlist playlist, boolean saveRelative, @Nullable ProgressAdapter<String> adapter) throws Exception
  {
    C collector = this.initCollector();
    final int totalSteps = playlist.getEntries().size() + 2;

    if (adapter != null) {
      adapter.setTotal(totalSteps);
    }

    this.writeHeader(collector, playlist);
    adapter.stepCompleted();

    int index = 0;
    for (PlaylistEntry entry : playlist.getEntries())
    {
      normalizeEntry(this.playListOptions, playlist, entry);
      this.writeEntry(collector, entry, index++);
      if (adapter != null)
      {
        if (adapter.getCancelled())
        {
          break;
        }
        adapter.stepCompleted();
      }
    }

    if (adapter != null && !adapter.getCancelled())
    {
      this.finalize(collector, playlist);
      adapter.setCompleted(totalSteps);
    }
  }

  protected abstract C initCollector() throws Exception;

  protected void writeHeader(C collector, Playlist playlist) throws Exception
  {
  }

  protected abstract void writeEntry(C collector, PlaylistEntry entry, int index) throws Exception;

  protected abstract void finalize(C collector, Playlist playlist) throws Exception;

}
