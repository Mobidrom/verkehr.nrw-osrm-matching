package nrw.mobidrom.verkehr.service.api.openlr.datex2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import openlr.map.FunctionalRoadClass;

@Getter
public class OpenlrPathAttributes {
    @JsonProperty("openlrLowestFRCToNextLRPoint")
    private FunctionalRoadClass openlrLowestFRCToNextLRPoint;
    @JsonProperty("openlrDistanceToNextLRPoint")
    private int openlrDistanceToNextLRPoint;
}
