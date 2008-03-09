package listfix.model;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import listfix.util.*;

/*
============================================================================
= Author:   Jeremy Caron
= File:     MP3Object.java
= Version:  1.0
= Purpose:  To encapsulate all of the information about an MP3 file.
=           Provides methods for setting and getting the path to the
=           file, determining if the file exists, searching for
=           the file in a list of directories, and finding a closest
=           match to the file name.
============================================================================
*/

import java.io.File;

public class MP3Object
{    
    private final static String fs = System.getProperty("file.separator");    
    private final static String br = System.getProperty("line.separator");
    public static Vector emptyDriveRoots = new Vector();
    
    private String path = "";
    private String extInf = "";
    private String fileName = "";
    private File thisFile = null;
    private String message = "Unknown";
    private boolean found = false;
    
    public MP3Object(String p, String f, String extra)
    {
        path = p;
        fileName = f;
        extInf = extra;
        thisFile = new File(path, fileName);
        if (!skipExistsCheck() && this.exists())
        {
            message = "Found!";
            found = true;
        }
        else if (skipExistsCheck())
        {
            message = "Not Found";
        }
    }
    
    public MP3Object(File input)
    {
        fileName = input.getName();
        path = input.getPath().substring(0, input.getPath().indexOf(fileName));
        extInf = "";
        thisFile = input;
        if (!skipExistsCheck() && this.exists())
        {
            message = "Found!";
            found = true;
        }
        else if (skipExistsCheck())
        {
            message = "Not Found";
        }
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public File getFile()
    {
        return thisFile;
    }
    
    public String getFileName()
    {
        return fileName;
    }
    
    public String getExtInf()
    {
        return extInf;
    }
    
    public boolean exists()
    {
        return thisFile.exists();
    }

    public void play()
    {
        try
        {
            String cmdLine = "";
            String lowerCaseOpSysName = System.getProperty("os.name").toLowerCase();
            if (lowerCaseOpSysName.contains("windows") && lowerCaseOpSysName.contains("nt"))
            {
                cmdLine = "cmd.exe /c start ";
                cmdLine += "\"" + this.thisFile.getPath() + "\"";
            }
            else if (lowerCaseOpSysName.contains("windows") && lowerCaseOpSysName.contains("vista"))
            {
                cmdLine = "cmd.exe /c ";
                cmdLine += "\"" + this.thisFile.getPath() + "\"";
            }
            else if (lowerCaseOpSysName.contains("windows"))
            {
                cmdLine = "start ";
                cmdLine += "\"" + this.thisFile.getPath() + "\"";
            }
            else
            {
                cmdLine = "open ";                
                cmdLine += this.thisFile.getPath();
            }         
            Process p = Runtime.getRuntime().exec(cmdLine);
        }
        catch (IOException ex)
        {
            Logger.getLogger(MP3Object.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setPath(String input)
    {
        path = input;
        thisFile = new File(path, fileName);
    }
    
    public void setFileName(String input)
    {
        fileName = input;
        thisFile = new File(path, fileName);
    }
    
    public void setMessage(String x)
    {
        message = x;
    }
    
    private void setFile(File input)
    {
        thisFile = input;
        fileName = input.getName();
        path = input.getPath().substring(0, input.getPath().indexOf(fileName));
    }
    
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();        
        if (!(this.getExtInf() == null) && !(this.getExtInf().equals(""))) 
        {
            result.append(this.getExtInf());
            result.append(br);
        }
        if (this.getPath().endsWith(fs)) 
        {
            result.append(this.getPath());
            result.append(this.getFileName());
        } 
        else 
        {
            result.append(this.getPath());
            result.append(fs);
            result.append(this.getFileName());
        }
        return result.toString();
    }
    
    public void findNewLocationFromFileList(String[] fileList)
    {
        int searchResult = -1;
        String trimmedFileName = fileName.trim();
        for (int i = 0; i < fileList.length; i++)
        {
            if (fileList[i].endsWith(trimmedFileName))
            {
                searchResult = i;
                break;
            }
        }
        if (searchResult >= 0)
        {
            File foundFile = new File(fileList[searchResult]);
            this.setFile(foundFile);
            this.setMessage("Found!");  
            found = true;
            return;
        }   
        this.setMessage("Not Found");
        found = false;        
    }
    
    public boolean skipExistsCheck()
    {
        String[] emptyPaths = new String[emptyDriveRoots.size()];
        emptyDriveRoots.copyInto(emptyPaths);
        return found == true || ArrayFunctions.ContainsStringWithPrefix(emptyPaths, path);        
    }
    
    public boolean isFound()
    {
        return found;
    }
    
    @Override
    public Object clone()
    {
        MP3Object result = new MP3Object(new File(this.getFile().getPath()));
        return result;
    }
}   