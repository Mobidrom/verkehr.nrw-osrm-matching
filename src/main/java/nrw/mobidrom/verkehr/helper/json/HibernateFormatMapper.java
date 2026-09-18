package nrw.mobidrom.verkehr.helper.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.Arc;
import io.quarkus.hibernate.orm.JsonFormat;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.FormatMapper;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;

/**
 * This class is used for newer java/quarkus versions where a clearer separation is needed between REST and Database
 * serialization of JSONs
 * <p>
 * This defaults back to use the already configured Jackson Json Serializer
 */
@JsonFormat
@PersistenceUnitExtension
public class HibernateFormatMapper implements FormatMapper {

    private final org.hibernate.type.format.FormatMapper delegate =
            new JacksonJsonFormatMapper(Arc.container().instance(ObjectMapper.class).get());

    public <T> T fromString(CharSequence charSequence, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        return delegate.fromString(charSequence, javaType, wrapperOptions);
    }

    public <T> String toString(T value, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        return delegate.toString(value, javaType, wrapperOptions);
    }

}
