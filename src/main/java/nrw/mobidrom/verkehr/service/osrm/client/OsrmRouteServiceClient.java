package nrw.mobidrom.verkehr.service.osrm.client;

import nrw.mobidrom.verkehr.service.osrm.OsrmContinueStraightType;
import nrw.mobidrom.verkehr.service.osrm.OsrmOverviewType;

import java.util.HashMap;
import java.util.Map;

/**
 * Osrm Rest/JSON Client for Service "Route"
 *
 * @author stni2032
 */
public class OsrmRouteServiceClient extends AbstractOsrmRestApiClient {

    private boolean alternatives = false;
    private boolean steps = false;
    private boolean annotations = false;
    private OsrmOverviewType overview = OsrmOverviewType.SIMPLIFIED;
    private OsrmContinueStraightType continueStraight = OsrmContinueStraightType.DEFAULT;

    // private final String geometries = "geojson";

    /**
     * @param routerHost host of OSRM Router
     * @param version    router version
     * @param profile    router profile
     */
    public OsrmRouteServiceClient(String routerHost, String version,
                                  String profile) {
        super(routerHost, version, profile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getService() {
        return "route";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> getQueryParams() {
        Map<String, String> ret = new HashMap<>();
        ret.put("alternatives", String.valueOf(alternatives));
        ret.put("steps", String.valueOf(steps));
        ret.put("annotations", String.valueOf(annotations));
        if (overview != null) {
            ret.put("overview", overview.getCode());
        }
        if (continueStraight != null) {
            ret.put("continue_straight", continueStraight.getCode());
        }
        return ret;
    }

    /**
     * @param alternatives Is the alternatives to set.
     * @return this
     */
    public OsrmRouteServiceClient withAlternatives(boolean alternatives) {
        this.alternatives = alternatives;
        return this;
    }

    /**
     * @param steps Is the steps to set.
     * @return this
     */
    public OsrmRouteServiceClient withSteps(boolean steps) {
        this.steps = steps;
        return this;
    }

    /**
     * @param annotations Is the annotations to set.
     * @return this
     */
    public OsrmRouteServiceClient withAnnotations(boolean annotations) {
        this.annotations = annotations;
        return this;
    }

    /**
     * @param overview Is the overview to set.
     * @return this
     */
    public OsrmRouteServiceClient withOverview(OsrmOverviewType overview) {
        this.overview = overview;
        return this;
    }

    /**
     * @param continueStraight Is the continueStraight to set.
     * @return this
     */
    public OsrmRouteServiceClient withContinueStraight(OsrmContinueStraightType continueStraight) {
        this.continueStraight = continueStraight;
        return this;
    }

}
