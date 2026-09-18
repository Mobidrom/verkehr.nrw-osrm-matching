package nrw.mobidrom.verkehr.helper.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import net.postgis.jdbc.geometry.Geometry;
import net.postgis.jdbc.geometry.GeometryBuilder;

import java.io.IOException;

public class GeometryDeserializer extends JsonDeserializer<Geometry> {
    @Override
    public Geometry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        try {
            return GeometryBuilder.geomFromString(p.getValueAsString());
        } catch (Exception e) {
            throw new JsonParseException(p, "Error deserializing net.postgis.jdbc.geometry.Geometry: " + e.getMessage(), e);
        }
    }
}
