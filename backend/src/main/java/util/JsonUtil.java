package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Utility class for Google Gson serialization and deserialization.
 * Configures type adapters for java.sql.Date, java.sql.Time, and java.sql.Timestamp.
 */
public class JsonUtil {

    private static final Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat("yyyy-MM-dd HH:mm:ss");

        // Adapter for java.sql.Date (yyyy-MM-dd)
        builder.registerTypeAdapter(Date.class, (JsonSerializer<Date>) (src, typeOfSrc, context) -> 
            new JsonPrimitive(src.toString()));
        builder.registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> 
            Date.valueOf(json.getAsString()));

        // Adapter for java.sql.Time (HH:mm:ss or HH:mm)
        builder.registerTypeAdapter(Time.class, (JsonSerializer<Time>) (src, typeOfSrc, context) -> 
            new JsonPrimitive(src.toString()));
        builder.registerTypeAdapter(Time.class, (JsonDeserializer<Time>) (json, typeOfT, context) -> {
            String timeStr = json.getAsString();
            if (timeStr.length() == 5) {
                timeStr += ":00"; // add seconds if missing (e.g., 09:30 -> 09:30:00)
            }
            return Time.valueOf(timeStr);
        });

        // Adapter for java.sql.Timestamp
        builder.registerTypeAdapter(Timestamp.class, (JsonSerializer<Timestamp>) (src, typeOfSrc, context) -> 
            new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(src)));
        builder.registerTypeAdapter(Timestamp.class, (JsonDeserializer<Timestamp>) (json, typeOfT, context) -> {
            try {
                return new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(json.getAsString()).getTime());
            } catch (Exception e) {
                return Timestamp.valueOf(json.getAsString());
            }
        });

        GSON = builder.create();
    }

    /**
     * Converts a Java object to its JSON string representation.
     */
    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    /**
     * Converts a JSON string to a typed Java object.
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    /**
     * Converts a JSON Reader (e.g. from HttpServletRequest.getReader()) to a typed Java object.
     */
    public static <T> T fromJson(java.io.Reader reader, Class<T> classOfT) {
        return GSON.fromJson(reader, classOfT);
    }

    public static Gson getGson() {
        return GSON;
    }
}
