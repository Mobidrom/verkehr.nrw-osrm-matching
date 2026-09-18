package nrw.mobidrom.verkehr.service.osrm;

import net.postgis.jdbc.geometry.LineString;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper Class for RoutingResponse
 *
 * @author stni2032
 */
public final class RoutingResponseUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoutingResponseUtil.class);

    /**
     * JSON Attribute "routes" in OSRM Response for Service "route".
     * Defined by OSRM-Spec.
     */
    public static final String ROUTES = "routes";
    public static final String WAYPOINTS = "waypoints";

    /**
     * JSON Attribute "matchings" in OSRM Response for Service "match".
     * Defined by OSRM-Spec.
     */
    public static final String MATCHINGS = "matchings";

    /**
     * JSON Attribute "legs" in "routes" or "matchings" Object.
     * Defined by OSRM-Spec.
     */
    public static final String LEGS = "legs";

    /**
     * JSON Attribute "annotation" in "log" Object.
     * Defined by OSRM-Spec.
     */
    public static final String ANNOTATION = "annotation";

    /**
     * JSON Attribute "nodes" in "annotation" Object.
     * Defined by OSRM-Spec.
     */
    public static final String NODES = "nodes";

    /**
     * JSON Attribute "geometry" in "routes" or "matchings" Object.
     * Defined by OSRM-Spec.
     */
    public static final String GEOMETRY = "geometry";

    /**
     * JSON Attribute for List of OSM IDs in OSRM Response Root Object.
     * Defined by VIP.NRW Code.
     */
    public static final String OSMIDS = "osmids";

    /**
     * JSON Attribute for List of Identifiers of Interferences in OSRM Response Root Object.
     * Defined by VIP.NRW Code.
     */
    public static final String INTERFERENCES = "interferences";

    /**
     * JSON Attribute for List of LOS Offset Data in OSRM Response Root Object.
     * Defined by VIP.NRW Code.
     */
    public static final String LOS_OFFSET_DATA = "losOffsetData";

    /**
     * JSON Attribute for LOS Value in LosOffsetData.
     * Defined by VIP.NRW Code.
     */
    public static final String LOS = "los";

    /**
     * JSON Attribute for Offset (n-th Point of Geometry) in LosOffsetData.
     * Defined by VIP.NRW Code.
     */
    public static final String OFFSET = "offset";

    /**
     * JSON Attribute for traveltime loss in OSRM Response Root Object.
     * Defined by VIP.NRW Code.
     */
    public static final String TRAVELTIME_LOSS = "traveltimeLoss";

    /**
     * Helper Class
     */
    private RoutingResponseUtil() {
    }

    /**
     * Method extracts Osm-Node IDs from first Route/Matching of OSRM Response
     *
     * @param routingResponse OSRM Response as JSON Object
     * @return List of OSM Node IDs
     */
    public static List<Long> getNodeIds(JSONObject routingResponse) {
        return getNodeIds(routingResponse, false);
    }

    /**
     * Method extracts Osm-Node IDs from first Route/Matching of OSRM Response
     *
     * @param routingResponse OSRM Response as JSON Object
     * @param duplications    result may contain duplications of ids (for u-turns or
     *                        circles)
     * @return List of OSM Node IDs
     */
    public static List<Long> getNodeIds(JSONObject routingResponse, boolean duplications) {
        try {
            // check response.routes/.matchings
            if (!routingResponse.has(ROUTES) && !routingResponse.has(MATCHINGS)) {
                // LOGGER.info("Routing Response has not \"routes\" or \"matchings\" attribute");
                // LOGGER.info("Check for nodes in waypoints:");
                if (routingResponse.has(WAYPOINTS)) {
                    JSONArray points = routingResponse.getJSONArray(WAYPOINTS);
                    // prevent duplication
                    List<Long> nodeOsmIds = new ArrayList<>();
                    for (int index = 0; index < points.length(); index++) {
                        JSONObject point = points.getJSONObject(index);
                        if (point.has(NODES)) {
                            JSONArray nodes = point.getJSONArray(NODES);
                            for (int i = 0; i < nodes.length(); i++) {
                                long osmid = nodes.getLong(i);
                                if (osmid != 0 && (duplications || !nodeOsmIds.contains(osmid))) {
                                    nodeOsmIds.add(osmid);
                                }
                            }
                        }
                    }
                    return nodeOsmIds;
                }
                return new ArrayList<Long>();
            }
            // check <responseTag>.legs
            JSONArray responseArray = routingResponse.getJSONArray(routingResponse.has(ROUTES) ? ROUTES : MATCHINGS);
            if (responseArray.isEmpty()) {
                LOGGER.info("Routing Response has empty \"routes\" or \"matchings\" attribute");
                return new ArrayList<Long>();
            }
            JSONObject responseTagObject = responseArray.getJSONObject(0);
            if (!responseTagObject.has(LEGS)) {
                LOGGER.info("ResponseTagObject has not \"legs\" attribute");
                return new ArrayList<Long>();
            }
            // use first
            JSONArray legsArray = responseTagObject.getJSONArray(LEGS);
            if (legsArray.length() == 0) {
                LOGGER.info("Routing Response has empty \"legs\" attribute");
                return new ArrayList<Long>();
            }
            // iterate through legs
            List<Long> ret = new ArrayList<Long>();
            for (int i = 0; i < legsArray.length(); i++) {
                // check for annotation
                JSONObject leg = legsArray.getJSONObject(i);
                if (!leg.has(ANNOTATION)) {
                    LOGGER.info("Leg has not \"annotation\" attribute");
                    continue;
                }
                // check for nodes
                JSONObject annotation = leg.getJSONObject(ANNOTATION);
                if (!annotation.has(NODES)) {
                    LOGGER.info("annotation has not \"nodes\" attribute");
                    continue;
                }
                // convert nodes array
                JSONArray nodes = annotation.getJSONArray(NODES);
                int startIndex = 0;
                if (duplications && i > 0) {
                    if (ret.size() >= 2 && nodes.length() >= 2 && ret.get(ret.size() - 2).equals(nodes.getLong(0))
                            && ret.get(ret.size() - 1).equals(nodes.getLong(1))) {
                        // Normal case: first segment in leg is duplication of
                        // last segment of previous leg. -> First two nodes are
                        // not added
                        startIndex = 2;
                    } else if (ret.size() >= 1 && nodes.length() >= 1 && ret.get(ret.size() - 1).equals(nodes.getLong(
                            0))) {
                        // U-Turn at end of leg -> first node is not dublicated
                        startIndex = 1;
                    }
                }
                for (int j = startIndex; j < nodes.length(); j++) {
                    long nodeId = nodes.getLong(j);
                    if (duplications || !ret.contains(nodeId)) {
                        ret.add(nodeId);
                    }
                }
            }
            return ret;

        } catch (JSONException e) {
            LOGGER.warn(e.getMessage(), e);
            return new ArrayList<Long>();
        }
    }

    /**
     * Method extracts Geometry of first Route/Matching of OSRM Response.
     * The Geometry is assumed to be in geoJson.
     *
     * @param routingResponse OSRM Response as JSON Object
     * @return Postgis LineString of Geometry of first Route/Matching
     */
    public static LineString getGeometry(JSONObject routingResponse) {
        try {
            // check response.routes/.matchings
            if (!routingResponse.has(ROUTES) && !routingResponse.has(MATCHINGS)) {
                LOGGER.info("Routing Response has not \"routes\" or \"matchings\" attribute");
                return null;
            }
            // check <responseTag>.legs
            JSONArray responseArray = routingResponse.getJSONArray(routingResponse.has(ROUTES) ? ROUTES : MATCHINGS);
            if (responseArray.isEmpty()) {
                LOGGER.info("Routing Response has empty \"routes\" or \"matchings\" attribute");
                return null;
            }
            // (first "routes" or "matchings")
            JSONObject responseTagObject = responseArray.getJSONObject(0);
            if (!responseTagObject.has(LEGS)) {
                LOGGER.info("ResponseTagObject has not \"legs\" attribute");
                return null;
            }
            return OsrmPolylineUtil.decodePoly(responseTagObject.getString(GEOMETRY));

        } catch (JSONException e) {
            LOGGER.warn(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Method modifies OSRM Response (as JSON Object) and attaches the osmIds as "osmIds"
     * at root JSON tag.
     *
     * @param routingResponse OSRM Response as JSON Object
     * @param osmIds          OSM Way IDs
     */
    public static void attachOsmIds(JSONObject routingResponse, List<Long> osmIds) {
        try {
            if (osmIds != null) {
                JSONArray osmIdArray = new JSONArray();
                for (int i = 0; i < osmIds.size(); i++) {
                    osmIdArray.put(i, osmIds.get(i).longValue());
                }
                routingResponse.put(OSMIDS, osmIdArray);
            }

        } catch (JSONException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    /**
     * clear all additional attachments
     *
     * @param routingResponse OSRM Response as JSON Object
     */
    public static void clearAttachsments(JSONObject routingResponse) {
        try {
            if (routingResponse.has(OSMIDS)) {
                routingResponse.remove(OSMIDS);
            }
            if (routingResponse.has(INTERFERENCES)) {
                routingResponse.remove(INTERFERENCES);
            }
            if (routingResponse.has(LOS_OFFSET_DATA)) {
                routingResponse.remove(LOS_OFFSET_DATA);
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
