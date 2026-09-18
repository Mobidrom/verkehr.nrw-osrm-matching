package nrw.mobidrom.verkehr.helper.json;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.postgis.jdbc.geometry.Geometry;

import java.io.IOException;

public class GeometrySerializer extends JsonSerializer<Geometry> {

    @Override
    public void serialize(Geometry value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            gen.writeString(value.toString());
        } catch (Exception e) {
            throw new JsonGenerationException("Error serializing net.postgis.jdbc.geometry.Geometry: " + e.getMessage(), e, gen);
        }
    }
}
