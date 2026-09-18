package nrw.mobidrom.verkehr.service.osrm.parser;

import lombok.Getter;
import lombok.Setter;
import net.postgis.jdbc.geometry.Point;
import org.json.JSONException;
import org.json.JSONObject;

@Setter
@Getter
public class OsrmWayPoint {
    private static final String HINT = "hint";
    private static final String DISTANCE = "distance";
    private static final String NAME = "name";
    private static final String LOCATION = "location";

    private String hint;
    private Double distance;
    private String name;
    private Point location;

    public OsrmWayPoint(JSONObject wayPoint) throws JSONException {
        setHint((wayPoint.has(HINT) ? wayPoint.getString(HINT) : null));
        setDistance((wayPoint.has(DISTANCE) ? wayPoint.getDouble(DISTANCE) : null));
        setName((wayPoint.has(NAME) ? wayPoint.getString(NAME) : null));
        if (wayPoint.has(LOCATION)) {
            double[] l = ParsingHelper.getDoubleArray(wayPoint.getJSONArray(LOCATION));
            setLocation(new Point(l[0], l[1]));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" - - Waypoint:");
        sb.append("\n");
        sb.append(" - - - Hint : " + getHint());
        sb.append("\n");
        sb.append(" - - - distance : " + distance);
        sb.append("\n");
        sb.append(" - - - name : " + name);
        sb.append("\n");
        sb.append(" - - - location : " + location);
        sb.append("\n");
        return sb.toString();
    }

}
