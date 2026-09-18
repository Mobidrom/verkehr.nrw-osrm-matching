package nrw.mobidrom.verkehr.helper;

public final class OsrmMatchingConstants {

    private OsrmMatchingConstants() {
    }

    // Rest parameters
    public static final String SRID = "srid";
    public static final String DEFAULT_SRID = "4326";
    public static final String REAL_LENGTH = "realLength";
    public static final String FORCE_RECALCULATE = "forceRecalculate";
    public static final String ZERO = "0";
    public static final String FIVE = "5";
    public static final String NUMBER = "number";

    // Rest Paths
    public static final String ROOT_PATH = "/";
    public static final String MATCH_OSM_IDS = "matchOsmIds";
    public static final String NEAREST_OSM_IDS = "nearestOsmIds";
    public static final String GEOMETRY = "geometry";
    public static final String OPENLR = "openlr";
    public static final String OPENLR_EXTENDED_LINEAR = "openlrExtendedLinear";
    public static final String FIND_LCL_MAPPING = "findLclMapping";
    public static final String UPDATE_OSM_IDS = "updateOsmIds";

    // Rest errors
    public static final String NO_MATCHING_SEGMENT = "Could not find a matching segment for any coordinate.";

    // Rest test parameters
    public static final String EMPTY = "";
}
