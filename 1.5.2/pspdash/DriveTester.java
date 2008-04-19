package pspdash;

import java.io.File;

/**
 * @author jcaron
 */
public class DriveTester 
{
	public static void main(String[] args)
	{
		NetworkDriveList lister = new NetworkDriveList();
		System.out.println(lister.getUNCName("Y:\\"));
		System.out.println(lister.isNetworkDrive("Y"));
		File[] roots = File.listRoots();
		for(int i=0; i < roots.length; i++)
		{
			try
			{
				System.out.println("Running tests for the '" + roots[i].getAbsolutePath() + "' drive:");
				if (lister.isNetworkDrive(roots[i].getAbsolutePath().charAt(0) + ""))
				{					
					System.out.println("Drive is mapped to '" + lister.getUNCName(roots[i].getAbsolutePath())+ "'");
				}
				else
				{
					System.out.println("Drive is not a network drive...");
				}
			}
			catch (Exception e)
			{
				// eat the error and continue
				e.printStackTrace();
			}			
		}
		System.out.println(lister.toUNCName("Y:\\MP3s\\02. Rock - Alternative - Metal - Industrial\\Evanescence - Fallen\\Evanescence-Fallen-2003-RNS\\02-evanescence-bring_me_to_life-rns.mp3"));
	}
}
