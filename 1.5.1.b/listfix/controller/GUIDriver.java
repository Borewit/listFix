package listfix.controller;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import listfix.comparators.*;
import listfix.exceptions.*;
import listfix.io.FileWriter;
import listfix.io.IniFileReader;
import listfix.io.M3UFileReader;
import listfix.model.*;
import listfix.tasks.*;
import listfix.util.ArrayFunctions;

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

    public int getURLCount()
    {
        int result = 0;
        int size = this.getEntryCount();
        for (int i = 0; i < size; i++)
        {
            PlaylistEntry tempEntry = (PlaylistEntry) entries.elementAt(i);
            if (tempEntry.isURL())
            {
                result++;
            }
        }
        return result;
    }

    public boolean isEntryListEmpty()
    {
        if (entries.size() == 0)
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
            Runtime.getRuntime().exec(cmdLine);
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
            originalEntries.add(((PlaylistEntry) value.get(i)).clone());
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
            PlaylistEntry tempEntry = (PlaylistEntry) entries.elementAt(i);
            if (!tempEntry.isFound() && !tempEntry.isURL() && (tempEntry.skipExistsCheck() || !tempEntry.exists()))
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

    public String[][] guiTableUpdate()
    {
        int size = entries.size();
        String[][] result = null;
        result = new String[size][3];
        for (int i = 0; i < size; i++)
        {
            PlaylistEntry tempEntry = (PlaylistEntry) entries.elementAt(i);
            if (!tempEntry.isURL())
            {
                result[i][0] = (i + 1) + ". " + tempEntry.getFileName();
                result[i][1] = tempEntry.getMessage();
                result[i][2] = tempEntry.getPath();
            }
            else
            {
                result[i][0] = (i + 1) + ". " + tempEntry.getURI().toString();
                result[i][1] = tempEntry.getMessage();
                result[i][2] = "";
            }
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
        if (mediaDir.length == 1)
        {
            while ((i < length) && (found != true))
            {
                if (mediaDir[i].equals(dir))
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
                mediaLibraryDirectoryList = new String[0];
                mediaLibraryFileList = new String[0];
                FileWriter.writeIni(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList);
            }
            else
            {
                throw new MediaDirNotFoundException(dir + " was not in the media directory list.");
            }
            return mediaDir;
        }
        else
        {
            while ((i < length) && (found != true))
            {
                if (mediaDir[i].equals(dir))
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
                Vector<String> mldVector = new Vector(Arrays.asList(mediaLibraryDirectoryList));
                Vector<String> toRemove = new Vector<String>();
                for (String toTest : mldVector)
                {
                    if (toTest.startsWith(dir))
                    {
                        toRemove.add(toTest);
                    }
                }
                mldVector.removeAll(toRemove);
                mediaLibraryDirectoryList = mldVector.toArray(new String[mldVector.size()]);
                mldVector = null;
                toRemove.removeAllElements();

                Vector<String> mlfVector = new Vector(Arrays.asList(mediaLibraryFileList));
                for (String toTest : mlfVector)
                {
                    if (toTest.startsWith(dir))
                    {
                        toRemove.add(toTest);
                    }
                }
                mlfVector.removeAll(toRemove);
                mediaLibraryFileList = mlfVector.toArray(new String[mlfVector.size()]);
                mlfVector = null;

                FileWriter.writeIni(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList);
            }
            else
            {
                throw new MediaDirNotFoundException(dir + " was not in the media directory list.");
            }
            return mediaDir;
        }
    }

    public String[][] moveUp(int entryIndex)
    {
        PlaylistEntry temp = (PlaylistEntry) entries.elementAt(entryIndex);
        entries.removeElementAt(entryIndex);
        entries.insertElementAt(temp, entryIndex - 1);
        return guiTableUpdate();
    }

    public String[][] moveDown(int entryIndex)
    {
        PlaylistEntry temp = (PlaylistEntry) entries.elementAt(entryIndex);
        entries.removeElementAt(entryIndex);
        entries.insertElementAt(temp, entryIndex + 1);
        return guiTableUpdate();
    }

    public String[][] moveTo(int initialPos, int finalPos)
    {
        PlaylistEntry temp = (PlaylistEntry) entries.elementAt(initialPos);
        entries.removeElementAt(initialPos);
        entries.insertElementAt(temp, finalPos);
        return guiTableUpdate();
    }

    public String[][] delete(int entryIndex)
    {
        entries.removeElementAt(entryIndex);
        return guiTableUpdate();
    }

    public String[][] addEntry(File input)
    {
        PlaylistEntry entryToInsert = new PlaylistEntry(input, "");
        entries.add(entryToInsert);
        return guiTableUpdate();
    }

    public String[][] appendPlaylist(File input) throws java.io.IOException
    {
        // currentPlaylist = null;
        M3UFileReader playlistProcessor = new M3UFileReader(input);
        entries.addAll(playlistProcessor.readM3U());
        playlistProcessor.closeFile();
        return guiTableUpdate();
    }

    public String[][] insertPlaylist(File input, int index) throws java.io.IOException
    {
        M3UFileReader playlistProcessor = new M3UFileReader(input);
        Vector temp = playlistProcessor.readM3U();
        while (temp.size() > 0)
        {
            Object x = temp.remove(temp.size() - 1);
            entries.insertElementAt(x, index + 1);
        }
        playlistProcessor.closeFile();
        return guiTableUpdate();
    }

    public void setCurrentPlaylist(File input) throws java.io.FileNotFoundException, java.io.IOException
    {
        currentPlaylist = input;
    }

    public String[][] locateFiles(LocateFilesTask input)
    {
        entries = input.locateFiles();
        return guiTableUpdate();
    }

    public String[][] locateFile(int entryIndex)
    {
        ((PlaylistEntry) entries.elementAt(entryIndex)).findNewLocationFromFileList(mediaLibraryFileList);
        return guiTableUpdate();
    }

    public Vector findClosestMatches(LocateClosestMatchesTask input)
    {
        Vector result = input.locateClosestMatches();
        Collections.sort(result, new MatchedPlaylistEntryComparator());
        return result;
    }

    public String[][] updateEntryAt(int entryIndex, PlaylistEntry newEntry)
    {
        entries.insertElementAt(newEntry, entryIndex);
        entries.removeElementAt(entryIndex + 1);
        return guiTableUpdate();
    }

    public String[][] randomizePlaylist()
    {
        Vector result = new Vector();
        while (entries.size() > 0)
        {
            int index = (int) (Math.random() * entries.size());
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
        PlaylistEntry tempEntry = null;
        for (int i = 0; i < entries.size(); i++)
        {
            tempEntry = (PlaylistEntry) entries.elementAt(i);
            if (!tempEntry.isURL() && !tempEntry.exists())
            {
                entries.removeElementAt(i--);
            }
        }
        return guiTableUpdate();
    }

    public void copyFilesToNewNewDirectory(CopyFilesTask thisTask)
    {
        thisTask.run();
    }

    public String[][] updateFileName(int entryIndex, String filename)
    {
        ((PlaylistEntry) entries.elementAt(entryIndex)).setFileName(filename);
        String[][] result = locateFile(entryIndex);
        return result;
    }

    public String[][] removeDuplicates()
    {
        Vector sortingList = new Vector();
        int i = 0;
        while (!entries.isEmpty())
        {
            sortingList.add(new PlaylistEntryPosition((PlaylistEntry) entries.firstElement(), i));
            entries.removeElementAt(0);
            i++;
        }
        // sort by fileName
        Collections.sort(sortingList, new PlaylistEntryPositionFileNameComparator());
        // remove dups
        for (int j = 0; j < sortingList.size() - 1; j++)
        {
            PlaylistEntry temp1 = ((PlaylistEntryPosition) sortingList.elementAt(j)).getPlaylistEntry();
            PlaylistEntry temp2 = ((PlaylistEntryPosition) sortingList.elementAt(j + 1)).getPlaylistEntry();
            if (temp1.getFileName().equals(temp2.getFileName()))
            {
                sortingList.removeElementAt(j + 1);
                j--;
            }
        }
        // sort by position
        Collections.sort(sortingList, new PlaylistEntryPositionIndexComparator());
        // copy back
        while (!sortingList.isEmpty())
        {
            entries.add(((PlaylistEntryPosition) sortingList.firstElement()).getPlaylistEntry());
            sortingList.removeElementAt(0);
        }
        return guiTableUpdate();
    }

    public String getEntryFileName(int entryIndex)
    {
        return ((PlaylistEntry) entries.elementAt(entryIndex)).getFileName();
    }

    public PlaylistEntry getEntryAt(int entryIndex)
    {
        return ((PlaylistEntry) entries.elementAt(entryIndex));
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
            for (int i = 0; i < entries.size(); i++)
            {
                PlaylistEntry entryA = (PlaylistEntry) entries.get(i);
                PlaylistEntry entryB = (PlaylistEntry) originalEntries.get(i);
                if ((entryA.isURL() && entryB.isURL()) || (!entryA.isURL() && !entryB.isURL()))
                {
                    if (!entryA.isURL())
                    {
                        if (!entryA.getFile().getPath().equals(entryB.getFile().getPath()))
                        {
                            result = true;
                            break;
                        }
                    }
                    else
                    {
                        if (!entryA.getURI().toString().equals(entryB.getURI().toString()))
                        {
                            result = true;
                            break;
                        }
                    }
                }
                else
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
                originalEntries.add(((PlaylistEntry) entries.get(i)).clone());
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
                    originalEntries.add(((PlaylistEntry) entries.get(i)).clone());
                }
                history.add(destination.getCanonicalPath());
                FileWriter.writeMruM3Us(history);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param entryToInsert
     * @param entryIndex
     * @return
     */
    public Object[][] insertFile(File fileToInsert, int entryIndex)
    {
        PlaylistEntry entryToInsert = new PlaylistEntry(fileToInsert, "");
        entries.insertElementAt(entryToInsert, entryIndex + 1);
        return guiTableUpdate();
    }
    
    /**
     * @param entryToInsert
     * @param entryIndex
     * @return
     */
    public Object[][] replaceFile(File fileToInsert, int entryIndex)
    {
        PlaylistEntry entryToInsert = new PlaylistEntry(fileToInsert, "");
        entries.remove(entryIndex);
        entries.insertElementAt(entryToInsert, entryIndex);
        return guiTableUpdate();
    }
}
