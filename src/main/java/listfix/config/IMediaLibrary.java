package listfix.config;

import java.util.Set;

public interface IMediaLibrary
{
  Set<String> getDirectories();

  Set<String> getNestedDirectories();

  Set<String> getNestedMediaFiles();
}
