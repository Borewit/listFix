package listfix.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.awt.*;
import java.io.IOException;
import listfix.view.support.FontExtensions;

public class JsonFontDeserializer extends JsonDeserializer<Font> {
  @Override
  public Font deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    return FontExtensions.deserialize(node.asText());
  }
}
