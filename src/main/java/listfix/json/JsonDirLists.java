package listfix.json;

import java.util.Set;
import java.util.TreeSet;

public class JsonDirLists
{

  private Set<String> mediaDirs;
  private Set<String> mediaLibraryDirectories;
  private Set<String> mediaLibraryFiles;

  public JsonDirLists()
  {
    this.mediaDirs = new TreeSet<>();
    this.mediaLibraryDirectories = new TreeSet<>();
    this.mediaLibraryFiles = new TreeSet<>();
  }

  public Set<String> getMediaDirs()
  {
    return this.mediaDirs;
  }

  public void setMediaDirs(Set<String> mediaDirs)
  {
    this.mediaDirs = mediaDirs;
  }

  public Set<String> getMediaLibraryDirectories()
  {
    return this.mediaLibraryDirectories;
  }

  public void setMediaLibraryDirectories(Set<String> mediaLibraryDirectories)
  {
    this.mediaLibraryDirectories = mediaLibraryDirectories;
  }

  public Set<String> getMediaLibraryFiles()
  {
    return this.mediaLibraryFiles;
  }

  public void setMediaLibraryFiles(Set<String> mediaLibraryFiles)
  {
    this.mediaLibraryFiles = mediaLibraryFiles;
  }


}
