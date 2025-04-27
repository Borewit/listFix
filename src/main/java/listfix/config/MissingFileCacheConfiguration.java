package listfix.config;

import java.io.IOException;
import listfix.json.JsonMissingFileCache;

public class MissingFileCacheConfiguration extends JsonConfigFile<JsonMissingFileCache> {

    private static final String CACHE_FILENAME = "missing_cache.json";

    public MissingFileCacheConfiguration() {
        super(CACHE_FILENAME);
    }

    @Override
    public void read() throws IOException {
        // If the file doesn't exist or fails to read, initPojo() will handle creating a
        // new empty cache
        this.jsonPojo = readJson(this.jsonFile, JsonMissingFileCache.class);
    }

    @Override
    public void initPojo() {
        this.jsonPojo = new JsonMissingFileCache();
    }

    /**
     * Helper method to add a fix and immediately persist the cache.
     * This ensures that fixes are saved as soon as they are made.
     *
     * @param originalPath  The path string as it appeared in the playlist when it
     *                      was missing.
     * @param correctedPath The new, correct path string found by the user or
     *                      automation.
     * @throws IOException if saving the cache file fails.
     */
    public void addFixAndPersist(String originalPath, String correctedPath) throws IOException {
        if (this.jsonPojo == null) {
            // Should not happen if init() was called, but handle defensively
            initPojo();
        }
        this.jsonPojo.addFix(originalPath, correctedPath);
        this.write(); // Persist immediately
        logger.debug("Added fix to cache and persisted: '{}' -> '{}'", originalPath, correctedPath);
    }

    /**
     * Loads the cache configuration from disk.
     * If the file doesn't exist, it initializes an empty cache.
     *
     * @return The loaded or initialized configuration.
     * @throws IOException if there's an error reading an existing file (but not if
     *                     it doesn't exist).
     */
    public static MissingFileCacheConfiguration load() throws IOException {
        MissingFileCacheConfiguration config = new MissingFileCacheConfiguration();
        config.init(); // init() handles reading or creating the file/pojo
        return config;
    }
}