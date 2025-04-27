package listfix.io.filters;

import listfix.io.FileUtils;

public class AudioFileFilter extends FileExtensionFilterBase {

  public AudioFileFilter() {
    super("Audio Files and Playlists", FileUtils.mediaExtensions);
  }
}
