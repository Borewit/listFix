package listfix.json;

import listfix.config.IMediaLibrary;

import java.util.Set;
import java.util.TreeSet;

public class JsonMediaLibrary implements IMediaLibrary
{

  private final TreeSet<String> directories;
  private final TreeSet<String> nestedDirectories;
  private final TreeSet<String> nestedMediaFiles;
  private final TreeSet<String> playlistDirectories;

  public JsonMediaLibrary()
  {
    this.directories = new TreeSet<>();
    this.nestedDirectories = new TreeSet<>();
    this.nestedMediaFiles = new TreeSet<>();
    this.playlistDirectories = new TreeSet<>();
  }

  @Override
  public Set<String> getMediaDirectories()
  {
    return this.directories;
  }

  @Override
  public Set<String> getNestedDirectories()
  {
    return this.nestedDirectories;
  }

  @Override
  public Set<String> getNestedMediaFiles()
  {
    return this.nestedMediaFiles;
  }

  @Override
  public Set<String> getPlaylistDirectories()
  {
    return playlistDirectories;
  }
}
