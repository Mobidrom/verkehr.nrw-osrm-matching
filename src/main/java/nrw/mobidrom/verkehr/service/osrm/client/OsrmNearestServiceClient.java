package nrw.mobidrom.verkehr.service.osrm.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Osrm Rest/JSON Client for Service "Match"
 *
 * @author stni2032
 */
public class OsrmNearestServiceClient extends AbstractOsrmRestApiClient {

    private int number = 1;

    /**
     * @param routerHost host of OSRM Router
     * @param version    router version
     * @param profile    router profile
     */
    public OsrmNearestServiceClient(String routerHost, String version, String profile) {
        super(routerHost, version, profile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getService() {
        return "nearest";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> getQueryParams() {
        Map<String, String> ret = new HashMap<>();
        ret.put("number", String.valueOf(number));
        return ret;
    }

    /**
     * @param number the number to set
     * @return this
     */
    public OsrmNearestServiceClient withNumber(int number) {
        this.number = number;
        return this;
    }

}
