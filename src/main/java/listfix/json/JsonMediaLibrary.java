package listfix.json;

import listfix.config.IMediaLibrary;

import java.util.Set;
import java.util.TreeSet;

public class JsonMediaLibrary implements IMediaLibrary
{

  private final Set<String> directories;
  private final Set<String> nestedDirectories;
  private final Set<String> nestedMediaFiles;

  public JsonMediaLibrary()
  {
    this.directories = new TreeSet<>();
    this.nestedDirectories = new TreeSet<>();
    this.nestedMediaFiles = new TreeSet<>();
  }

  public Set<String> getDirectories()
  {
    return this.directories;
  }

  public Set<String> getNestedDirectories()
  {
    return this.nestedDirectories;
  }
  public Set<String> getNestedMediaFiles()
  {
    return this.nestedMediaFiles;
  }
}
