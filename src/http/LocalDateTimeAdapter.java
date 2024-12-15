package http;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    @Override
    public JsonElement serialize(LocalDateTime dateTime, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(dateTime.toString());
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        return LocalDateTime.parse(json.getAsString());
    }
}
