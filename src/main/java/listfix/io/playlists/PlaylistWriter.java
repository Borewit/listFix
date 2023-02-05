package listfix.io.playlists;

import listfix.io.FileUtils;
import listfix.io.IPlaylistOptions;
import listfix.io.UNCFile;
import listfix.model.playlists.FilePlaylistEntry;
import listfix.model.playlists.Playlist;
import listfix.model.playlists.PlaylistEntry;
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

  protected static void normalizeEntry(IPlaylistOptions playListOptions, Playlist playlist, PlaylistEntry entry) throws IOException
  {
    final boolean saveRelative = playListOptions.getSavePlaylistsWithRelativePaths();

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
          Path relativePath = FileUtils.getRelativePath(filePlaylistEntry.getAbsolutePath(), playlist.getPath());
          if (!OperatingSystem.isWindows() && !relativePath.toString().contains("/"))
          {
            relativePath = Path.of(".").resolve(relativePath); // ToDo: is this required?
          }

          // make a new file out of this relative path, and see if it's really relative...
          // if it's absolute, we have to perform the UNC check and convert if necessary.
          File temp = relativePath.toFile();
          if (temp.isAbsolute())
          {
            // Switch to UNC representation if selected in the options
            if (playListOptions.getAlwaysUseUNCPaths())
            {
              UNCFile uncFile = new UNCFile(temp);
              temp = new File(uncFile.getUNCPath());
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
    if (adapter == null) {
      // Create a dummy progress-adapter
      adapter = ProgressAdapter.wrap(null);
    }

    final int totalSteps = playlist.getEntries().size() + 3;
    adapter.setTotal(totalSteps);
    C collector = this.initCollector();
    adapter.stepCompleted(); // Extra step 1/3

    this.writeHeader(collector, playlist);
    adapter.stepCompleted(); // Extra step 2/3

    int index = 0;
    for (PlaylistEntry entry : playlist.getEntries())
    {
      normalizeEntry(this.playListOptions, playlist, entry);
      this.writeEntry(collector, entry, index++);
      if (adapter.getCancelled())
      {
        break;
      }
      adapter.stepCompleted();
    }

    this.finalize(collector, playlist);
    adapter.setCompleted(totalSteps); // Extra step 3/3
  }

  protected abstract C initCollector() throws Exception;

  protected void writeHeader(C collector, Playlist playlist) throws Exception
  {
  }

  protected abstract void writeEntry(C collector, PlaylistEntry entry, int index) throws Exception;

  protected abstract void finalize(C collector, Playlist playlist) throws Exception;

}
