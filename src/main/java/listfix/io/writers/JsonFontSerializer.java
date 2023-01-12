package listfix.io.writers;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import listfix.view.support.FontExtensions;

import java.awt.*;
import java.io.IOException;

public class JsonFontSerializer extends JsonSerializer<Font>
{
  @Override
  public void serialize(Font value, JsonGenerator jgen, SerializerProvider serializers) throws IOException
  {
    jgen.writeString(FontExtensions.serialize(value));
  }
}
