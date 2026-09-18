package nrw.mobidrom.verkehr.helper;

/**
 * Helper Class for Postgis related issues.
 * <p>
 * This class should contain constants and often used convenience methods
 */
public class SridConstants {

    /**
     * SRID for WGS84
     */
    public static final int SRID_WGS_84 = 4326;

    /**
     * SRID for EPSG Gauß-Krueger, zone two
     */
    public static final int SRID_EPSG_GAUS_KRUEGER_ZONE_TWO = 31466;

    /**
     * SRID for EPSG Gauß-Krueger, zone three
     */
    public static final int SRID_EPSG_GAUS_KRUEGER_ZONE_THREE = 31467;

    /**
     * SRID for EPSG Gauß-Krueger, zone four
     */
    public static final int SRID_EPSG_GAUS_KRUEGER_ZONE_FOUR = 31468;

    /**
     * SRID for EPSG Gauß-Krueger, zone five
     */
    public static final int SRID_EPSG_GAUS_KRUEGER_ZONE_FIVE = 31469;

    /**
     * SRID for the standardized srid
     */
    public static final int SRID_ESPG_UTM_ZONE_32N = 25832;

    /**
     * SRID for the WGS 84 / UTM zone 32N
     */
    public static final int SRID_WGS84_UTM_ZONE_32N = 32632;

    /**
     * SRID (alias) for calculations in metres e.g. distances
     */
    public static final int METRIC_SRID = SRID_EPSG_GAUS_KRUEGER_ZONE_TWO;

    private static final String EPSG_25832 = "ETRS89 EPSG 25832";

    public static int parseSrs(String srs) {
        if (EPSG_25832.equals(srs)) {
            return SRID_ESPG_UTM_ZONE_32N;
        }
        // default
        return SRID_WGS_84;
    }
}
