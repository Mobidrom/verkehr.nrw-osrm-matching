package nrw.mobidrom.verkehr.service.api.openlr;

import lombok.Getter;
import lombok.Setter;

/**
 * Parameter set to create osrm match request
 *
 * @author anqu2052
 */
@Getter
@Setter
public class OpenLRMatchingParameter {

    // use timestamp parameter in osrm request
    private boolean useTimestampParameter = true;

    // tolerance for bearing in degrees (0 = disabled)
    private int bearingTolerance = 30;

    // ignore bearing for last segment
    private boolean ignoreLastBearing = false;

    // maximal distance between openLR positions and osrm positions (0 = disabled/use default)
    private int maxRadius = 0;

    // maximal distance between openLR positions and osrm positions but only for start and end point (0 = disabled/use default)
    private int startEndRadius = 0;

    // Use ignore gap parameter for osrm match request.
    // Does not Allow the input track splitting based on huge timestamp gaps
    // between points
    // Available for osrm 5.7 and above
    private boolean ignoreGaps = true;

    // use openlr profile of routing engines
    private boolean openlrRouter = true;

    /**
     * @param ignoreLastBearing
     * @param maxRadius
     * @param openlrRouter
     */
    public OpenLRMatchingParameter(boolean ignoreLastBearing, int maxRadius, int startEndRadius, boolean openlrRouter) {
        super();
        this.ignoreLastBearing = ignoreLastBearing;
        this.maxRadius = maxRadius;
        this.openlrRouter = openlrRouter;
        this.startEndRadius = startEndRadius;
    }
}
