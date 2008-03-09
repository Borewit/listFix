package listfix.controller;

import listfix.util.ArrayFunctions;
import listfix.io.ProcessFile;
import listfix.io.IniFileReader;
import listfix.io.FileWriter;
import listfix.model.*;
import listfix.tasks.*;
import listfix.comparators.*;
import listfix.exceptions.*;
import java.io.File;
import java.util.*;
import java.awt.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class GUIDriver
{    
    private static String[] mediaDir = null;
    private static final int HISTORY_LIMIT = 5;
    private static boolean showMediaDirWindow = false;
    private static String[] mediaLibraryDirectoryList = null;
    private static String[] mediaLibraryFileList = null;
    private static File currentPlaylist;
    private static Vector mp3s = new Vector();    
    private static Vector originalMP3s = new Vector();
    private static M3UHistory history = new M3UHistory(HISTORY_LIMIT);
    
    public GUIDriver()
    {
        try 
        {
            IniFileReader initReader = new IniFileReader();
            initReader.readIni();
            mediaDir = initReader.getMediaDirs();
            history.initHistory(initReader.getHistory());
            mediaLibraryDirectoryList = initReader.getMediaLibrary();
            mediaLibraryFileList = initReader.getMediaLibraryFiles();
            if (mediaDir.length == 0)
            {
                showMediaDirWindow = true;            
            }
        }
        catch (Exception e)
        {
            showMediaDirWindow = true;
            e.printStackTrace();
        }
    }
    
    public boolean isMP3ListEmpty()
    {
        if(mp3s.size() == 0)
        {
            return true;
        }
        return false;
    }
    
    public String[] getMediaDirs()
    {
        return mediaDir;
    }

    public void playPlaylist()
    {
        try
        {
            String cmdLine = "";
            String lowerCaseOpSysName = System.getProperty("os.name").toLowerCase();
            if (lowerCaseOpSysName.contains("windows") && lowerCaseOpSysName.contains("nt"))
            {
                cmdLine = "cmd.exe /c start ";
                cmdLine += "\"" + this.getPlaylist().getPath() + "\"";
            }
            else if (lowerCaseOpSysName.contains("windows") && lowerCaseOpSysName.contains("vista"))
            {
                cmdLine = "cmd.exe /c ";
                cmdLine += "\"" + this.getPlaylist().getPath() + "\"";
            }
            else if (lowerCaseOpSysName.contains("windows"))
            {
                cmdLine = "start ";
                cmdLine += "\"" + this.getPlaylist().getPath() + "\"";
            }
            else
            {
                cmdLine = "open ";
                cmdLine += this.getPlaylist().getPath();
            }
            Process p = Runtime.getRuntime().exec(cmdLine);
        }
        catch (IOException ex)
        {
            Logger.getLogger(MP3Object.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setMediaDirs(String[] value)
    {
        mediaDir = value;
    }
    
    public String[] getMediaLibraryDirectoryList()
    {
        return mediaLibraryDirectoryList;
    }
    
    public void setMediaLibraryDirectoryList(String[] value)
    {
        mediaLibraryDirectoryList = value;
    }
    
    public String[] getMediaLibraryFileList()
    {
        return mediaLibraryFileList;
    }
    
    public void setMediaLibraryFileList(String[] value)
    {
        mediaLibraryFileList = value;
    }
    
    public boolean getShowMediaDirWindow()
    {
        return showMediaDirWindow;
    }
    
    public M3UHistory getHistory()
    {
        return history;        
    }
    
    public Vector getMP3s()
    {
        return mp3s;
    }
    
    public void setMP3s(Vector value)
    {
        mp3s = value;
        originalMP3s.clear();
        for (int i = 0; i < value.size(); i++)
        {
            originalMP3s.add(((MP3Object)value.get(i)).clone());
        }
    }
    
    public File getPlaylist()
    {
        return currentPlaylist;
    }
    
    public String getPlaylistFilename()
    {
        if (currentPlaylist == null)
        {
            return "";
        }
        return currentPlaylist.getName();
    }
    
    public int getMP3Count()
    {
        return mp3s.size();
    }
    
    public int getLostMP3Count()
    {
        int result = 0;
        int size = this.getMP3Count();
        for (int i = 0; i < size; i++)
        {
            if ( !((MP3Object)mp3s.elementAt(i)).isFound() && (((MP3Object)mp3s.elementAt(i)).skipExistsCheck() || !((MP3Object)mp3s.elementAt(i)).exists()) )
            {
                result++;
            }
        }
        return result;
    }
    
    public void clearM3UHistory()
    {
        history.clearHistory();
        FileWriter.writeMruM3Us(history);
    }
    
    public Object[][] closePlaylist()
    {
        currentPlaylist = null;
        mp3s.clear();
        Object[][] result = new Object[0][3];
        return result;
    }
    
    public String[][] guiTableIni()
    {
        int size = mp3s.size();
        String[][] result = null;
        result = new String[size][3];
        for (int i = 0; i < size; i++)
        {
            result[i][0] = (i + 1) + ". " + ((MP3Object)mp3s.elementAt(i)).getFileName();
            result[i][1] = ((MP3Object)mp3s.elementAt(i)).getMessage();
            result[i][2] = ((MP3Object)mp3s.elementAt(i)).getPath();
        }
        return result;
    }
    
    private String[][] guiTableUpdate()
    {
        int size = mp3s.size();
        String[][] result = null;
        result = new String[size][3];
        for (int i = 0; i < size; i++)
        {
            result[i][0] = (i + 1) + ". " + ((MP3Object)mp3s.elementAt(i)).getFileName();
            result[i][1] = ((MP3Object)mp3s.elementAt(i)).getMessage();
            result[i][2] = ((MP3Object)mp3s.elementAt(i)).getPath();
        }
        return result;
    }
    
    public String[] getRecentM3Us()
    {
        return history.getM3UFilenames();
    }
    
    public String[] removeMediaDir(String dir) throws MediaDirNotFoundException
    {
        boolean found = false;
        int i = 0;
        int length = mediaDir.length;
        while (( i < length) && (found != true))
        {
            if(mediaDir[i].equals(dir))
            {
                mediaDir = ArrayFunctions.removeItem(mediaDir, i);
                found = true;
            }
            else
            {
                i++;
            }
        }
        if (found)
        {
            for (int j = 0; j < mediaLibraryDirectoryList.length; j++)
            {            
                if (mediaLibraryDirectoryList[j].startsWith(dir))
                {
                    mediaLibraryDirectoryList = ArrayFunctions.removeItem(mediaLibraryDirectoryList, j);
                    j--;
                }
            }
            for (int j = 0; j < mediaLibraryFileList.length; j++)
            {            
                if (mediaLibraryFileList[j].startsWith(dir))
                {
                    mediaLibraryFileList = ArrayFunctions.removeItem(mediaLibraryFileList, j);
                    j--;
                }
            }
            java.util.Arrays.sort(mediaDir);
            java.util.Arrays.sort(mediaLibraryDirectoryList);
            java.util.Arrays.sort(mediaLibraryFileList);
            FileWriter.writeIni(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList);
        }
        else
        {
            throw new MediaDirNotFoundException(dir + " was not in the media directory list.");
        }
        return mediaDir;
    }
    
    public String[][] moveUp(int mp3Index)
    {
        MP3Object temp = (MP3Object)mp3s.elementAt(mp3Index);
        mp3s.removeElementAt(mp3Index);
        mp3s.insertElementAt(temp, mp3Index - 1);
        return guiTableUpdate();
    }
    
    public String[][] moveDown(int mp3Index)
    {
        MP3Object temp = (MP3Object)mp3s.elementAt(mp3Index);
        mp3s.removeElementAt(mp3Index);
        mp3s.insertElementAt(temp, mp3Index + 1);
        return guiTableUpdate();
    }
    
    public String[][] moveTo(int initialPos, int finalPos)
    {
        MP3Object temp = (MP3Object)mp3s.elementAt(initialPos);
        mp3s.removeElementAt(initialPos);
        mp3s.insertElementAt(temp, finalPos);
        return guiTableUpdate();
    }
    
    public String[][] delete(int mp3Index)
    {
        mp3s.removeElementAt(mp3Index);
        return guiTableUpdate();
    }
    
    public String[][] addMP3(File input)
    {
        MP3Object mp3ToInsert = new MP3Object(input);
        mp3s.add(mp3ToInsert);
        return guiTableUpdate();
    }
    
    public String[][] appendPlaylist(File input) throws java.io.IOException
    {
        // currentPlaylist = null;
        ProcessFile playlistProcessor = new ProcessFile(input);
        mp3s.addAll(playlistProcessor.readM3U());
        playlistProcessor.close_file();
        return guiTableIni();
    }
    
    public String[][] insertPlaylist(File input, int index) throws java.io.IOException
    {
        ProcessFile playlistProcessor = new ProcessFile(input);
        Vector temp = playlistProcessor.readM3U();
        while(temp.size() > 0)
        {
            Object x = temp.remove(temp.size() - 1);
            mp3s.insertElementAt(x, index+1);           
        }
        playlistProcessor.close_file();
        return guiTableIni();
    }
    
    public void setCurrentPlaylist(File input) throws java.io.FileNotFoundException, java.io.IOException
    {
        currentPlaylist = input;
    }      
    
    public String[][] locateMP3s(LocateMP3sTask input)
    {
        mp3s = input.locateMP3s();
        return guiTableUpdate();
    }
    
    public String[][] locateMP3(int mp3ID)
    {
        ((MP3Object)mp3s.elementAt(mp3ID)).findNewLocationFromFileList(mediaLibraryFileList);
        return guiTableUpdate();
    }
    
    public Vector findClosestMatches(LocateClosestMatchesTask input)
    {
        Vector result = input.locateClosestMatches();
        Collections.sort(result, new MatchedMP3ObjectComparator());
        return result;
    }
    
    public String[][] updateMP3At(int mp3ID, MP3Object newMp3)
    {
        mp3s.insertElementAt(newMp3, mp3ID);
        mp3s.removeElementAt(mp3ID + 1);
        return guiTableUpdate();
    }
    
    public String[][] randomizePlaylist()
    {
        Vector result = new Vector();
        while (mp3s.size() > 0)
        {
            int index = (int)(Math.random() * mp3s.size());
            result.add(mp3s.elementAt(index));
            mp3s.removeElementAt(index);
        }
        mp3s = result;
        return guiTableUpdate();
    }
    
    public String[][] reversePlaylist()
    {
        Stack temp = new Stack();
        while (!mp3s.isEmpty())
        {
            temp.push(mp3s.firstElement());
            mp3s.remove(0);
        }
        while (!temp.empty())
        {
            mp3s.add(temp.pop());
        }
        return guiTableUpdate();
    }
    
    public String[][] ascendingFilenameSort()
    {
        Collections.sort(mp3s, new AscendingFilenameComparator());
        return guiTableUpdate();
    }
    
    public String[][] descendingFilenameSort()
    {
        Collections.sort(mp3s, new DescendingFilenameComparator());
        return guiTableUpdate();
    }
    
    public String[][] ascendingStatusSort()
    {
        Collections.sort(mp3s, new AscendingStatusComparator());
        return guiTableUpdate();
    }
    
    public String[][] descendingStatusSort()
    {
        Collections.sort(mp3s, new DescendingStatusComparator());
        return guiTableUpdate();
    }
    
    public String[][] ascendingPathSort()
    {
        Collections.sort(mp3s, new AscendingPathComparator());
        return guiTableUpdate();
    }
    
    public String[][] descendingPathSort()
    {
        Collections.sort(mp3s, new DescendingPathComparator());
        return guiTableUpdate();
    }
    
    public String[][] removeMissing()
    {
        MP3Object tempMp3 = null;
        for (int i = 0; i < mp3s.size(); i++)
        {
            tempMp3 = (MP3Object) mp3s.elementAt(i);
            if (!tempMp3.exists())
            {
                mp3s.removeElementAt(i--);                
            }
        }  
        return guiTableUpdate();
    }
    
    public void copyMP3sToNewNewDirectory(CopyMP3sTask thisTask)
    {
        thisTask.run();
    }
    
    public String[][] updateFileName(int mp3ID, String filename)
    {
        ((MP3Object)mp3s.elementAt(mp3ID)).setFileName(filename);
        String[][] result = locateMP3(mp3ID);
        return result;
    }
    
    public String[][] removeDuplicates()
    {
        Vector sortingList = new Vector();
        int i = 0;
        while (!mp3s.isEmpty())
        {
            sortingList.add(new MP3PositionObject((MP3Object)mp3s.firstElement(), i));
            mp3s.removeElementAt(0); i++;
        }
        // sort by fileName
        Collections.sort(sortingList, new MP3PositionObjectFileNameComparator());
        // remove dups
        for (int j = 0; j < sortingList.size() - 1; j++)
        {
            MP3Object temp1 = ((MP3PositionObject)sortingList.elementAt(j)).getMp3();
            MP3Object temp2 = ((MP3PositionObject)sortingList.elementAt(j+1)).getMp3();
            if (temp1.getFileName().equals(temp2.getFileName()))
            {
                sortingList.removeElementAt(j+1);
                j--;
            }
        }
        // sort by position
        Collections.sort(sortingList, new MP3PositionObjectPositionComparator());
        // copy back
        while (!sortingList.isEmpty())
        {
            mp3s.add(((MP3PositionObject)sortingList.firstElement()).getMp3());
            sortingList.removeElementAt(0);
        }
        return guiTableUpdate();
    }
    
    public String getMP3FileName(int mp3ID)
    {
        return ((MP3Object)mp3s.elementAt(mp3ID)).getFileName();
    }
    
    public boolean playlistModified()
    {
        boolean result = false;
        if (originalMP3s.size() != mp3s.size())
        {
            result = true;
        }
        else
        {
            for (int i = 0; i< mp3s.size(); i++)
            {
                MP3Object mp3a = (MP3Object)mp3s.get(i);
                MP3Object mp3b = (MP3Object)originalMP3s.get(i);
                if ( !mp3a.getFile().getPath().equals(mp3b.getFile().getPath()) )
                {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    
    public void saveM3U()
    {
        if (currentPlaylist != null)
        {
            FileWriter.writeM3U(mp3s, currentPlaylist);
            originalMP3s.clear();
            for (int i = 0; i < mp3s.size(); i++)
            {
                originalMP3s.add(((MP3Object)mp3s.get(i)).clone());
            }
        }
    }
    
    public void saveM3U(File destination)
    {
        if (destination != null)
        {
            try
            {
                FileWriter.writeM3U(mp3s, destination);
                currentPlaylist = destination;
                originalMP3s.clear();
                for (int i = 0; i < mp3s.size(); i++)
                {
                    originalMP3s.add(((MP3Object)mp3s.get(i)).clone());
                }
                history.add(destination.getCanonicalPath());
                FileWriter.writeMruM3Us(history);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
    * @param mp3ToInsert
    * @param mp3Index
    * @return
    */
    public Object[][] insertMP3(File fileToInsert, int mp3Index)
    {
        MP3Object mp3ToInsert = new MP3Object(fileToInsert);
        mp3s.insertElementAt(mp3ToInsert, mp3Index+1);
        return guiTableIni();
    }
}