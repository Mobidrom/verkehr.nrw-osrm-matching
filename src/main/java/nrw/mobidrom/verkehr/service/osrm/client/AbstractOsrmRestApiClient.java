package nrw.mobidrom.verkehr.service.osrm.client;

import jakarta.ws.rs.core.MediaType;
import lombok.Getter;
import nrw.mobidrom.verkehr.service.osrm.OsrmRestResponseCodeType;
import nrw.mobidrom.verkehr.service.osrm.OsrmServiceException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Abstract Implementation Rest-Client for OSRM API
 *
 * @author stni2032
 */
@Getter
public abstract class AbstractOsrmRestApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOsrmRestApiClient.class);

    private final String routerHost;
    private final String version;
    private final String profile;

    /**
     * @param routerHost host of OSRM Router
     * @param version    router version
     * @param profile    router profile
     */
    public AbstractOsrmRestApiClient(String routerHost, String version, String profile) {
        this.routerHost = routerHost;
        this.version = version;
        this.profile = profile;
    }

    /**
     * @return Query Params for Service Call
     */
    protected abstract Map<String, String> getQueryParams();

    /**
     * @return OSRM service name (e.g. "route")
     */
    protected abstract String getService();

    // /**
    // * Call OSRM Service
    // *
    // * @param coordinates List of Coordinates "x1,y1;x2,y2;x3,y3"
    // *
    // * @return OSRM Service Result
    // *
    // * @throws OsrmServiceException with HTTP or OSRM Error Code
    // */
    // public T service(XOsrmGeometry geometry) throws OsrmServiceException {
    // String coordinates = convert(geometry);
    // return service(coordinates);
    // }

    /**
     * Call OSRM Service
     *
     * @param coordinates List of Coordinates "x1,y1;x2,y2;x3,y3"
     * @return OSRM Service Result
     * @throws OsrmServiceException with HTTP or OSRM Error Code
     */
    public JSONObject service(String coordinates) throws OsrmServiceException {

        // call GET
        HttpResponse<String> response = doRequest(coordinates);

        // handle Response Converting
        String body = response.body();
        LOGGER.debug("Received Body {}", body);

        // convert to json
        JSONObject json = null;
        try {
            json = new JSONObject(new JSONTokener(body));
        } catch (JSONException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        LOGGER.debug("Return JSON: {}", body);
        return json;
    }

    private HttpResponse<String> doRequest(String coordinates) throws OsrmServiceException {
        String service = Objects.requireNonNull(getService(), "service must be not null");
        String params = printParameters();
        try {
            HttpResponse<String> response = httpRequest(coordinates, getService(), params);
            int code = response.statusCode();
            if (code == 400) {
                for (OsrmRestResponseCodeType state : OsrmRestResponseCodeType.values()) {
                    if (response.body()
                            .contains(state.getCode())
                            && !OsrmRestResponseCodeType.ignoredMessages()
                            .contains(state)) {
                        LOGGER.warn(
                                "OsrmRestCall produced a bad request: {}, for service: {}, params: {} and coordinates: {}",
                                response.body(), service, params, coordinates);
                    }
                }
            }
            return response;
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
            int httpStatus = -1;
            throw new OsrmServiceException(httpStatus, null, e.getMessage());
        }
    }

    protected HttpResponse<String> httpRequest(String coordinates, String service, String params)
            throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(routerHost + "/" + service + "/" + version + "/"
                        + profile + "/" + coordinates + "?" + params))
                .headers("Accept", MediaType.APPLICATION_JSON)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        //        System.out.println(request.uri());
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            return httpClient.send(request, BodyHandlers.ofString());
        }
    }

    private String printParameters() {

        Map<String, String> params = getQueryParams();
        StringBuilder sb = new StringBuilder();
        if (params != null) {
            for (Entry<String, String> entry : params.entrySet()) {
                sb.append(!sb.isEmpty() ? "&" : "");
                sb.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue());
            }
        }
        return sb.toString();
    }

}
