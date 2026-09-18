package nrw.mobidrom.verkehr.service.osrm;

import jakarta.enterprise.context.ApplicationScoped;
import net.postgis.jdbc.geometry.LineString;
import net.postgis.jdbc.geometry.Point;
import nrw.mobidrom.verkehr.service.osrm.client.OsrmCustomServiceClient;
import nrw.mobidrom.verkehr.service.osrm.client.OsrmMatchServiceClient;
import nrw.mobidrom.verkehr.service.osrm.client.OsrmNearestServiceClient;
import nrw.mobidrom.verkehr.service.osrm.client.OsrmRouteServiceClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service handling OSRM Routing Routing Requests. This Service delegates the Routing
 * Requests to the OSRM Router defined by properties "osrm.router.*"
 *
 * @author stni2032
 */
@ApplicationScoped
public class OsrmRoutingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsrmRoutingService.class);

    @ConfigProperty(name = "osrm.openlr.router.host")
    private String routerHost;

    @ConfigProperty(name = "osrm.openlr.router.version", defaultValue = "v1")
    private String version;

    @ConfigProperty(name = "osrm.openlr.router.profile", defaultValue = "driving")
    private String profile;

    @ConfigProperty(name = "osrm.nearest.number", defaultValue = "10")
    private Integer number;

    /**
     * OSRM Service "match" with annotations=true
     *
     * @param lineString Geometry as LineString
     * @return List of OSM Ids
     * @throws OsrmServiceException thrown, if OSRM Service is not reachable or returned an error code
     */
    public List<Long> matchOsmIds(LineString lineString) throws OsrmServiceException {
        JSONObject response = match(toGeom(lineString));
        return RoutingResponseUtil.getNodeIds(response);
    }

    /**
     * OSRM Service "nearest"
     *
     * @param point
     * @return List of OSM Ids
     * @throws OsrmServiceException, if OSRM Service is not reachable or returned an error code
     */
    public List<Long> nearestOsmIds(Point point) throws OsrmServiceException {
        return nearestOsmIds(point, number);
    }

    /**
     * OSRM Service "nearest"
     *
     * @param point
     * @param number number of results
     * @return List of OSM Ids
     * @throws OsrmServiceException, if OSRM Service is not reachable or returned an error code
     */
    public List<Long> nearestOsmIds(Point point, int number) throws OsrmServiceException {
        JSONObject response = nearest(toGeom(point), number);
        return RoutingResponseUtil.getNodeIds(response);
    }

    /**
     * @param lineString
     * @return
     */
    private String toGeom(LineString lineString) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < lineString.numPoints(); i++) {
            if (i > 0) {
                sb.append(";");
            }
            sb.append(lineString.getPoint(i).x);
            sb.append(",");
            sb.append(lineString.getPoint(i).y);
        }
        return sb.toString();
    }

    private String toGeom(Point point) {
        return point.x + "," + point.y;
    }

    /**
     * OSRM Service "match" with annotations=true
     *
     * @param geom Geometry as "x1,y1;x2,y2;x3,y3"
     * @return OSRM Response as JSON Object
     * @throws OsrmServiceException thrown, if OSRM Service is not reachable or returned an error code
     */
    public JSONObject match(String geom) throws OsrmServiceException {
        OsrmMatchServiceClient client = new OsrmMatchServiceClient(routerHost, version, profile);
        String trimmedGeom = trim(geom, 100);
        JSONObject response = client.withAnnotations(true).service(trimmedGeom);
        checkInvalidResponse(response);
        return response;
    }

    public JSONObject nearest(String geom, int number) throws OsrmServiceException {
        OsrmNearestServiceClient client = new OsrmNearestServiceClient(routerHost, version, profile);
        JSONObject response = client.withNumber(number).service(geom);
        ;
        checkInvalidResponse(response);
        return response;
    }

    /**
     * OSRM Service "route" with annotations=true
     *
     * @param geom Geometry as "x1,y1;x2,y2;x3,y3"
     * @return OSRM Response as JSON Object
     * @throws OsrmServiceException thrown, if OSRM Service is not reachable or returned an error code
     */
    public JSONObject route(String geom) throws OsrmServiceException {
        OsrmRouteServiceClient client = new OsrmRouteServiceClient(routerHost, version, profile);
        JSONObject response = client.withAnnotations(true).service(geom);
        checkInvalidResponse(response);
        return response;
    }

    /**
     * Delegate Plain OSRM Request "Router/<pathInfo>?queryString" to OSRM Service
     *
     * @param pathInfo    URL Path (e.g. "match/v1/driving/5,50;6,60")
     * @param queryString URL Query-Paramters as query-String (e.g. "annotations=true&steps=false")
     * @return OSRM Response as JSON Object
     * @throws OsrmServiceException thrown, if OSRM Service is not reachable or returned an error code
     */
    public JSONObject callPlainOsrm(String pathInfo, String queryString) throws OsrmServiceException {
        OsrmCustomServiceClient client = new OsrmCustomServiceClient(routerHost, version, profile, pathInfo,
                queryString);
        JSONObject response = client.service("");
        checkInvalidResponse(response);
        return response;
    }

    /**
     * Method checks, if Response contains a Error Code and throws the OsrmServiceException
     */
    private void checkInvalidResponse(JSONObject response) throws OsrmServiceException {
        if (!response.has("code")) {
            throw new OsrmServiceException(-1, null, "No Element \"code\" in Response");
        }
        try {
            String code = response.getString("code");
            if (!"ok".equalsIgnoreCase(code)) {
                // invalid response
                int httpStatus = -1;
                OsrmRestResponseCodeType responseCode = OsrmRestResponseCodeType.getByCode(code);
                String message = "";
                if (response.has("message")) {
                    message = response.getString("message");
                }
                throw new OsrmServiceException(httpStatus, responseCode, message);
            }
        } catch (JSONException e) {
            LOGGER.trace(e.getMessage(), e);
            throw new OsrmServiceException(-1, null, "JSON Parsing Exception: " + e.getMessage());
        }
    }

    /**
     * @param line linestring text to trim
     * @param max  max number of points in linestring to keep
     * @return the trimmed string
     */
    private String trim(String line, int max) {
        String[] split = line.split(";");
        if (split.length <= max) {
            return line;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(split[0]);
        double f = (double) (split.length - 1) / (double) (max - 1);
        for (int i = 1; i < max; i++) {
            {
                sb.append(";");
                sb.append(split[(int) Math.round(i * f)]);
            }
        }
        return sb.toString();
    }

}
