package nrw.mobidrom.verkehr.service.osrm.parser;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

@Setter
@Getter
public class OsrmAnnotation {

    private static final String DISTANCE = "distance";
    private static final String DURATION = "duration";
    private static final String DATASOURCE = "datasource";
    private static final String NODES = "nodes";
    private static final String SPEED = "speed";

    private double[] distance;
    private double[] duration;
    private int[] datasource;
    private long[] nodes;
    private double[] speed;

    public OsrmAnnotation(JSONObject annotation) throws JSONException {
        if (annotation.has(DISTANCE)) {
            setDistance(ParsingHelper.getDoubleArray(annotation.getJSONArray(DISTANCE)));
        }
        if (annotation.has(DURATION)) {
            setDuration(ParsingHelper.getDoubleArray(annotation.getJSONArray(DURATION)));
        }
        if (annotation.has(DATASOURCE)) {
            setDatasource(ParsingHelper.getIntArray(annotation.getJSONArray(DATASOURCE)));
        }
        if (annotation.has(NODES)) {
            setNodes(ParsingHelper.getLongArray(annotation.getJSONArray(NODES)));
        }
        if (annotation.has(SPEED)) {
            setSpeed(ParsingHelper.getDoubleArray(annotation.getJSONArray(SPEED)));
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" - - - Annotation:");
        sb.append("\n");
        sb.append(" - - - - Distances : " + Arrays.toString(distance));
        sb.append("\n");
        sb.append(" - - - - Durations : " + Arrays.toString(duration));
        sb.append("\n");
        sb.append(" - - - - Datasources : " + Arrays.toString(datasource));
        sb.append("\n");
        sb.append(" - - - - Nodes : " + Arrays.toString(nodes));
        sb.append("\n");
        sb.append(" - - - - Speed : " + Arrays.toString(speed));
        return sb.toString();
    }
}
