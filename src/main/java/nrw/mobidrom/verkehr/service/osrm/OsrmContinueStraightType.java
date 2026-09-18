package nrw.mobidrom.verkehr.service.osrm;

import lombok.Getter;

/**
 * continue_straight Type for OSRM Rest Request.
 * <p>
 * See https://github.com/Project-OSRM/osrm-backend/blob/master/docs/http.md
 * <p>
 * Forces the route to keep going straight at waypoints and don't do a uturn
 * even if it would be faster. Default value depends on the profile.
 *
 * @author stni2032
 */
@Getter
public enum OsrmContinueStraightType {

    DEFAULT("default"),

    TRUE("true"),

    FALSE("false");

    private final String code;

    OsrmContinueStraightType(String code) {
        this.code = code;
    }

}
