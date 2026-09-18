package nrw.mobidrom.verkehr.service.osrm.parser;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

@Setter
@Getter
public class OsrmMatching extends OsrmRoute {
    /**
     * JSON Attribute "confidence" in OSRM Response for Service "match".
     * Defined by OSRM-Spec.
     */
    public static final String CONFIDENCE = "confidence";

    private Double confidence;

    public OsrmMatching(JSONObject matching) throws JSONException {
        super(matching);
        setConfidence(matching.has(CONFIDENCE) ? matching.getDouble(CONFIDENCE) : null);

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("\n");
        sb.append(" - - Confidence : " + confidence);
        return sb.toString();
    }

}
