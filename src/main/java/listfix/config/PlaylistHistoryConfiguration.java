package listfix.config;

import java.io.IOException;
import listfix.json.JsonPlayListHistory;

public class PlaylistHistoryConfiguration extends JsonConfigFile<JsonPlayListHistory> {

  public PlaylistHistoryConfiguration() {
    super("history.json");
  }

  @Override
  public void read() throws IOException {
    this.jsonPojo = readJson(this.jsonFile, JsonPlayListHistory.class);
  }

  @Override
  public void initPojo() {
    this.jsonPojo = new JsonPlayListHistory();
  }

  public static PlaylistHistoryConfiguration load() throws IOException {
    PlaylistHistoryConfiguration config = new PlaylistHistoryConfiguration();
    config.init();
    return config;
  }
}
