package listfix.io.filters;

import io.github.borewit.lizzy.content.type.ContentType;
import io.github.borewit.lizzy.playlist.SpecificPlaylistProvider;
import java.io.File;
import java.io.FileFilter;

public class SpecificPlaylistFileFilter extends FileExtensionFilterBase implements FileFilter {
  private final SpecificPlaylistProvider provider;
  private final ContentType contentType;

  public SpecificPlaylistFileFilter(SpecificPlaylistProvider provider, ContentType contentType) {
    super(contentType.getDescription(), contentType.getExtensions());
    this.provider = provider;
    this.contentType = contentType;
  }

  public boolean accept(File file) {
    return true;
  }

  public SpecificPlaylistProvider getPlaylistProvider() {
    return this.provider;
  }

  public ContentType getContentType() {
    return this.contentType;
  }
}
