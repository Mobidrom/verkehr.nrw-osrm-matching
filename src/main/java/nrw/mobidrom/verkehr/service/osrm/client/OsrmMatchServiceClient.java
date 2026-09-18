package nrw.mobidrom.verkehr.service.osrm.client;

import nrw.mobidrom.verkehr.service.osrm.OsrmOverviewType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Osrm Rest/JSON Client for Service "Match"
 *
 * @author stni2032
 */
public class OsrmMatchServiceClient extends AbstractOsrmRestApiClient {

    private boolean steps = false;
    private boolean annotations = false;
    private boolean gaps = true;
    private OsrmOverviewType overview = OsrmOverviewType.SIMPLIFIED;
    private List<Integer> timestamps = null;
    private List<Double> radiuses = null;
    private List<Integer> bearings = null;
    private List<Integer> bearingRanges = null;

    /**
     * @param routerHost host of OSRM Router
     * @param version    router version
     * @param profile    router profile
     */
    public OsrmMatchServiceClient(String routerHost, String version,
                                  String profile) {
        super(routerHost, version, profile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getService() {
        return "match";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> getQueryParams() {
        Map<String, String> ret = new HashMap<>();
        ret.put("steps", String.valueOf(steps));
        ret.put("annotations", String.valueOf(annotations));
        if (!gaps) {
            ret.put("gaps", "ignore");
        }
        if (overview != null) {
            ret.put("overview", overview.getCode());
        }
        if (timestamps != null) {
            ret.put("timestamps", print(timestamps));
        }
        if (radiuses != null) {
            ret.put("radiuses", print(radiuses));
        }
        if (bearings != null) {
            ret.put("bearings", print(bearings, bearingRanges));
        }
        return ret;
    }

    private String print(List<? extends Number> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Number number : list) {
            sb.append(sb.isEmpty() ? "" : ";").append(number);
        }
        return sb.toString();
    }

    private String print(List<Integer> list1, List<Integer> list2) {
        if (list1 == null || list1.isEmpty() || list2 == null || list2.isEmpty() || list1.size() != list2.size()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list1.size(); i++) {
            sb.append(sb.isEmpty() ? "" : ";").append(list1.get(i)).append(",").append(list2.get(i));
        }
        return sb.toString();
    }

    /**
     * @param steps the steps to set
     * @return this
     */
    public OsrmMatchServiceClient withSteps(boolean steps) {
        this.steps = steps;
        return this;
    }

    /**
     * @param annotations the annotations to set
     * @return this
     */
    public OsrmMatchServiceClient withAnnotations(boolean annotations) {
        this.annotations = annotations;
        return this;
    }

    /**
     * @param overview the overview to set
     * @return this
     */
    public OsrmMatchServiceClient withOverview(OsrmOverviewType overview) {
        this.overview = overview;
        return this;
    }

    /**
     * @param timestamps the timestamps to set
     * @return this
     */
    public OsrmMatchServiceClient withTimestamps(List<Integer> timestamps) {
        this.timestamps = timestamps;
        return this;
    }

    /**
     * @param radiuses the radiuses to set
     * @return this
     */
    public OsrmMatchServiceClient withRadiuses(List<Double> radiuses) {
        this.radiuses = radiuses;
        return this;
    }

    /**
     * @param gaps Allows the input track splitting based on huge timestamp gaps between points.
     * @return this
     */
    public OsrmMatchServiceClient withGaps(boolean gaps) {
        this.gaps = gaps;
        return this;
    }

    /**
     * @param bearings      Limits the search to segments with given bearing in degrees towards true north in clockwise direction.
     * @param bearingRanges allowed range, one range per bearing, in same order
     * @return this
     */
    public OsrmMatchServiceClient withBearing(List<Integer> bearings, List<Integer> bearingRanges) {
        this.bearings = bearings;
        this.bearingRanges = bearingRanges;
        return this;
    }
}
