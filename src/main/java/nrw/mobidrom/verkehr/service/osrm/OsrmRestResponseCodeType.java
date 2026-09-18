package nrw.mobidrom.verkehr.service.osrm;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Response Code for OSRM Rest Response.
 * <p>
 * See https://github.com/Project-OSRM/osrm-backend/blob/master/docs/http.md
 *
 * @author stni2032
 */
@Getter
public enum OsrmRestResponseCodeType {

    /**
     * Request could be processed as expected.
     */
    OK("Ok"),

    /**
     * URL string is invalid.
     */
    INVALID_URL("InvalidUrl"),

    /**
     * Service name is invalid.
     */
    INVALID_SERVICE("InvalidService"),

    /**
     * Version is not found.
     */
    INVALID_VERSION("InvalidVersion"),

    /**
     * Options are invalid.
     */
    INVALID_OPTIONS("InvalidOptions"),

    /**
     *
     */
    INVALID_VALUE("InvalidValue"),

    /**
     *
     */
    INVALID_QUERY("InvalidQuery"),

    /**
     * One of the supplied input coordinates could not snap to street segment.
     */
    NO_SEGMENT("NoSegment"),

    /**
     * The request size violates one of the service specific request size
     * restrictions.
     */
    TOO_BIG("TooBig"),

    /**
     * No route found. (only service "route")
     */
    NO_ROUTE("NoRoute"),

    /**
     * No route found. (only service "table")
     */
    NO_TABLE("NoTable"),

    /**
     * No matchings found. (only service "match")
     */
    NO_MATCH("NoMatch"),

    /**
     * No trips found. (only service "trip")
     */
    NO_TRIPS("NoTrips");

    private final String code;

    OsrmRestResponseCodeType(String code) {
        this.code = code;
    }

    public static List<OsrmRestResponseCodeType> ignoredMessages() {
        return Arrays.asList(NO_TRIPS, NO_MATCH, NO_TABLE, NO_SEGMENT, NO_ROUTE, OK);
    }

    /**
     * return Enum by OSRM Code
     *
     * @param code OSRM Attribute "code"
     * @return matching OsrmRestResponseCode or null
     */
    public static OsrmRestResponseCodeType getByCode(String code) {
        for (OsrmRestResponseCodeType e : values()) {
            if (code.equalsIgnoreCase(e.code)) {
                return e;
            }
        }
        return null;
    }
}
