/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package listfix.model;

import java.util.Hashtable;

/**
 *
 * @author jcaron
 */
public class AppOptions 
{
    private boolean savePlaylistsWithRelativePaths = false;
    private boolean autoLocateEntriesOnPlaylistLoad = false;
    private int maxPlaylistHistoryEntries = 5;
    public static final Hashtable<String,Integer> optionEnumTable = new Hashtable<String,Integer>();
    
    static
    {
        optionEnumTable.put("SAVE_RELATIVE_REFERENCES", AppOptionsEnum.SAVE_RELATIVE_REFERENCES);
        optionEnumTable.put("AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD", AppOptionsEnum.AUTO_FIND_ENTRIES_ON_PLAYLIST_LOAD);
        optionEnumTable.put("MAX_PLAYLIST_HISTORY_SIZE", AppOptionsEnum.MAX_PLAYLIST_HISTORY_SIZE);
    }
    
    public AppOptions()
    {
        // creates an AppOptions instance with the default settings.
    }
    
    public AppOptions(int maxPlaylistHistoryEntries, boolean autoLocateEntriesOnPlaylistLoad, boolean savePlaylistsWithRelativePaths)
    {
        this.autoLocateEntriesOnPlaylistLoad = autoLocateEntriesOnPlaylistLoad;
        this.maxPlaylistHistoryEntries = maxPlaylistHistoryEntries;
        this.savePlaylistsWithRelativePaths = savePlaylistsWithRelativePaths;
    }
    
    public boolean getAutoLocateEntriesOnPlaylistLoad()
    {
        return autoLocateEntriesOnPlaylistLoad;
    }

    public void setAutoLocateEntriesOnPlaylistLoad(boolean autoLocateEntriesOnPlaylistLoad)
    {
        this.autoLocateEntriesOnPlaylistLoad = autoLocateEntriesOnPlaylistLoad;
    }

    public int getMaxPlaylistHistoryEntries()
    {
        return maxPlaylistHistoryEntries;
    }

    public void setMaxPlaylistHistoryEntries(int maxPlaylistHistoryEntries)
    {
        this.maxPlaylistHistoryEntries = maxPlaylistHistoryEntries;
    }

    public boolean getSavePlaylistsWithRelativePaths()
    {
        return savePlaylistsWithRelativePaths;
    }

    public void setSavePlaylistsWithRelativePaths(boolean savePlaylistsWithRelativePaths)
    {
        this.savePlaylistsWithRelativePaths = savePlaylistsWithRelativePaths;
    }
}
