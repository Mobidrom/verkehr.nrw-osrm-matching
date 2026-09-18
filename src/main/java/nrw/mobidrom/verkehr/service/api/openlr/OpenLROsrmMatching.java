package nrw.mobidrom.verkehr.service.api.openlr;

import lombok.Getter;
import net.postgis.jdbc.geometry.LineString;
import nrw.mobidrom.verkehr.service.osrm.OsrmOverviewType;
import nrw.mobidrom.verkehr.service.osrm.OsrmServiceException;
import nrw.mobidrom.verkehr.service.osrm.client.OsrmMatchServiceClient;
import nrw.mobidrom.verkehr.service.osrm.parser.OsrmLeg;
import nrw.mobidrom.verkehr.service.osrm.parser.OsrmMatch;
import nrw.mobidrom.verkehr.service.osrm.parser.OsrmMatching;
import nrw.mobidrom.verkehr.service.osrm.parser.OsrmRoute;
import openlr.LocationReferencePoint;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
public class OpenLROsrmMatching {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenLROsrmMatching.class);
    private static final double SPEED_M_PER_S = 7.;

    private final OpenLRMatchingParameter parameter;

    private final List<LocationReferencePoint> locationReferencePoints;

    private OsrmMatch routing;

    private boolean osrmConnectionError;

    public OpenLROsrmMatching(List<LocationReferencePoint> locationReferencePoints, OpenLRMatchingParameter parameter,
                              OsrmMatchServiceClient matchingClient) {
        this.locationReferencePoints = locationReferencePoints;
        this.parameter = parameter;
        osrmConnectionError = false;
        matchingClient.withAnnotations(true);
        matchingClient.withGaps(!parameter.isIgnoreGaps());
        matchingClient.withSteps(false);
        matchingClient.withOverview(OsrmOverviewType.FULL);
        matchingClient.withBearing(buildBearings(), buildBearingRanges());
        matchingClient.withRadiuses(buildRadiuses());
        matchingClient.withTimestamps(buildTimeStamps());
        JSONObject response;
        try {
            String geom = buildRoutingGeometry(locationReferencePoints);
            response = matchingClient.service(geom);
            if (response != null) {
                routing = new OsrmMatch(response);
            }
        } catch (OsrmServiceException e) {
            osrmConnectionError = true;
            LOGGER.trace(e.getMessage(), e);
        }
    }

    private boolean isValid() {
        if (routing == null) {
            return false;
        }
        if (routing.getMatchings() == null || routing.getMatchings().isEmpty()) {
            return false;
        }
        return routing.getMatchings().size() <= 1;
    }

    public OpenLRDecodingStatus validate() {
        if (osrmConnectionError) {
            return OpenLRDecodingStatus.OSRM_CONNECTION_ERROR;
        }
        if (routing == null) {
            return OpenLRDecodingStatus.OSRM_MATCHING_FAILED;
        }
        if (routing.getMatchings() == null || routing.getMatchings().isEmpty()) {
            return OpenLRDecodingStatus.OSRM_MATCHING_FAILED;
        }
        if (routing.getMatchings().size() > 1) {
            return OpenLRDecodingStatus.OSRM_RETURNED_MULTIPLE_MATCHINGS;
        }
        OsrmMatching first = routing.getMatchings().getFirst();
        if (first == null || first.getLegs() == null || first.getLegs().isEmpty()) {
            return OpenLRDecodingStatus.OSRM_MATCHING_FAILED;
        }
        Iterator<OsrmLeg> iterator = first.getLegs().iterator();
        OsrmLeg leg = iterator.next();
        long[] firsLegNodes = leg.getAnnotation().getNodes();
        long lastId = firsLegNodes[firsLegNodes.length - 1];
        long secondLastId = firsLegNodes[firsLegNodes.length - 2];
        while (iterator.hasNext()) {
            leg = iterator.next();
            long[] nodes = leg.getAnnotation().getNodes();
            if (nodes == null || nodes.length < 2) {
                return OpenLRDecodingStatus.OSRM_RETURNED_INVALID_LEG;
            }
            if (lastId != nodes[1] || secondLastId != nodes[0]) {
                if (lastId == nodes[0] && secondLastId == nodes[1]) {
                    return OpenLRDecodingStatus.OSRM_RETURNED_U_TURN;
                } else {
                    return OpenLRDecodingStatus.INVALID_NODE_ID_ORDER;
                }
            }
            lastId = nodes[nodes.length - 1];
            secondLastId = nodes[nodes.length - 2];
        }
        return OpenLRDecodingStatus.OK;
    }

    public LineString getGeometry() {
        if (!isValid()) {
            return null;
        }
        OsrmRoute osrmRoute = routing.getMatchings().getFirst();
        return (LineString) osrmRoute.getGeometry();
    }

    /**
     * build list of time stamps, representing pessimistic estimate of arrival at via-points
     *
     * @return list of time stamps
     */
    private List<Integer> buildTimeStamps() {
        List<Integer> go = null;
        if (parameter.isUseTimestampParameter()) {
            go = new ArrayList<>();
            int timestamp = 0;
            go.add(timestamp);
            for (LocationReferencePoint locationReferencePoint : locationReferencePoints) {
                if (!locationReferencePoint.isLastLRP()) {
                    timestamp += Math.max(1, (int) (locationReferencePoint.getDistanceToNext() / SPEED_M_PER_S));
                    go.add(timestamp);
                }
            }
        }
        return go;
    }

    /**
     * @return List of radiuses
     */
    private List<Double> buildRadiuses() {
        int startEnd = parameter.getStartEndRadius();
        int radius = parameter.getMaxRadius();
        final int defaultRadius = 5;
        if (radius <= 0 && startEnd <= 0) {
            return null;
        } else if (radius <= 0) {
            radius = defaultRadius;
        } else if (startEnd <= 0) {
            startEnd = defaultRadius;
        }
        List<Double> res = new ArrayList<>();
        for (int idx = 0; idx < locationReferencePoints.size(); idx++) {
            if (idx == 0) {
                res.add((double) startEnd);
            } else if (idx == locationReferencePoints.size() - 1) {
                res.add((double) startEnd);
            } else {
                res.add((double) radius);
            }
        }
        return res;
    }

    private String buildRoutingGeometry(List<LocationReferencePoint> locationReferencePoints) {
        StringBuilder sb = new StringBuilder();
        if (locationReferencePoints != null && !locationReferencePoints.isEmpty()) {
            Iterator<LocationReferencePoint> iterator = locationReferencePoints.iterator();
            LocationReferencePoint p = iterator.next();
            sb.append(p.getLongitudeDeg());
            sb.append(",");
            sb.append(p.getLatitudeDeg());
            while (iterator.hasNext()) {
                p = iterator.next();
                sb.append(";");
                sb.append(p.getLongitudeDeg());
                sb.append(",");
                sb.append(p.getLatitudeDeg());

            }
        }
        return sb.toString();
    }

    private List<Integer> buildBearings() {
        if (parameter.getBearingTolerance() <= 0) {
            return null;
        }
        if (locationReferencePoints == null || locationReferencePoints.isEmpty()) {
            return null;
        }
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < locationReferencePoints.size() - 1; i++) {
            res.add((int) locationReferencePoints.get(i).getBearing());
        }
        LocationReferencePoint p = locationReferencePoints.getLast();
        // angle in last locationReferencePoints is incoming angle, osrm
        // needs bearing of driving direction
        res.add(p.getBearing() > 180 ? (int) p.getBearing() - 180 : (int) p.getBearing() + 180);
        return res;
    }

    private List<Integer> buildBearingRanges() {
        if (parameter.getBearingTolerance() <= 0) {
            return null;
        }
        if (locationReferencePoints == null || locationReferencePoints.isEmpty()) {
            return null;
        }
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < locationReferencePoints.size() - 1; i++) {
            res.add(parameter.getBearingTolerance());
        }
        if (parameter.isIgnoreLastBearing()) {
            res.add(180);
        } else {
            res.add(parameter.getBearingTolerance());
        }
        return res;
    }

}
