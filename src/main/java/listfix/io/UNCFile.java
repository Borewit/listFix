package listfix.io;

import pspdash.NetworkDriveList;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class UNCFile extends File
{
  private static final NetworkDriveList driveLister = new NetworkDriveList();
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


  public UNCFile(String pathname)
  {
    super(pathname);
  }


  public UNCFile(File parent, String child)
  {
    super(parent, child);
  }


  public UNCFile(String parent, String child)
  {
    super(parent, child);
  }


  public UNCFile(URI uri)
  {
    super(uri);
  }


  public UNCFile(File file)
  {
    super(file.getPath());
  }


  public String getDrivePath()
  {
    String result = this.getPath();
    if (this.isInUNCFormat())
    {
      result = driveLister.fromUNCName(this.getAbsolutePath());
    }
    return result;
  }


  public String getUNCPath()
  {
    String result = this.getPath();
    if (this.onNetworkDrive())
    {
      result = driveLister.toUNCName(this.getAbsolutePath());
    }
    return result;
  }


  public boolean isInUNCFormat()
  {
    return this.getAbsolutePath().startsWith("\\\\");
  }


  public static List<UNCFile> listMappedRoots()
  {
    return networkDrives;
  }

  /**
   * Return true if this file is not on a local hard drive.
   *
   * @return True if the file is on a network drive, false otherwise.
   */
  public boolean onNetworkDrive()
  {
    return driveLister.onNetworkDrive(this.getAbsolutePath());
  }

  public static UNCFile from(File file)
  {
    return new UNCFile(file);
  }
}
