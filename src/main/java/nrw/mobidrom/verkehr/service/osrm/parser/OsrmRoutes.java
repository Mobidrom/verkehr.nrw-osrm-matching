package nrw.mobidrom.verkehr.service.osrm.parser;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class OsrmRoutes {
    /**
     * JSON Attribute "routes" in OSRM Response for Service "route".
     * Defined by OSRM-Spec.
     */
    public static final String ROUTES = "routes";

    private static final Logger LOGGER = LoggerFactory.getLogger(OsrmRoutes.class);

    private List<OsrmRoute> routes;

    public OsrmRoutes(JSONObject routingResponse) {
        routes = new ArrayList<OsrmRoute>();
        try {
            // check response.routes/.matchings
            if (!routingResponse.has(ROUTES)) {
                LOGGER.warn("Routing Response has not \"routes\" attribute");
                return;
            }
            JSONArray responseArray = routingResponse.getJSONArray(ROUTES);
            if (responseArray.length() == 0) {
                LOGGER.warn("Routing Response has empty \"routes\" attribute");
                return;
            }
            for (int i = 0; i < responseArray.length(); i++) {
                routes.add(new OsrmRoute(responseArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Routes: \n");
        for (OsrmRoute route : routes) {
            sb.append(route);
            sb.append("\n");
        }
        return sb.toString();
    }

}
