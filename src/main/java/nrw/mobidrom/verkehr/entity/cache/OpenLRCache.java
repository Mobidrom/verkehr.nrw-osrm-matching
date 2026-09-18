package nrw.mobidrom.verkehr.entity.cache;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRObject;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Calendar;

@Setter
@Getter
@Entity
@Table(schema = "cache", name = "openlr", uniqueConstraints = {
        @UniqueConstraint(name = "openlr_cache_uniq_base64_real_length", columnNames = {"openlr_base64", "real_length"})
})
public class OpenLRCache extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "openlr_base64", nullable = false)
    private String openlrBase64;

    @Column(name = "real_length", nullable = false)
    private int realLength;

    @Column(name = "last_decoded")
    private Calendar lastDecoded;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_result", columnDefinition = "jsonb")
    private OpenLRObject jsonResult;

}
