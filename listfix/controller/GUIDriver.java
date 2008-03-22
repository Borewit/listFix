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
    private static Vector entries = new Vector();    
    private static Vector originalEntries = new Vector();
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
        if(entries.size() == 0)
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
            Logger.getLogger(PlaylistEntry.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public Vector getEntries()
    {
        return entries;
    }
    
    public void setEntries(Vector value)
    {
        entries = value;
        originalEntries.clear();
        for (int i = 0; i < value.size(); i++)
        {
            originalEntries.add(((PlaylistEntry)value.get(i)).clone());
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
    
    public int getEntryCount()
    {
        return entries.size();
    }
    
    public int getLostEntryCount()
    {
        int result = 0;
        int size = this.getEntryCount();
        for (int i = 0; i < size; i++)
        {
            if ( !((PlaylistEntry)entries.elementAt(i)).isFound() && (((PlaylistEntry)entries.elementAt(i)).skipExistsCheck() || !((PlaylistEntry)entries.elementAt(i)).exists()) )
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
        entries.clear();
        Object[][] result = new Object[0][3];
        return result;
    }
    
    public String[][] guiTableIni()
    {
        int size = entries.size();
        String[][] result = null;
        result = new String[size][3];
        for (int i = 0; i < size; i++)
        {
            result[i][0] = (i + 1) + ". " + ((PlaylistEntry)entries.elementAt(i)).getFileName();
            result[i][1] = ((PlaylistEntry)entries.elementAt(i)).getMessage();
            result[i][2] = ((PlaylistEntry)entries.elementAt(i)).getPath();
        }
        return result;
    }
    
    private String[][] guiTableUpdate()
    {
        int size = entries.size();
        String[][] result = null;
        result = new String[size][3];
        for (int i = 0; i < size; i++)
        {
            result[i][0] = (i + 1) + ". " + ((PlaylistEntry)entries.elementAt(i)).getFileName();
            result[i][1] = ((PlaylistEntry)entries.elementAt(i)).getMessage();
            result[i][2] = ((PlaylistEntry)entries.elementAt(i)).getPath();
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
        PlaylistEntry temp = (PlaylistEntry)entries.elementAt(mp3Index);
        entries.removeElementAt(mp3Index);
        entries.insertElementAt(temp, mp3Index - 1);
        return guiTableUpdate();
    }
    
    public String[][] moveDown(int mp3Index)
    {
        PlaylistEntry temp = (PlaylistEntry)entries.elementAt(mp3Index);
        entries.removeElementAt(mp3Index);
        entries.insertElementAt(temp, mp3Index + 1);
        return guiTableUpdate();
    }
    
    public String[][] moveTo(int initialPos, int finalPos)
    {
        PlaylistEntry temp = (PlaylistEntry)entries.elementAt(initialPos);
        entries.removeElementAt(initialPos);
        entries.insertElementAt(temp, finalPos);
        return guiTableUpdate();
    }
    
    public String[][] delete(int mp3Index)
    {
        entries.removeElementAt(mp3Index);
        return guiTableUpdate();
    }
    
    public String[][] addMP3(File input)
    {
        PlaylistEntry mp3ToInsert = new PlaylistEntry(input);
        entries.add(mp3ToInsert);
        return guiTableUpdate();
    }
    
    public String[][] appendPlaylist(File input) throws java.io.IOException
    {
        // currentPlaylist = null;
        ProcessFile playlistProcessor = new ProcessFile(input);
        entries.addAll(playlistProcessor.readM3U());
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
            entries.insertElementAt(x, index+1);           
        }
        playlistProcessor.close_file();
        return guiTableIni();
    }
    
    public void setCurrentPlaylist(File input) throws java.io.FileNotFoundException, java.io.IOException
    {
        currentPlaylist = input;
    }      
    
    public String[][] locateFiles(LocateFilesTask input)
    {
        entries = input.locateMP3s();
        return guiTableUpdate();
    }
    
    public String[][] locateMP3(int mp3ID)
    {
        ((PlaylistEntry)entries.elementAt(mp3ID)).findNewLocationFromFileList(mediaLibraryFileList);
        return guiTableUpdate();
    }
    
    public Vector findClosestMatches(LocateClosestMatchesTask input)
    {
        Vector result = input.locateClosestMatches();
        Collections.sort(result, new MatchedPlaylistEntryComparator());
        return result;
    }
    
    public String[][] updateEntryAt(int mp3ID, PlaylistEntry newMp3)
    {
        entries.insertElementAt(newMp3, mp3ID);
        entries.removeElementAt(mp3ID + 1);
        return guiTableUpdate();
    }
    
    public String[][] randomizePlaylist()
    {
        Vector result = new Vector();
        while (entries.size() > 0)
        {
            int index = (int)(Math.random() * entries.size());
            result.add(entries.elementAt(index));
            entries.removeElementAt(index);
        }
        entries = result;
        return guiTableUpdate();
    }
    
    public String[][] reversePlaylist()
    {
        Stack temp = new Stack();
        while (!entries.isEmpty())
        {
            temp.push(entries.firstElement());
            entries.remove(0);
        }
        while (!temp.empty())
        {
            entries.add(temp.pop());
        }
        return guiTableUpdate();
    }
    
    public String[][] ascendingFilenameSort()
    {
        Collections.sort(entries, new AscendingFilenameComparator());
        return guiTableUpdate();
    }
    
    public String[][] descendingFilenameSort()
    {
        Collections.sort(entries, new DescendingFilenameComparator());
        return guiTableUpdate();
    }
    
    public String[][] ascendingStatusSort()
    {
        Collections.sort(entries, new AscendingStatusComparator());
        return guiTableUpdate();
    }
    
    public String[][] descendingStatusSort()
    {
        Collections.sort(entries, new DescendingStatusComparator());
        return guiTableUpdate();
    }
    
    public String[][] ascendingPathSort()
    {
        Collections.sort(entries, new AscendingPathComparator());
        return guiTableUpdate();
    }
    
    public String[][] descendingPathSort()
    {
        Collections.sort(entries, new DescendingPathComparator());
        return guiTableUpdate();
    }
    
    public String[][] removeMissing()
    {
        PlaylistEntry tempMp3 = null;
        for (int i = 0; i < entries.size(); i++)
        {
            tempMp3 = (PlaylistEntry) entries.elementAt(i);
            if (!tempMp3.exists())
            {
                entries.removeElementAt(i--);                
            }
        }  
        return guiTableUpdate();
    }
    
    public void copyMP3sToNewNewDirectory(CopyFilesTask thisTask)
    {
        thisTask.run();
    }
    
    public String[][] updateFileName(int mp3ID, String filename)
    {
        ((PlaylistEntry)entries.elementAt(mp3ID)).setFileName(filename);
        String[][] result = locateMP3(mp3ID);
        return result;
    }
    
    public String[][] removeDuplicates()
    {
        Vector sortingList = new Vector();
        int i = 0;
        while (!entries.isEmpty())
        {
            sortingList.add(new PlaylistEntryPosition((PlaylistEntry)entries.firstElement(), i));
            entries.removeElementAt(0); i++;
        }
        // sort by fileName
        Collections.sort(sortingList, new PlaylistEntryPositionFileNameComparator());
        // remove dups
        for (int j = 0; j < sortingList.size() - 1; j++)
        {
            PlaylistEntry temp1 = ((PlaylistEntryPosition)sortingList.elementAt(j)).getMp3();
            PlaylistEntry temp2 = ((PlaylistEntryPosition)sortingList.elementAt(j+1)).getMp3();
            if (temp1.getFileName().equals(temp2.getFileName()))
            {
                sortingList.removeElementAt(j+1);
                j--;
            }
        }
        // sort by position
        Collections.sort(sortingList, new PlaylistEntryPositionIndexComparator());
        // copy back
        while (!sortingList.isEmpty())
        {
            entries.add(((PlaylistEntryPosition)sortingList.firstElement()).getMp3());
            sortingList.removeElementAt(0);
        }
        return guiTableUpdate();
    }
    
    public String getMP3FileName(int mp3ID)
    {
        return ((PlaylistEntry)entries.elementAt(mp3ID)).getFileName();
    }
    
    public boolean playlistModified()
    {
        boolean result = false;
        if (originalEntries.size() != entries.size())
        {
            result = true;
        }
        else
        {
            for (int i = 0; i< entries.size(); i++)
            {
                PlaylistEntry mp3a = (PlaylistEntry)entries.get(i);
                PlaylistEntry mp3b = (PlaylistEntry)originalEntries.get(i);
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
            FileWriter.writeM3U(entries, currentPlaylist);
            originalEntries.clear();
            for (int i = 0; i < entries.size(); i++)
            {
                originalEntries.add(((PlaylistEntry)entries.get(i)).clone());
            }
        }
    }
    
    public void saveM3U(File destination)
    {
        if (destination != null)
        {
            try
            {
                FileWriter.writeM3U(entries, destination);
                currentPlaylist = destination;
                originalEntries.clear();
                for (int i = 0; i < entries.size(); i++)
                {
                    originalEntries.add(((PlaylistEntry)entries.get(i)).clone());
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
        PlaylistEntry mp3ToInsert = new PlaylistEntry(fileToInsert);
        entries.insertElementAt(mp3ToInsert, mp3Index+1);
        return guiTableIni();
    }
}