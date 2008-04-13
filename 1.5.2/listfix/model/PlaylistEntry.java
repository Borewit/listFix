/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2008 Jeremy Caron
 * 
 * This file is part of listFix().
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.model;

import java.io.File;
import java.net.URI;
import java.util.*;
import listfix.util.*;

public class PlaylistEntry
{    
    private final static String fs = System.getProperty("file.separator");    
    private final static String br = System.getProperty("line.separator");
    public static Vector<String> emptyDirectories = new Vector<String>();
    public static String basePath = "";
    
    private String path = "";
    private String extInf = "";
    private String fileName = "";
    private File thisFile = null;
    private File absoluteFile = null;

    private URI thisURI = null;
    private String message = "Unknown";
    private boolean found = false;

    public PlaylistEntry(URI uri, String extra)
    {
        thisURI = uri;
        extInf = extra;
        message = "URL";
    }
    
    public PlaylistEntry(String p, String f, String extra)
    {
        path = p;
        fileName = f;
        extInf = extra;
        thisFile = new File(path, fileName);
        if (skipExistsCheck())
        {
            message = "Not Found";
        }
        else if (this.exists())
        {
            message = "Found!";
            found = true;
            if (!thisFile.isAbsolute())
            {
                absoluteFile = thisFile;
            }
        }
        else
        {
            if (!thisFile.isAbsolute())
            {
                absoluteFile = new File(basePath, p + fs + f);
                if (absoluteFile.exists())
                {
                    message = "Found!";
                    found = true;
                }
                else
                {
                    message = "Not Found";
                }
            }
            else
            {
                message = "Not Found";
            }
        }
    }
    
    public PlaylistEntry(File input, String extra)
    {
        fileName = input.getName();
        path = input.getPath().substring(0, input.getPath().indexOf(fileName));
        extInf = extra;
        thisFile = input;
        if (skipExistsCheck())
        {
            message = "Not Found";
        }
        else if (this.exists())
        {
            message = "Found!";
            found = true;
            if (!thisFile.isAbsolute())
            {
                absoluteFile = thisFile;
            }
        }
        else
        {
            if (!thisFile.isAbsolute())
            {
                absoluteFile = new File(basePath, input.getPath());
                if (absoluteFile.exists())
                {
                    message = "Found!";
                    found = true;
                }
                else
                {
                    message = "Not Found";
                }
            }
            else
            {
                message = "Not Found";
            }
        }
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public String getPath()
    {
        return this.isURL() ? "" : path;
    }
    
    public File getFile()
    {
        return thisFile;
    }
    
    public String getFileName()
    {
        return this.isURL() ? thisURI.toString() : fileName;
    }
    
    public String getExtInf()
    {
        return extInf;
    }
    
    public boolean exists()
    {
        return found || thisFile.exists();
    }

    public URI getURI()
    {
        return thisURI;
    }

    public void play() throws Exception
    {
        String cmdLine = "";
        // try to figure out the OS so we can issue the correct command
        // TODO: Lots of debugging on different OSes to make sure this works.
        String lowerCaseOpSysName = System.getProperty("os.name").toLowerCase();
        if (lowerCaseOpSysName.contains("windows") && lowerCaseOpSysName.contains("nt"))
        {
            cmdLine = "cmd.exe /K start ";
            cmdLine += "\"" + (this.isURL() ? this.thisURI.toString() : (this.isRelative() ? this.absoluteFile.getAbsolutePath() : this.thisFile.getAbsolutePath()) ) + "\"";
        }
        else if (lowerCaseOpSysName.contains("windows") && lowerCaseOpSysName.contains("vista"))
        {
            cmdLine = "cmd.exe /K ";
            cmdLine += "\"" + (this.isURL() ? "start " + this.thisURI.toString() : (this.isRelative() ? this.absoluteFile.getAbsolutePath() : this.thisFile.getAbsolutePath())) + "\"";
        }
        else if (lowerCaseOpSysName.contains("windows"))
        {
            cmdLine = "start ";
            cmdLine += "\"" + (this.isURL() ? this.thisURI.toString() : (this.isRelative() ? this.absoluteFile.getAbsolutePath() : this.thisFile.getAbsolutePath())) + "\"";
        }
        else
        {
            cmdLine = "open ";
            cmdLine += (this.isURL() ? this.thisURI.toString() : (this.isRelative() ? this.absoluteFile.getAbsolutePath() : this.thisFile.getAbsolutePath()));
        }
        try
        {
            Runtime.getRuntime().exec(cmdLine);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
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
    
    public String toM3UString()
    {
        StringBuilder result = new StringBuilder();        
        if (!(this.getExtInf() == null) && !(this.getExtInf().equals(""))) 
        {
            result.append(this.getExtInf());
            result.append(br);
        }
        if (!this.isURL())
        {
            if (!this.isRelative())
            {
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
            }
            else
            {
                String tempPath = thisFile.getPath();
                if (tempPath.substring(0, tempPath.indexOf(fileName)).equals(fs))
                {
                    result.append(fileName);
                }
                else
                {
                    result.append(thisFile.getPath());
                }
            }
        }
        else
        {
            result.append(thisURI.toString());
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
        String[] emptyPaths = new String[emptyDirectories.size()];
        emptyDirectories.copyInto(emptyPaths);
        return found == true || this.isURL() || ArrayFunctions.ContainsStringWithPrefix(emptyPaths, path);        
    }
    
    public boolean isFound()
    {
        return found;
    }
    
    public boolean isURL()
    {
        return thisURI != null && thisFile == null;
    }
    
    public boolean isRelative()
    {
        return !this.isURL() && thisFile != null && !thisFile.isAbsolute();
    }
    
    @Override
    public Object clone()
    {
        PlaylistEntry result = null;
        if (!this.isURL())
        {
            result = new PlaylistEntry(new File(this.getFile().getPath()), this.getExtInf());
        }
        else
        {
            try
            {
                result = new PlaylistEntry(new URI(this.getURI().toString()), this.getExtInf());
            }
            catch (Exception e)
            {
                //eat the error for now.
                e.printStackTrace();
            }
        }            
        return result;
    }
	
	public File getAbsoluteFile()
	{
		return absoluteFile;
	}
}   