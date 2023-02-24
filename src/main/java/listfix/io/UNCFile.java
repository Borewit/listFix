

package listfix.io;

import pspdash.NetworkDriveList;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class UNCFile extends File
{
  private static NetworkDriveList driveLister = new NetworkDriveList();
  private static final List<UNCFile> networkDrives = new ArrayList<>();

  static
  {
    File[] roots = File.listRoots();
    for (File root : roots)
    {
      UNCFile file = new UNCFile(root);
      if (file.onNetworkDrive())
      {
        networkDrives.add(file);
      }
    }
  }

  /**
   *
   * @param pathname
   */
  public UNCFile(String pathname)
  {
    super(pathname);
  }

  /**
   *
   * @param parent
   * @param child
   */
  public UNCFile(File parent, String child)
  {
    super(parent, child);
  }

  /**
   *
   * @param parent
   * @param child
   */
  public UNCFile(String parent, String child)
  {
    super(parent, child);
  }

  /**
   *
   * @param uri
   */
  public UNCFile(URI uri)
  {
    super(uri);
  }

  /**
   *
   * @param file
   */
  public UNCFile(File file)
  {
    super(file.getPath());
  }

  /**
   *
   * @return
   */
  public String getDrivePath()
  {
    String result = this.getPath();
    if (this.isInUNCFormat())
    {
      result = driveLister.fromUNCName(this.getAbsolutePath());
    }
    return result;
  }

  /**
   *
   * @return
   */
  public String getUNCPath()
  {
    String result = this.getPath();
    if (this.onNetworkDrive())
    {
      result = driveLister.toUNCName(this.getAbsolutePath());
    }
    return result;
  }

  /**
   *
   * @return
   */
  public boolean isInUNCFormat()
  {
    return this.getAbsolutePath().startsWith("\\\\");
  }

  /**
   *
   * @return
   */
  public static List<UNCFile> listMappedRoots()
  {
    return networkDrives;
  }

  /**
   * Return true if this file is not on a local hard drive.
   * @return True if the file is on a network drive, false otherwise.
   */
  public boolean onNetworkDrive()
  {
    return driveLister.onNetworkDrive(this.getAbsolutePath());
  }

  public static UNCFile from(File file) {
    return new UNCFile(file);
  }
}
