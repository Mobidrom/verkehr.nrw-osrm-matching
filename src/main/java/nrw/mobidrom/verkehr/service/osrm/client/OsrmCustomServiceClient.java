package nrw.mobidrom.verkehr.service.osrm.client;

import jakarta.ws.rs.core.MediaType;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Osrm Rest/JSON Client for Service "Route"
 *
 * @author stni2032
 */
public class OsrmCustomServiceClient extends AbstractOsrmRestApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsrmCustomServiceClient.class);

    private final String pathInfo;
    private final String queryString;

    /**
     * @param routerHost  host of OSRM Router
     * @param version     router version
     * @param profile     router profile
     * @param pathInfo    URL Path (e.g. "match/v1/driving/5,50;6,60")
     * @param queryString URL Query-Paramters as query-String (e.g. "annotations=true&steps=false")
     */
    public OsrmCustomServiceClient(String routerHost, String version, String profile, String pathInfo,
                                   String queryString) {
        super(routerHost, version, profile);
        this.pathInfo = pathInfo;
        this.queryString = queryString;
    }

    /**
     * @param coordinates OSRM coordinates String
     * @param service     OSRM service identifier
     * @param params      OSRM URL Parameters
     * @return Response of OSRM
     * @throws URISyntaxException   URI Syntax Error
     * @throws IOException          Http request failed
     * @throws InterruptedException Http request got interrupted
     */
    @Override
    protected HttpResponse<String> httpRequest(String coordinates, String service,
                                               String params) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(getRouterHost() + "/" + pathInfo + "/" + queryString))
                .headers("Accept", MediaType.APPLICATION_JSON)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            return httpClient.send(request, BodyHandlers.ofString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getService() {
        return "customservice";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> getQueryParams() {
        return new HashMap<String, String>();
    }

}
