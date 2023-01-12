package listfix.config;

import listfix.json.JsonPlayListHistory;

import java.io.IOException;

import static listfix.io.Constants.DATA_DIR;

/**
 * @author Borewit
 */
public class PlaylistHistoryConfiguration extends ApplicationConfigFile<JsonPlayListHistory>
{

  private static final String path_json_history = DATA_DIR + "history.json";

  public PlaylistHistoryConfiguration()
  {
    super(path_json_history);
  }

  @Override
  public void read() throws IOException
  {
    this.jsonPojo = readJson(this.jsonFile, JsonPlayListHistory.class);
  }

  @Override
  public void initPojo()
  {
    this.jsonPojo = new JsonPlayListHistory();
  }

  public static PlaylistHistoryConfiguration load() throws IOException
  {
    PlaylistHistoryConfiguration config = new PlaylistHistoryConfiguration();
    config.init();
    return config;
  }
}
