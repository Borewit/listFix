package listfix.tasks;

import listfix.model.*;
import listfix.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public class CopyFilesTask extends listfix.controller.Task 
{
    private static Vector files;
    private static File destination;
    
    /** Creates new CopyFilesTask */
    public CopyFilesTask(Vector x, File y) 
    {
        files = x;
        destination = y;
    }

    /** Run the task. This method is the body of the thread for this task.  */
    public void run() 
    {
        PlaylistEntry tempEntry = null;
        File fileToCopy = null;
        File dest = null;
        String fs = System.getProperty("file.separator");
        for (int i = 0; i < files.size(); i++)
        {
            tempEntry = (PlaylistEntry) files.elementAt(i);
            if (!tempEntry.isURL())
            {
                fileToCopy = tempEntry.getFile();
                if (tempEntry.isFound() && fileToCopy.exists())
                {
                    dest = new File(destination.getPath() + fs + tempEntry.getFileName());
                    try
                    {
                        FileCopier.copy(new FileInputStream(fileToCopy), new FileOutputStream(dest));
                    }
                    catch (IOException e)
                    {
                        // eat the error and continue
                        e.printStackTrace();
                    }
                }
            }
            this.notifyObservers((int)((double)i/(double)(files.size()-1) * 100.0));
        }   
    }
}
