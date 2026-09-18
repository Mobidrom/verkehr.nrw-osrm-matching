package nrw.mobidrom.verkehr.service.osrm.parser;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

@Setter
@Getter
public class OsrmTracePoint extends OsrmWayPoint {
    private static final String MATCHINGS_INDEX = "matchings_index";
    private static final String WAYPOINT_INDEX = "waypoint_index";

    private Integer matchingsIndex;
    private Integer waypointIndex;

    public OsrmTracePoint(JSONObject tracePoint) throws JSONException {
        super(tracePoint);
        setMatchingsIndex(tracePoint.has(MATCHINGS_INDEX) ? tracePoint.getInt(MATCHINGS_INDEX) : null);
        setWaypointIndex(tracePoint.has(WAYPOINT_INDEX) ? tracePoint.getInt(WAYPOINT_INDEX) : null);

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(" - - - matchings Index : " + matchingsIndex);
        sb.append("\n");
        sb.append(" - - - waypoint Index : " + waypointIndex);
        sb.append("\n");
        return sb.toString();
    }

}
