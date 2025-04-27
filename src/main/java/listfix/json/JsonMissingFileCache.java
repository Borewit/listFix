package listfix.json;

import java.util.Map;
import java.util.TreeMap;

/**
 * POJO for storing the persistent mapping of originally missing file paths
 * to their last known correct locations.
 */
public class JsonMissingFileCache {

    /** Map where Key = Original Missing Path, Value = Corrected Path */
    private final Map<String, String> fixedPaths = new TreeMap<>();

    public Map<String, String> getFixedPaths() {
        return fixedPaths;
    }

    /**
     * Retrieves the corrected path for a given original missing path.
     *
     * @param originalPath The path string as it appeared in the playlist when it
     *                     was missing.
     * @return The corrected path string if found in the cache, otherwise null.
     */
    public String getFixedPath(String originalPath) {
        return fixedPaths.get(originalPath);
    }

    /**
     * Adds or updates a mapping in the cache.
     *
     * @param originalPath  The path string as it appeared in the playlist when it
     *                      was missing.
     * @param correctedPath The new, correct path string found by the user or
     *                      automation.
     */
    public void addFix(String originalPath, String correctedPath) {
        fixedPaths.put(originalPath, correctedPath);
    }
}