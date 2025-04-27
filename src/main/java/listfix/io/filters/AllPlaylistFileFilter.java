package listfix.io.filters;

import java.io.File;
import java.io.FileFilter;
import listfix.io.playlists.LizzyPlaylistUtil;

/** A FileFilter that accepts our currently supported playlist types, and directories. */
public class AllPlaylistFileFilter extends FileExtensionFilterBase implements FileFilter {
  public AllPlaylistFileFilter() {
    super("All Playlists", LizzyPlaylistUtil.playlistExtensions);
  }

  @Override
  public String getDescription() {
    return "Playlists";
  }

  @Override
  public boolean accept(File file) {
    return file.isDirectory() || super.accept(file);
  }

  @Override
  public String toString() {
    return getDescription();
  }
}
