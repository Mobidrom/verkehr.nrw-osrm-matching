package nrw.mobidrom.verkehr.service.openlr;

import com.google.protobuf.ByteString;
import net.postgis.jdbc.geometry.LineString;
import net.postgis.jdbc.geometry.Point;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import nrw.mobidrom.verkehr.service.osrm.parser.OsrmMatch;
import nrw.mobidrom.verkehr.service.osrm.parser.OsrmTracePoint;
import openlr.LocationReferencePoint;
import openlr.LocationType;
import openlr.PhysicalDecoder;
import openlr.PhysicalFormatException;
import openlr.binary.ByteArray;
import openlr.binary.impl.LocationReferenceBinaryImpl;
import openlr.decoder.OpenLRDecoder;
import openlr.rawLocRef.RawLocationReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Collection of helping methods
 *
 * @author keko2055
 */
public final class DecoderHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecoderHelper.class);

    private DecoderHelper() {
    }

    /**
     * Decoding openLR ByteString Only accepting line geometries
     *
     * @param openlr input
     * @return decoded openLR object
     */
    public static RawLocationReference decode(ByteString openlr) {
        ByteArray ba = new ByteArray(openlr.toByteArray());
        try {
            LocationReferenceBinaryImpl locRef;
            locRef = new LocationReferenceBinaryImpl("LocationID", ba);
            if (!LocationType.LINE_LOCATION.equals(locRef.getLocationType())) {
                LOGGER.warn("openLR code is not a line geometry");
                return null;
            }
            if (!locRef.isValid()) {
                LOGGER.warn("openLR code is invalid");
                return null;
            }
            return read(locRef);
        } catch (PhysicalFormatException e) {
            LOGGER.warn(e.getLocalizedMessage());
            return null;
        }
    }

    private static RawLocationReference read(LocationReferenceBinaryImpl locRef) {
        List<PhysicalDecoder> decoders = OpenLRDecoder.resolvePhysicalDecoder();
        for (PhysicalDecoder p : decoders) {
            if (p.getDataFormatIdentifier().equals(locRef.getDataIdentifier())) {
                try {
                    return p.decodeData(locRef);
                } catch (PhysicalFormatException e) {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
        }
        return null;
    }

    /**
     * provide maximum distance of trace points of an OSRM match
     *
     * @param osrmMatch OSRM match
     * @return maximum distance of trace points
     */
    public static Long getTraceMaxDistance(OsrmMatch osrmMatch) {
        Long go = -1L;
        List<OsrmTracePoint> osrmTracePointL = osrmMatch == null ? null : osrmMatch.getTracePoints();
        if (osrmTracePointL != null && osrmTracePointL.size() > 1) {
            for (OsrmTracePoint osrmTracePoint : osrmTracePointL) {
                if (osrmTracePoint == null) {
                    go = -1L;
                    break;
                }
                Double dist = osrmTracePoint.getDistance();
                if (dist > go) {
                    go = Math.round(dist);
                }
            }
        }
        return go;
    }

    /**
     * Adding up length of segments in openLR object. Offset values in openLR object are not considered.
     *
     * @param locationReferencePoints list of reference points from openLR object
     * @return length of line
     */
    public static double getDecodedLength(List<LocationReferencePoint> locationReferencePoints) {
        if (locationReferencePoints == null) {
            return -1;
        }
        double l = 0;
        for (LocationReferencePoint locationReferencePoint : locationReferencePoints) {
            l += locationReferencePoint.getDistanceToNext();
        }
        return l;
    }

    /**
     * Calculates length of a LineString
     *
     * @param geometry line
     * @param gd       Geodesic object must use same srid as line object
     * @return length in meter
     */
    public static double getRoutingLength(LineString geometry, Geodesic gd) {
        if (geometry == null) {
            return -1;
        }
        double dist = 0;

        Point pfrom = geometry.getPoint(0);
        for (int i = 1; i < geometry.numPoints(); i++) {
            Point pto = geometry.getPoint(i);
            double dd = getDistance(gd, pfrom, pto);
            dist += dd;
            pfrom = pto;
        }
        return dist;
    }

    /**
     * Calculates distance between to points
     *
     * @param gd    Geodesic object must use same srid as point objects
     * @param pfrom from
     * @param pto   to
     * @return distance in meters
     */
    public static double getDistance(Geodesic gd, Point pfrom, Point pto) {
        GeodesicData inverse = gd.Inverse(pfrom.y, pfrom.x, pto.y, pto.x);
        return inverse.s12;
    }
}
