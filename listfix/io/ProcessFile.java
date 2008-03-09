package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     ProcessFile.java
= Version:  1.0
= Purpose:  Read in the playlist file and return a Vector containing 
=           Mp3Objects that represent the files in the playlist.
============================================================================
*/

import listfix.model.MP3Object;
import listfix.view.support.*;
import listfix.tasks.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

public class ProcessFile
{
    private BufferedReader B;
    private String fs = System.getProperty("file.separator");
    private Vector results = new Vector();
    private long fileLength = 0;

    public ProcessFile(File in_data) throws FileNotFoundException
    {
        B = new BufferedReader(new FileReader(in_data));
        fileLength = in_data.length();
    }

    public Vector readM3U(OpenM3UTask input) throws IOException
    {
        StringBuilder cache = new StringBuilder();
        String line1 = B.readLine(); // ignore line 1
        cache.append(line1);
        String line2 = "";
        line1 = B.readLine();
        cache.append(line1);
        if (line1 != null)
        {
            if (!line1.startsWith("#EXTINF"))
            {
                line2 = line1;
                line1 = "";
            }
            else
            {
                line2 = B.readLine();
                cache.append(line2);
            }        
            while (line1 != null)
            {
                processMP3(line1, line2);
                input.notifyObservers((int)((double)cache.length()/(double)(fileLength + 1.0) * 100.0));
                line1 = B.readLine();
                if (line1 != null)
                {
                    if (!line1.startsWith("#EXTINF"))
                    {
                        line2 = line1;
                        line1 = "";
                    }
                    else
                    {
                        line2 = B.readLine();
                        cache.append(line2);
                    }    
                }
            }
        }
        return results;
    }
    
    public Vector readM3U() throws IOException
    {
        String line1 = B.readLine(); // ignore line 1
        String line2 = "";
        line1 = B.readLine();
        if (line1 != null)
        {
            if (!line1.startsWith("#EXTINF"))
            {
                line2 = line1;
                line1 = "";
            }
            else
            {
                line2 = B.readLine();
            }        
            while (line1 != null)
            {
                processMP3(line1, line2);
                line1 = B.readLine();
                if (line1 != null)
                {
                    if (!line1.startsWith("#EXTINF"))
                    {
                        line2 = line1;
                        line1 = "";
                    }
                    else
                    {
                        line2 = B.readLine();
                    }    
                }
            }
        }
        return results;
    }

    private void processMP3(String L1, String L2) throws IOException
    {
        StringTokenizer pathTokenizer = null;
        StringBuilder path = new StringBuilder();
        if (fs.equalsIgnoreCase("/")) //OS Specific Hack
        {
            if (!L2.startsWith("\\\\"))
            {
                path.append("/");
            }
            pathTokenizer = new StringTokenizer(L2, ":\\/");
        }
        if (fs.equalsIgnoreCase(":")) //OS Specific Hack
        {
            pathTokenizer = new StringTokenizer(L2, ":\\/");
        }
        if (fs.equalsIgnoreCase("\\")) //OS Specific Hack
        {
            pathTokenizer = new StringTokenizer(L2, "\\/");
        }
        
        String fileName = "";
        String extInf = L1;
        if (L2.startsWith("\\\\"))
        {
            path.append("\\\\");
        }
        
        int tokenNumber = 0;
        while(pathTokenizer.hasMoreTokens())
        {
            String word = pathTokenizer.nextToken();            
            if (tokenNumber == 0 && !L2.startsWith("\\\\") && !MP3Object.emptyDriveRoots.contains(word + fs))
            {
                // This token is the closest thing we have to the notion of a 'drive' on any OS... 
                // make a file out of this and see if it has any files.
                File testFile = new File(word + fs);
                if (!(testFile.exists() && testFile.isDirectory() && testFile.list().length > 0))
                {
                    MP3Object.emptyDriveRoots.add(path.toString() + word + fs);
                }
            }
            if(pathTokenizer.hasMoreTokens())
            {
                path.append(word);
                path.append(fs);
            }
            else
            {
                fileName = word;
            }
            tokenNumber++;
        }  
        results.addElement(new MP3Object(path.toString(), fileName, extInf));
    }

    public void close_file() throws IOException
    {
        B.close();
    }
}