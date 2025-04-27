package listfix.config;

import java.io.IOException;
import listfix.json.JsonAppOptions;

public class ApplicationOptionsConfiguration extends JsonConfigFile<IAppOptions> {
  /** The path to the options JSON file. */
  public ApplicationOptionsConfiguration() {
    super("options.json");
  }

  @Override
  public void read() throws IOException {
    this.jsonPojo = readJson(this.jsonFile, JsonAppOptions.class);
  }

  @Override
  public void initPojo() {
    this.jsonPojo = new JsonAppOptions();
  }

  public static ApplicationOptionsConfiguration load() throws IOException {
    ApplicationOptionsConfiguration config = new ApplicationOptionsConfiguration();
    config.init();
    return config;
  }
}
