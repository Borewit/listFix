package listfix.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.awt.*;
import java.io.IOException;
import listfix.view.support.FontExtensions;

public class JsonFontSerializer extends JsonSerializer<Font> {
  @Override
  public void serialize(Font value, JsonGenerator jgen, SerializerProvider serializers)
      throws IOException {
    jgen.writeString(FontExtensions.serialize(value));
  }
}
