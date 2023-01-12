package listfix.config;

import listfix.controller.tasks.WriteMediaLibraryIniTask;
import listfix.exceptions.MediaDirNotFoundException;
import listfix.io.UNCFile;
import listfix.json.JsonDirLists;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import static listfix.io.Constants.DATA_DIR;

/**
 * @author Borewit
 */
public class MediaLibraryConfiguration extends ApplicationConfigFile<JsonDirLists>
{
  private static final String path_json_dirLists = DATA_DIR + "dirLists.json";

  public MediaLibraryConfiguration()
  {
    super(path_json_dirLists);
  }

  @Override
  public void read() throws IOException
  {
    this.jsonPojo = readJson(this.jsonFile, JsonDirLists.class);
  }

  @Override
  public void initPojo()
  {
    this.jsonPojo = new JsonDirLists();
  }

  public static MediaLibraryConfiguration load() throws IOException
  {
    MediaLibraryConfiguration config = new MediaLibraryConfiguration();
    config.init();
    return config;
  }

  private void clearMediaLibraryDirectoryList()
  {
    this.jsonPojo.getMediaLibraryDirectories().clear();
  }

  /**
   * @param dir
   * @return
   * @throws MediaDirNotFoundException
   */
  public void removeMediaDir(String dir) throws MediaDirNotFoundException
  {
    final Set<String> mediaDir = this.jsonPojo.getMediaDirs();
    final boolean found = mediaDir.remove(dir);

    if (found)
    {
      if (mediaDir.isEmpty())
      {
        this.jsonPojo.getMediaLibraryDirectories().clear();
        this.jsonPojo.getMediaLibraryFiles().clear();
      }
      else
      {
        // Remove folders starting with dir
        this.jsonPojo.setMediaDirs(this.jsonPojo.getMediaLibraryDirectories().stream().filter(libDir -> libDir.startsWith(dir)).collect(Collectors.toSet()));
        // Remove files starting with dir
        this.jsonPojo.setMediaLibraryFiles(this.jsonPojo.getMediaLibraryFiles().stream().filter(libDir -> libDir.startsWith(dir)).collect(Collectors.toSet()));
      }
      this.writeOnBackground();
    }
    else
    {
      throw new MediaDirNotFoundException(dir + " was not in the media directory list.");
    }
  }

  public void cleanNonExistingMediaDirectories() throws MediaDirNotFoundException
  {
    for (String dir : new ArrayList<>(this.jsonPojo.getMediaDirs()))
    {
      if (!new File(dir).exists())
      {
        this.removeMediaDir(dir);
      }
    }
  }

  private static Set<String> normalizeFileSetToUNC(Set<String> input)
  {
    return input.stream().map(path -> {
      UNCFile file = new UNCFile(path);
      return file.onNetworkDrive() ? file.getUNCPath() : path;
    }).collect(Collectors.toSet());
  }

  public void switchMediaLibraryToUNCPaths()
  {
    this.getConfig().setMediaLibraryFiles(normalizeFileSetToUNC(this.getConfig().getMediaLibraryFiles()));
    this.getConfig().setMediaLibraryDirectories(normalizeFileSetToUNC(this.getConfig().getMediaLibraryDirectories()));
    this.getConfig().setMediaLibraryFiles(normalizeFileSetToUNC(this.getConfig().getMediaLibraryFiles()));
  }

  /**
   * Spawns a background task to write the media library to disk.
   */
  public void writeOnBackground()
  {
    WriteMediaLibraryIniTask thisTask = new WriteMediaLibraryIniTask(this);
    thisTask.start();
  }

}
