package listfix.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public abstract class JsonConfigFile<T>
{
  public static final Logger _logger = Logger.getLogger(JsonConfigFile.class);

  protected final File jsonFile;
  protected T jsonPojo = null;

  public JsonConfigFile(String filename)
  {
    this.jsonFile = new File(filename);
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
    return mapper.reader().readValue(jsonFile, valueType);
  }

  public void write() throws IOException
  {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT); // pretty JSON
    mapper.writer().writeValue(this.jsonFile, this.jsonPojo);
  }

  public boolean existAndIsValid()
  {
    if (this.jsonFile.exists())
    {
      try
      {
        this.read();
        return true;
      }
      catch (IOException ioException)
      {
        return false;
      }
    }
    return true;
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
