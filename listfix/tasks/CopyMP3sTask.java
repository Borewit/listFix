package listfix.tasks;

import listfix.model.*;
import listfix.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public class CopyMP3sTask extends listfix.view.support.Task {

    private static Vector mp3s;
    private static File destination;
    
    /** Creates new CopyMP3sTask */
    public CopyMP3sTask(Vector x, File y) {
        mp3s = x;
        destination = y;
    }

    /** Run the task. This method is the body of the thread for this task.  */
    public void run() 
    {
        MP3Object tempMp3 = null;
        File fileToCopy = null;
        File dest = null;
        String fs = System.getProperty("file.separator");
        for (int i = 0; i < mp3s.size(); i++)
        {
            tempMp3 = (MP3Object) mp3s.elementAt(i);
            fileToCopy = tempMp3.getFile();
            if (fileToCopy.exists())
            {
                dest = new File(destination.getPath() + fs + tempMp3.getFileName());
                try 
                {
                    FileCopier.copy(new FileInputStream(fileToCopy), new FileOutputStream(dest));
                }
                catch(IOException e)
                {
                    
                }
            }
            this.notifyObservers((int)((double)i/(double)(mp3s.size()-1) * 100.0));
        }   
    }
}
