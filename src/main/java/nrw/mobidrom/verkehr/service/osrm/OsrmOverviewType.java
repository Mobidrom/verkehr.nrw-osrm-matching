package nrw.mobidrom.verkehr.service.osrm;

import lombok.Getter;

/**
 * Overview Type for OSRM Rest Request.
 * <p>
 * See https://github.com/Project-OSRM/osrm-backend/blob/master/docs/http.md
 *
 * @author stni2032
 */
@Getter
public enum OsrmOverviewType {

    /**
     * Add overview geometry simplified according to highest zoom level it could be display on
     * <p>
     * (default)
     */
    SIMPLIFIED("simplified"),

    /**
     * Add overview geometry either full
     */
    FULL("full"),

    /**
     * Add overview geometry not at all.
     */
    FALSE("false");

    private final String code;

    OsrmOverviewType(String code) {
        this.code = code;
    }

}
