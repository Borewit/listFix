package listfix.io;

/*
============================================================================
= Author:   Jeremy Caron
= File:     FileWriter.java
= Purpose:  Provides static methods for writing out a playlist
=           to a file and writing out the ini files for this program.
============================================================================
 */
import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;

import listfix.model.AppOptions;
import listfix.model.M3UHistory;
import listfix.tasks.WriteIniFileTask;
import listfix.model.PlaylistEntry;

public class FileWriter
{
    private final static String br = System.getProperty("line.separator");
    private final static String fs = System.getProperty("file.separator");
    private final static String homeDir = System.getProperty("user.home");
    private static FileOutputStream outputStream;
    private static BufferedOutputStream output;

    public static String getRelativePath(File file, File relativeTo)
    {
        StringTokenizer fileTizer = new StringTokenizer(file.getAbsolutePath(), fs);
        StringTokenizer relativeToTizer = new StringTokenizer(relativeTo.getAbsolutePath(), fs);
        Vector<String> fileTokens = new Vector<String>();
        Vector<String> relativeToTokens = new Vector<String>();
        while (fileTizer.hasMoreTokens())
        {
            fileTokens.add(fileTizer.nextToken());
        }
        while (relativeToTizer.hasMoreTokens())
        {
            relativeToTokens.add(relativeToTizer.nextToken());
        }

        // throw away last token from each, don't need the file names for path calculation.
        String fileName = fileTokens.remove(fileTokens.size() - 1);
        relativeToTokens.removeElementAt(relativeToTokens.size() - 1);

        int maxSize = fileTokens.size() >= relativeToTokens.size() ? relativeToTokens.size() : fileTokens.size();
        boolean tokenMatch = false;
        for (int i = 0; i < maxSize; i++)
        {
            if (fileTokens.get(i).equals(relativeToTokens.get(i)))
            {
                tokenMatch = true;
                fileTokens.remove(i);
                relativeToTokens.remove(i);
                i--;
                maxSize--;
            }
            else if (tokenMatch == false)
            {
                // files can not be made relative to one another.
                return file.getAbsolutePath();
            }
        }

        StringBuffer resultBuffer = new StringBuffer();
        for (int i = 0; i < relativeToTokens.size(); i++)
        {
            resultBuffer.append("..").append(fs);
        }

        for (int i = 0; i < fileTokens.size(); i++)
        {
            resultBuffer.append(fileTokens.get(i)).append(fs);
        }

        resultBuffer.append(fileName);

        return resultBuffer.toString();
    }

    public static void writeDefaultIniFilesIfNeeded()
    {
        File testFile = new File(homeDir + fs + "dirLists.ini");
        if (!testFile.exists() || (testFile.exists() && testFile.length() == 0))
        {
            try
            {
                StringBuffer buffer = new StringBuffer();
                AppOptions options = new AppOptions();
                outputStream = new FileOutputStream(homeDir + fs + "dirLists.ini");
                output = new BufferedOutputStream(outputStream);
                buffer.append("[Media Directories]" + br);
                buffer.append("[Options]" + br);
                buffer.append("AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD=" + Boolean.toString(options.getAutoLocateEntriesOnPlaylistLoad()) + br);
                buffer.append("MAX_PLAYLIST_HISTORY_SIZE=" + options.getMaxPlaylistHistoryEntries() + br);
                buffer.append("SAVE_RELATIVE_REFERENCES=" + Boolean.toString(options.getSavePlaylistsWithRelativePaths()) + br);
                buffer.append("[Media Library Directories]" + br);
                buffer.append("[Media Library Files]" + br);
                output.write(buffer.toString().getBytes());
                output.close();
                outputStream.close();
            }
            catch (IOException e)
            {
                // eat the error and continue
                e.printStackTrace();
            }
        }

        testFile = new File(homeDir + fs + "listFixHistory.ini");
        if (!testFile.exists() || (testFile.exists() && testFile.length() == 0))
        {
            try
            {
                outputStream = new FileOutputStream(homeDir + fs + "listFixHistory.ini");
                output = new BufferedOutputStream(outputStream);
                output.write(new String("[Recent M3Us]" + br).getBytes());
                output.close();
                outputStream.close();
            }
            catch (IOException e)
            {
                // eat the error and continue
                e.printStackTrace();
            }
        }
    }

    public static Vector writeM3U(Vector entries, File fileName)
    {
        PlaylistEntry tempEntry = null;
        try
        {
            StringBuffer buffer = new StringBuffer();
            outputStream = new FileOutputStream(fileName);
            output = new BufferedOutputStream(outputStream);
            buffer.append("#EXTM3U" + br);
            for (int i = 0; i < entries.size(); i++)
            {
                tempEntry = (PlaylistEntry) entries.elementAt(i);
				if (tempEntry.isRelative())
				{
					tempEntry = new PlaylistEntry(tempEntry.getAbsoluteFile().getCanonicalFile(), tempEntry.getExtInf());
					buffer.append(tempEntry.toM3UString() + br);
					entries.remove(i);
					entries.add(i, tempEntry);
				}
				else
				{
					buffer.append(tempEntry.toM3UString() + br);
				}
            }
            output.write(buffer.toString().getBytes());
            output.close();
            outputStream.close();
			return entries;
        }
        catch (IOException e)
        {
            // eat the error and continue
            e.printStackTrace();
			return entries;
        }
    }

    public static Vector writeRelativeM3U(Vector entries, File fileName)
    {
        PlaylistEntry tempEntry = null;
        try
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append("#EXTM3U" + br);
            for (int i = 0; i < entries.size(); i++)
            {
                tempEntry = (PlaylistEntry) entries.elementAt(i);
				if (!tempEntry.isRelative())
				{
					if (!tempEntry.getExtInf().isEmpty())
					{
						buffer.append(tempEntry.getExtInf() + br);
					}
					String relPath = getRelativePath(tempEntry.getFile().getAbsoluteFile(), fileName);
					buffer.append(relPath + br);
					// replace the existing entry with a new relative one...
					entries.remove(i);
					entries.add(i, new PlaylistEntry(new File(relPath), tempEntry.getExtInf()));
				}
				else
				{
					buffer.append(tempEntry.toM3UString() + br);
				}
            }			
            outputStream = new FileOutputStream(fileName);
            output = new BufferedOutputStream(outputStream);
            output.write(buffer.toString().getBytes());			
            output.close();
            outputStream.close();
			return entries;
        }
        catch (IOException e)
        {
            // eat the error and continue
            e.printStackTrace();
			return entries;
        }
    }

    public static void writeMruM3Us(M3UHistory history)
    {
        try
        {
            outputStream = new FileOutputStream(homeDir + fs + "listFixHistory.ini");
            output = new BufferedOutputStream(outputStream);
            StringBuffer buffer = new StringBuffer();
            buffer.append("[Recent M3Us]" + br);
            String[] filenames = history.getM3UFilenames();
            for (int i = 0; i < filenames.length; i++)
            {
                buffer.append(filenames[i] + br);
            }
            output.write(buffer.toString().getBytes());
            output.close();
            outputStream.close();
        }
        catch (IOException e)
        {
            // eat the error and continue
            e.printStackTrace();
        }
    }

    public static void writeIni(String[] mediaDir, String[] mediaLibraryDirList, String[] mediaLibraryFileList, AppOptions options)
    {
        try
        {
            WriteIniFileTask thisTask = new WriteIniFileTask(mediaDir, mediaLibraryDirList, mediaLibraryFileList, options);
            thisTask.start();
        }
        catch (Exception e)
        {
            // eat the error and continue
            e.printStackTrace();
        }
    }
}
        