package nrw.mobidrom.verkehr.service.osrm.parser;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

@Setter
@Getter
public class OsrmLeg {
    private static final String DISTANCE = "distance";
    private static final String DURATION = "duration";
    private static final String WEIGHT = "weight";
    private static final String ANNOTATION = "annotation";

    private Double distance;
    private Double duration;
    private Double weight;
    private OsrmAnnotation annotation;

    public OsrmLeg(JSONObject leg) throws JSONException {
        setDistance((leg.has(DISTANCE) ? leg.getDouble(DISTANCE) : null));
        setDuration((leg.has(DURATION) ? leg.getDouble(DURATION) : null));
        setWeight((leg.has(WEIGHT) ? leg.getDouble(WEIGHT) : null));
        if (leg.has(ANNOTATION)) {
            setAnnotation(new OsrmAnnotation(leg.getJSONObject(ANNOTATION)));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" - - Leg:");
        sb.append("\n");
        sb.append(" - - - Distance : " + distance);
        sb.append("\n");
        sb.append(" - - - Duration : " + duration);
        sb.append("\n");
        sb.append(" - - - Weight : " + weight);
        sb.append("\n");
        if (annotation != null) {
            sb.append(annotation);
            sb.append("\n");
        }
        return sb.toString();
    }
}
