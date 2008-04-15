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

package listfix.controller;

import java.io.*;
import java.util.*;
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
    private boolean showMediaDirWindow = false;  
    private String[] mediaDir = null;
    private String[] mediaLibraryDirectoryList = null;
    private String[] mediaLibraryFileList = null;
	private Playlist currentList = new Playlist(); 
    private AppOptions options = new AppOptions();
    private M3UHistory history = new M3UHistory(options.getMaxPlaylistHistoryEntries());


    public GUIDriver()
    {
        try
        {
            (new FileWriter()).writeDefaultIniFilesIfNeeded();
            IniFileReader initReader = new IniFileReader();
            initReader.readIni();
            mediaDir = initReader.getMediaDirs();
            if (mediaDir.length == 0)
            {
                showMediaDirWindow = true;
            }
            options = initReader.getAppOptions();
            history = new M3UHistory(options.getMaxPlaylistHistoryEntries());
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
	
	public Playlist getCurrentList()
	{
		return currentList;
	}

	public void setCurrentList(Playlist aCurrentList)
	{
		currentList = aCurrentList;
	}

    public AppOptions getAppOptions()
    {
        return options;
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
                cmdLine += "\"" + getCurrentList().getFile().getPath() + "\"";
            }
            else if (lowerCaseOpSysName.contains("windows") && lowerCaseOpSysName.contains("vista"))
            {
                cmdLine = "cmd.exe /c ";
                cmdLine += "\"" + getCurrentList().getFile().getPath() + "\"";
            }
            else if (lowerCaseOpSysName.contains("windows"))
            {
                cmdLine = "start ";
                cmdLine += "\"" + getCurrentList().getFile().getPath() + "\"";
            }
            else
            {
                cmdLine = "open ";
                cmdLine += getCurrentList().getFile().getPath();
            }
            Runtime.getRuntime().exec(cmdLine);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setAppOptions(AppOptions opts)
    {
        options = opts;
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

    public Vector<PlaylistEntry> getEntries()
    {
        return getCurrentList().getEntries();
    }

    public Playlist getPlaylist()
    {
        return getCurrentList();
    }

    public void clearM3UHistory()
    {
        history.clearHistory();
        (new FileWriter()).writeMruM3Us(history);
    }

    public Object[][] closePlaylist()
    {
        setCurrentList(new Playlist());
        Object[][] result = new Object[0][3];
        return result;
    }

    public String[][] guiTableUpdate()
    {
        int size = getCurrentList().getEntries().size();
        String[][] result = null;
        result = new String[size][3];
        for (int i = 0; i < size; i++)
        {
            PlaylistEntry tempEntry = getCurrentList().getEntries().elementAt(i);
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
                (new FileWriter()).writeIni(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList, options);
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
                Vector<String> mldVector = new Vector<String>(Arrays.asList(mediaLibraryDirectoryList));
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

                Vector<String> mlfVector = new Vector<String>(Arrays.asList(mediaLibraryFileList));
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

                (new FileWriter()).writeIni(mediaDir, mediaLibraryDirectoryList, mediaLibraryFileList, options);
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
		Vector<PlaylistEntry> entries = getCurrentList().getEntries();
        PlaylistEntry temp = entries.elementAt(entryIndex);
        entries.removeElementAt(entryIndex);
        entries.insertElementAt(temp, entryIndex - 1);
        return guiTableUpdate();
    }

    public String[][] moveDown(int entryIndex)
    {
		Vector<PlaylistEntry> entries = getCurrentList().getEntries();
        PlaylistEntry temp = entries.elementAt(entryIndex);
        entries.removeElementAt(entryIndex);
        entries.insertElementAt(temp, entryIndex + 1);
        return guiTableUpdate();
    }

    public String[][] moveTo(int initialPos, int finalPos)
    {
		Vector<PlaylistEntry> entries = getCurrentList().getEntries();
        PlaylistEntry temp = entries.elementAt(initialPos);
        entries.removeElementAt(initialPos);
        entries.insertElementAt(temp, finalPos);
        return guiTableUpdate();
    }

    public String[][] delete(int entryIndex)
    {
        getCurrentList().getEntries().removeElementAt(entryIndex);
        return guiTableUpdate();
    }

    public String[][] addEntry(File input)
    {
        PlaylistEntry entryToInsert = new PlaylistEntry(input, "");
        getCurrentList().getEntries().add(entryToInsert);
        return guiTableUpdate();
    }

    public String[][] appendPlaylist(File input) throws java.io.IOException
    {
        M3UFileReader playlistProcessor = new M3UFileReader(input);
        getCurrentList().getEntries().addAll(playlistProcessor.readM3U());
        playlistProcessor.closeFile();
        return guiTableUpdate();
    }

    public String[][] insertPlaylist(File input, int index) throws java.io.IOException
    {
        M3UFileReader playlistProcessor = new M3UFileReader(input);
        Vector<PlaylistEntry> temp = playlistProcessor.readM3U();
        while (temp.size() > 0)
        {
        	PlaylistEntry x = temp.remove(temp.size() - 1);
            getCurrentList().getEntries().insertElementAt(x, index + 1);
        }
        playlistProcessor.closeFile();
        return guiTableUpdate();
    }

    public void setPlaylistFile(File input) throws java.io.FileNotFoundException, java.io.IOException
    {
        getCurrentList().setFile(input);
    }

    public String[][] locateFiles(LocateFilesTask input)
    {
        getCurrentList().updateEntries(input.locateFiles());
        return guiTableUpdate();
    }

    public String[][] locateFile(int entryIndex)
    {
        getCurrentList().getEntries().elementAt(entryIndex).findNewLocationFromFileList(mediaLibraryFileList);
        return guiTableUpdate();
    }

    public Vector<MatchedPlaylistEntry> findClosestMatches(LocateClosestMatchesTask input)
    {
        Vector<MatchedPlaylistEntry> result = input.locateClosestMatches();
        Collections.sort(result, new MatchedPlaylistEntryComparator());
        return result;
    }

    public String[][] updateEntryAt(int entryIndex, PlaylistEntry newEntry)
    {
        getCurrentList().getEntries().insertElementAt(newEntry, entryIndex);
        getCurrentList().getEntries().removeElementAt(entryIndex + 1);
        return guiTableUpdate();
    }

    public String[][] randomizePlaylist()
    {
        Vector<PlaylistEntry> result = new Vector<PlaylistEntry>();
        while (getCurrentList().getEntries().size() > 0)
        {
            int index = (int) (Math.random() * getCurrentList().getEntries().size());
            result.add(getCurrentList().getEntries().elementAt(index));
            getCurrentList().getEntries().removeElementAt(index);
        }
        getCurrentList().updateEntries(result);
        return guiTableUpdate();
    }

    public String[][] reversePlaylist()
    {
        Stack<PlaylistEntry> temp = new Stack<PlaylistEntry>();
        while (!currentList.getEntries().isEmpty())
        {
            temp.push(getCurrentList().getEntries().firstElement());
            getCurrentList().getEntries().remove(0);
        }
        while (!temp.empty())
        {
            getCurrentList().getEntries().add(temp.pop());
        }
        return guiTableUpdate();
    }

    public String[][] ascendingFilenameSort()
    {
        Collections.sort(getCurrentList().getEntries(), new AscendingFilenameComparator());
        return guiTableUpdate();
    }

    public String[][] descendingFilenameSort()
    {
        Collections.sort(getCurrentList().getEntries(), new DescendingFilenameComparator());
        return guiTableUpdate();
    }

    public String[][] ascendingStatusSort()
    {
        Collections.sort(getCurrentList().getEntries(), new AscendingStatusComparator());
        return guiTableUpdate();
    }

    public String[][] descendingStatusSort()
    {
        Collections.sort(getCurrentList().getEntries(), new DescendingStatusComparator());
        return guiTableUpdate();
    }

    public String[][] ascendingPathSort()
    {
        Collections.sort(getCurrentList().getEntries(), new AscendingPathComparator());
        return guiTableUpdate();
    }

    public String[][] descendingPathSort()
    {
        Collections.sort(getCurrentList().getEntries(), new DescendingPathComparator());
        return guiTableUpdate();
    }

    public String[][] removeMissing()
    {
        PlaylistEntry tempEntry = null;
        for (int i = 0; i < getCurrentList().getEntries().size(); i++)
        {
            tempEntry = getCurrentList().getEntries().elementAt(i);
            if (!tempEntry.isURL() && !tempEntry.exists())
            {
                getCurrentList().getEntries().removeElementAt(i--);
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
        getCurrentList().getEntries().elementAt(entryIndex).setFileName(filename);
        String[][] result = locateFile(entryIndex);
        return result;
    }

    public String[][] removeDuplicates()
    {
        Vector<PlaylistEntryPosition> sortingList = new Vector<PlaylistEntryPosition>();
        int i = 0;
        while (!currentList.getEntries().isEmpty())
        {
            sortingList.add(new PlaylistEntryPosition(getCurrentList().getEntries().firstElement(), i));
            getCurrentList().getEntries().removeElementAt(0);
            i++;
        }
        // sort by fileName
        Collections.sort(sortingList, new PlaylistEntryPositionFileNameComparator());
        // remove dups
        for (int j = 0; j < sortingList.size() - 1; j++)
        {
            PlaylistEntry temp1 = sortingList.elementAt(j).getPlaylistEntry();
            PlaylistEntry temp2 = sortingList.elementAt(j + 1).getPlaylistEntry();
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
            getCurrentList().getEntries().add(sortingList.firstElement().getPlaylistEntry());
            sortingList.removeElementAt(0);
        }
        return guiTableUpdate();
    }

    public String getEntryFileName(int entryIndex)
    {
        return getCurrentList().getEntries().elementAt(entryIndex).getFileName();
    }

    public PlaylistEntry getEntryAt(int entryIndex)
    {
        return getCurrentList().getEntries().elementAt(entryIndex);
    }

    public void saveM3U()
    {
        if (getCurrentList().getFile() != null)
        {
            if (options.getSavePlaylistsWithRelativePaths())
			{
				getCurrentList().setEntries((new FileWriter()).writeRelativeM3U(getCurrentList().getEntries(), getCurrentList().getFile()));
			}
			else
			{
				getCurrentList().setEntries((new FileWriter()).writeM3U(getCurrentList().getEntries(), getCurrentList().getFile()));
			}
        }
    }

    public void saveM3U(File destination)
    {
        if (destination != null)
        {
            try
            {
				if (options.getSavePlaylistsWithRelativePaths())
				{
					getCurrentList().setEntries((new FileWriter()).writeRelativeM3U(getCurrentList().getEntries(), destination));
				}
				else
				{
					getCurrentList().setEntries((new FileWriter()).writeRelativeM3U(getCurrentList().getEntries(), destination));
				}
                getCurrentList().setFile(destination);
                history.add(destination.getCanonicalPath());
                (new FileWriter()).writeMruM3Us(history);
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
        getCurrentList().getEntries().insertElementAt(entryToInsert, entryIndex + 1);
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
        getCurrentList().getEntries().remove(entryIndex);
        getCurrentList().getEntries().insertElementAt(entryToInsert, entryIndex);
        return guiTableUpdate();
    }
}