package nrw.mobidrom.verkehr.service.api.openlr.datex2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class OpenlrLastLocationReferencePoint {
    @JsonProperty("openlrCoordinate")
    private OpenlrCoordinate openlrCoordinate;
    @JsonProperty("openlrLineAttributes")
    private OpenlrLineAttributes openlrLineAttributes;
}
