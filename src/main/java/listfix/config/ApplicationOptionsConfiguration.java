package listfix.config;

import listfix.json.JsonAppOptions;

import java.io.IOException;

import static listfix.io.Constants.DATA_DIR;

public class ApplicationOptionsConfiguration extends ApplicationConfigFile<JsonAppOptions>
{

  /**
   * The path to the options JSON file.
   */
  public static final String path_json_options = DATA_DIR + "options.json";

  public ApplicationOptionsConfiguration()
  {
    super(path_json_options);
  }

  @Override
  public void read() throws IOException
  {
    this.jsonPojo = readJson(this.jsonFile, JsonAppOptions.class);
  }

  @Override
  public void initPojo()
  {
    this.jsonPojo = new JsonAppOptions();
  }

  public static ApplicationOptionsConfiguration load() throws IOException
  {
    ApplicationOptionsConfiguration config = new ApplicationOptionsConfiguration();
    config.init();
    return config;
  }
}
