package nrw.mobidrom.verkehr.service.api.openlr.datex2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import openlr.map.FormOfWay;
import openlr.map.FunctionalRoadClass;

@Getter
public class OpenlrLineAttributes {
    @JsonProperty("openlrFunctionalRoadClass")
    private FunctionalRoadClass openlrFunctionalRoadClass;
    @JsonProperty("openlrFormOfWay")
    private FormOfWay openlrFormOfWay;
    @JsonProperty("openlrBearing")
    private int openlrBearing;
}
