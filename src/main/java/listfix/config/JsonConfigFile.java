package listfix.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static listfix.io.Constants.DATA_DIR;

public abstract class JsonConfigFile<T>
{
  public static final Logger _logger = LogManager.getLogger(JsonConfigFile.class);

  protected final File jsonFile;
  protected T jsonPojo = null;

  public JsonConfigFile(String configurationFileName)
  {
    this.jsonFile = DATA_DIR.resolve(configurationFileName).toFile();
  }

  public File getFile()
  {
    return this.jsonFile;
  }

  public T getConfig()
  {
    return this.jsonPojo;
  }

  public void setConfig(T jsonPojo)
  {
    this.jsonPojo = jsonPojo;
  }

  /**
   * Reads the POJO from disk
   *
   * @throws IOException
   */
  public abstract void read() throws IOException;

  /**
   * Initialize a default instance of the POJO
   *
   * @throws IOException
   */
  public abstract void initPojo();

  protected static <T> T readJson(File jsonFile, Class<T> valueType) throws IOException
  {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper.reader().readValue(jsonFile, valueType);
  }

  public void write() throws IOException
  {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT); // pretty JSON
    mapper.writer().writeValue(this.jsonFile, this.jsonPojo);
  }

  /**
   * Loads the configuration file disk.
   * If it does not exist, it will create an empty file.
   * If required it will also create the direct parent folder.
   *
   * @throws IOException
   */
  public void init() throws IOException
  {
    if (this.jsonFile.exists())
    {
      this.read();
    }
    else
    {
      File dataDir = this.jsonFile.getParentFile();
      if (!dataDir.exists())
      {
        dataDir.mkdir();
      }
      this.initPojo();
      this.write();
    }
  }
}
