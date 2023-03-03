package listfix.io.filters;

import listfix.model.playlists.Playlist;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Set;

public abstract class FileExtensionFilterBase extends FileFilter
{
  private Set<String> extensions = Playlist.playlistExtensions;

  protected FileExtensionFilterBase(Set<String> extensions)
  {
    this.extensions = extensions;
  }

  @Override
  public boolean accept(File file)
  {
    if (file.isDirectory())
    {
      return true;
    }

    String name = file.getName();
    int ix = name.lastIndexOf('.');
    if (ix >= 0 && ix < name.length() - 1)
    {
      String ext = name.substring(ix + 1).toLowerCase();
      return extensions.contains(ext);
    }
    else
    {
      return false;
    }
  }

}
