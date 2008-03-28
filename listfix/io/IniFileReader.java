package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     ProcessIniFile.java
= Purpose:  Read in the dirLists.ini file and return
=           a String array containing the directories listed in the file.
============================================================================
*/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class IniFileReader
{
    private BufferedReader B1;
    private BufferedReader B2;
    private String fname1;
    private String fname2;
    private String fs = System.getProperty("file.separator");
    private String homeDir = System.getProperty("user.home");
    private String[] mediaDirs = new String[0];
    private String[] history = new String[0];
    private String[] mediaLibrary = new String[0];
    private String[] mediaLibraryFiles = new String[0]; 

    public IniFileReader() throws FileNotFoundException
    {
        fname1 = homeDir + fs + "dirLists.ini";
        fname2 = homeDir + fs + "listFixHistory.ini";
        File in_data1 = new File(fname1);
        if (in_data1.length() == 0)
        {
            throw new FileNotFoundException("File found, but was of zero size.");
        }
        B1 = new BufferedReader(new FileReader(in_data1));
        
        File in_data2 = new File(fname2);
        if (in_data2.length() == 0)
        {
            throw new FileNotFoundException("File found, but was of zero size.");
        }
        B2 = new BufferedReader(new FileReader(in_data2));
    }

    public void readIni() throws IOException
    {
        Vector tempVector = new Vector();
        // ignore line 1, contains static data
        String line = B1.readLine(); 
        line = B1.readLine();
        while ( ( line != null) && ( !line.startsWith("[") ) )
        {
            tempVector.addElement(line);
            line = B1.readLine();
        }        
        mediaDirs = new String[tempVector.size()];
        tempVector.copyInto(mediaDirs);
        tempVector.clear();
        line = B1.readLine(); // skip to line after header
        while ( ( line != null) && ( !line.startsWith("[") ) )
        {
            tempVector.addElement(line);
            line = B1.readLine();
        }   
        mediaLibrary = new String[tempVector.size()];
        tempVector.copyInto(mediaLibrary);
        tempVector.clear(); 
        line = B1.readLine(); // skip to line after header
        while (line != null) 
        {
            tempVector.addElement(line);
            line = B1.readLine();
        }   
        mediaLibraryFiles = new String[tempVector.size()];
        tempVector.copyInto(mediaLibraryFiles);
        
        tempVector.clear();
        line = B2.readLine(); 
        line = B2.readLine();
        while ( line != null )
        {
            tempVector.addElement(line);
            line = B2.readLine();
        }
        history = new String[tempVector.size()];
        tempVector.copyInto(history);
    }

    public void close_file() throws IOException
    {
        B1.close();
        B2.close();
    }
    
    public String[] getHistory()
    {
        return history;
    }
    
    public String[] getMediaDirs()
    {
        return mediaDirs;
    }
    
    public String[] getMediaLibrary()
    {
        return mediaLibrary;
    }
    
    public String[] getMediaLibraryFiles()
    {
        return mediaLibraryFiles;
    } 
}