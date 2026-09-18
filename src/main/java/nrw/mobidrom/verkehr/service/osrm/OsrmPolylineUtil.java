package nrw.mobidrom.verkehr.service.osrm;

import net.postgis.jdbc.geometry.LineString;
import net.postgis.jdbc.geometry.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper Class for Google's Polylines
 * From: http://www.geekyblogger.com/2010/12/decoding-polylines-from-google-maps.html
 *
 * @author stni2032
 */
public final class OsrmPolylineUtil {

    // constants for algorithm
    private static final int SHIFT_BITMASK = 0x1f;
    private static final int SUM_SHIFT = 5;
    private static final int TERM_ASCII = 0x20;
    private static final int MIN_ASCII = 63;
    private static final double LAT_LON_FACTOR = 1E5;

    /**
     * Helper Class
     */
    private OsrmPolylineUtil() {
    }

    /**
     * From: http://www.geekyblogger.com/2010/12/decoding-polylines-from-google-maps.html
     *
     * @param encoded Polylines from Google Maps Directions API
     * @return decoded XOsrmGeometry
     */
    public static LineString decodePoly(String encoded) {
        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;
        List<Point> points = new ArrayList<>();
        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = encoded.charAt(index++) - MIN_ASCII;
                result |= (b & SHIFT_BITMASK) << shift;
                shift += SUM_SHIFT;
            } while (b >= TERM_ASCII);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - MIN_ASCII;
                result |= (b & SHIFT_BITMASK) << shift;
                shift += SUM_SHIFT;
            } while (b >= TERM_ASCII);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            double x = (lng / LAT_LON_FACTOR);
            double y = (lat / LAT_LON_FACTOR);
            Point p = new Point(x, y);
            points.add(p);
        }
        if (points.isEmpty()) {
            return null;
        }
        return new LineString(points.toArray(new Point[0]));
    }
}
