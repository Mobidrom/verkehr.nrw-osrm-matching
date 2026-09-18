package nrw.mobidrom.verkehr.service.osrm.parser;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OsrmMatch {
    /**
     * JSON Attribute "routes" in OSRM Response for Service "route".
     * Defined by OSRM-Spec.
     */
    public static final String CODE = "code";
    public static final String MATCHINGS = "matchings";
    public static final String TRACEPOINTS = "tracepoints";

    private static final Logger LOGGER = LoggerFactory.getLogger(OsrmMatch.class);

    private String code;

    private List<OsrmTracePoint> tracePoints;

    private List<OsrmMatching> matchings;

    public OsrmMatch(JSONObject routingResponse) {
        setTracePoints(new ArrayList<OsrmTracePoint>());
        setMatchings(new ArrayList<OsrmMatching>());
        try {
            if (routingResponse.has(CODE)) {
                setCode(routingResponse.getString(CODE));
                if ("NoMatch".equals(getCode())) {
                    return;
                }
            }
            // check response.tracepoints
            if (!routingResponse.has(TRACEPOINTS)) {
                // LOGGER.warn("Routing Response has not \"tracepoints\" attribute");
                return;
            }
            JSONArray tracePointArray = routingResponse.getJSONArray(TRACEPOINTS);
            if (tracePointArray.isEmpty()) {
                LOGGER.warn("Routing Response has empty \"tracepoints\" attribute");
                return;
            }
            for (int i = 0; i < tracePointArray.length(); i++) {
                getTracePoints().add(tracePointArray.isNull(i) ? null : new OsrmTracePoint(tracePointArray.getJSONObject(i)));
            }

            // check response.routes/.matchings
            if (!routingResponse.has(MATCHINGS)) {
                LOGGER.warn("Routing Response has not \"matching\" attribute");
                return;
            }
            JSONArray matchingArray = routingResponse.getJSONArray(MATCHINGS);
            if (matchingArray.isEmpty()) {
                LOGGER.warn("Routing Response has empty \"matching\" attribute");
                return;
            }
            for (int i = 0; i < matchingArray.length(); i++) {
                getMatchings().add(new OsrmMatching(matchingArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Code: " + getCode() + "\n");
        sb.append("Matchings: \n");
        for (OsrmMatching matching : getMatchings()) {
            sb.append(matching);
            sb.append("\n");
        }
        sb.append("Traceponts: \n");
        for (OsrmTracePoint tracePoint : getTracePoints()) {
            sb.append(tracePoint);
            sb.append("\n");
        }
        return sb.toString();
    }

}
