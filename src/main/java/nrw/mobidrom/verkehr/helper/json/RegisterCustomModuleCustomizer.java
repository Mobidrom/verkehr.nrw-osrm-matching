package nrw.mobidrom.verkehr.helper.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import net.postgis.jdbc.geometry.Geometry;

@Singleton
public class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {

    public void customize(ObjectMapper mapper) {
        mapper.registerModule(new JavaTimeModule());
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Geometry.class, new GeometryDeserializer());
        module.addSerializer(Geometry.class, new GeometrySerializer());
        mapper.registerModule(module);
    }
}
