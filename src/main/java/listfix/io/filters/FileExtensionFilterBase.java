package listfix.io.filters;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.filechooser.FileFilter;
import listfix.io.FileUtils;

public abstract class FileExtensionFilterBase extends FileFilter {
  private final String description;
  private final Set<String> extensions;

  protected FileExtensionFilterBase(String description, Collection<String> extensions) {
    this.description = description;
    this.extensions =
        extensions.stream()
            .map(extension -> extension.startsWith(".") ? extension.substring(1) : extension)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
  }

  protected FileExtensionFilterBase(String description, String[] extensions) {
    this(description, List.of(extensions));
  }

  @Override
  public boolean accept(File file) {
    if (!file.isFile()) {
      return false;
    }

    Optional<String> extension = FileUtils.getExtension(file.getName());
    return extension.filter(s -> extensions.contains(s.toLowerCase())).isPresent();
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  // Fixes display in linux
  public String toString() {
    return getDescription();
  }
}
