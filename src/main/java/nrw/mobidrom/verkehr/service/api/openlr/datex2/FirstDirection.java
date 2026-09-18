package nrw.mobidrom.verkehr.service.api.openlr.datex2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class FirstDirection {
    @JsonProperty("openlrLocationReferencePoint")
    private List<OpenlrLocationReferencePoint> openlrLocationReferencePoint;
    @JsonProperty("openlrLastLocationReferencePoint")
    private OpenlrLastLocationReferencePoint openlrLastLocationReferencePoint;
    @JsonProperty("openlrOffsets")
    private OpenlrOffsets openlrOffsets;
}
