package nrw.mobidrom.verkehr.service.api.openlr.datex2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class OpenlrLocationReferencePoint {
    @JsonProperty("openlrCoordinate")
    private OpenlrCoordinate openlrCoordinate;
    @JsonProperty("openlrLineAttributes")
    private OpenlrLineAttributes openlrLineAttributes;
    @JsonProperty("openlrPathAttributes")
    private OpenlrPathAttributes openlrPathAttributes;
}
