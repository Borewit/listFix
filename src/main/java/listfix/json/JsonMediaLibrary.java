package listfix.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import java.util.TreeSet;
import listfix.config.IMediaLibrary;

public class JsonMediaLibrary implements IMediaLibrary {

  private final TreeSet<String> directories;
  private final TreeSet<String> nestedDirectories;
  private final TreeSet<String> nestedMediaFiles;

  public JsonMediaLibrary() {
    this.directories = new TreeSet<>();
    this.nestedDirectories = new TreeSet<>();
    this.nestedMediaFiles = new TreeSet<>();
  }

  @Override
  @JsonProperty("directories")
  public Set<String> getMediaDirectories() {
    return this.directories;
  }

  @Override
  public Set<String> getNestedDirectories() {
    return this.nestedDirectories;
  }

  @Override
  public Set<String> getNestedMediaFiles() {
    return this.nestedMediaFiles;
  }
}
