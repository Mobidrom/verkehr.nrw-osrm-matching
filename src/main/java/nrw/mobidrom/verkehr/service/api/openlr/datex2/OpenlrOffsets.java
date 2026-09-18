package nrw.mobidrom.verkehr.service.api.openlr.datex2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class OpenlrOffsets {
    @JsonProperty("openlrPositiveOffset")
    private int openlrPositiveOffset;
    @JsonProperty("openlrNegativeOffset")
    private int openlrNegativeOffset;
}
