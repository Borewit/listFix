package listfix.config;

import listfix.controller.tasks.WriteMediaLibraryIniTask;
import listfix.exceptions.MediaDirNotFoundException;
import listfix.io.UNCFile;
import listfix.json.JsonMediaLibrary;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static listfix.io.Constants.DATA_DIR;

/**
 * @author Borewit
 */
public class MediaLibraryConfiguration extends ApplicationConfigFile<JsonMediaLibrary>
{
  private static final String path_json_dirLists = DATA_DIR + "mediaLibrary.json";

  public MediaLibraryConfiguration()
  {
    super(path_json_dirLists);
  }

  @Override
  public void read() throws IOException
  {
    this.jsonPojo = readJson(this.jsonFile, JsonMediaLibrary.class);
  }

  @Override
  public void initPojo()
  {
    this.jsonPojo = new JsonMediaLibrary();
  }

  public static MediaLibraryConfiguration load() throws IOException
  {
    MediaLibraryConfiguration config = new MediaLibraryConfiguration();
    config.init();
    return config;
  }

  private void clearMediaLibraryDirectoryList()
  {
    this.jsonPojo.getNestedDirectories().clear();
  }

  /**
   * @param dir
   * @return
   * @throws MediaDirNotFoundException
   */
  public void removeMediaDir(String dir) throws MediaDirNotFoundException
  {
    final Set<String> mediaDir = this.jsonPojo.getDirectories();
    final boolean found = mediaDir.remove(dir);

    if (found)
    {
      if (mediaDir.isEmpty())
      {
        this.jsonPojo.getNestedDirectories().clear();
        this.jsonPojo.getNestedMediaFiles().clear();
      }
      else
      {
        // Remove folders starting with dir
        this.jsonPojo.getNestedDirectories().removeIf(mediaLibDir -> mediaLibDir.startsWith(dir));
        // Remove files starting with dir
        this.jsonPojo.getNestedMediaFiles().removeIf(mediaLibDir -> mediaLibDir.startsWith(dir));
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
    for (String dir : new ArrayList<>(this.jsonPojo.getDirectories()))
    {
      if (!new File(dir).exists())
      {
        this.removeMediaDir(dir);
      }
    }
  }

  private static void normalizeFileSetToUNC(Set<String> paths)
  {
    Iterator<String> i = paths.iterator();
    paths.clear();

    while (i.hasNext())
    {
      String path = i.next();
      UNCFile file = new UNCFile(path);
      paths.add(file.onNetworkDrive() ? file.getUNCPath() : path);
    }
  }

  public void switchMediaLibraryToUNCPaths()
  {
    normalizeFileSetToUNC(this.getConfig().getNestedMediaFiles());
    normalizeFileSetToUNC(this.getConfig().getNestedDirectories());
    normalizeFileSetToUNC(this.getConfig().getNestedMediaFiles());
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
