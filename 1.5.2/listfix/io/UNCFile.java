package listfix.io;

import java.io.File;
import java.net.URI;
import java.util.Vector;
import pspdash.NetworkDriveList;

/**
 *
 * @author jcaron
 */
public class UNCFile extends File
{
    private static NetworkDriveList driveLister = new NetworkDriveList();
    private static final Vector<UNCFile> networkDrives = new Vector<UNCFile>();
    
    static
    {
        File[] roots = File.listRoots();
		for(int i=0; i < roots.length; i++)
		{
			try
			{
                UNCFile file = new UNCFile(roots[i]);
				if (file.onNetworkDrive())
				{					
					networkDrives.add(file);
				}
			}
			catch (Exception e)
			{
				// eat the error and continue
				e.printStackTrace();
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
    
    public static Vector<UNCFile> listMappedRoots()
    {
        return networkDrives;
    }
    
    public boolean onNetworkDrive()
    {
        return driveLister.onNetworkDrive(this.getAbsolutePath());
    } 
}
