package nrw.mobidrom.verkehr.service.osrm.parser;

import lombok.Getter;
import lombok.Setter;
import net.postgis.jdbc.geometry.Geometry;
import nrw.mobidrom.verkehr.service.osrm.OsrmPolylineUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class OsrmRoute {

    private static final String DISTANCE = "distance";
    private static final String DURATION = "duration";
    private static final String WEIGHT = "weight";
    private static final String WEIGHT_NAME = "weight_name";
    private static final String LEGS = "legs";
    private static final String GEOMETRY = "geometry";

    private Double distance;
    private Double duration;
    private Double weight;
    private String weightName;
    private Geometry geometry;
    private List<OsrmLeg> legs;

    public OsrmRoute(JSONObject route) throws JSONException {
        setDistance((route.has(DISTANCE) ? route.getDouble(DISTANCE) : null));
        setDuration((route.has(DURATION) ? route.getDouble(DURATION) : null));
        setWeight((route.has(WEIGHT) ? route.getDouble(WEIGHT) : null));
        setWeightName((route.has(WEIGHT_NAME) ? route.getString(WEIGHT_NAME) : ""));
        if (route.has(GEOMETRY)) {
            setGeometry((OsrmPolylineUtil.decodePoly(route.getString(GEOMETRY))));
        }
        setLegs(new ArrayList<OsrmLeg>());
        if (route.has(LEGS)) {
            JSONArray jsonArray = route.getJSONArray(LEGS);

            for (int i = 0; i < jsonArray.length(); i++) {
                getLegs().add(new OsrmLeg(jsonArray.getJSONObject(i)));
            }
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" - Route:");
        sb.append("\n");
        sb.append(" - - Distance : " + distance);
        sb.append("\n");
        sb.append(" - - Duration : " + duration);
        sb.append("\n");
        sb.append(" - - Weight : " + weight);
        sb.append("\n");
        sb.append(" - - WeightName : " + weightName);
        sb.append("\n");
        sb.append(" - - Geometry : " + geometry);
        sb.append("\n");
        for (OsrmLeg leg : legs) {
            sb.append(leg);
            sb.append("\n");
        }
        return sb.toString();
    }
}
