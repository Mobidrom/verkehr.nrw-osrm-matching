package nrw.mobidrom.verkehr.service.openlr;

import com.google.protobuf.ByteString;
import net.postgis.jdbc.geometry.LineString;
import net.sf.geographiclib.Geodesic;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRDecodingStatus;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRObject;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLROsrmMatching;
import nrw.mobidrom.verkehr.service.osrm.parser.OsrmMatch;
import nrw.mobidrom.verkehr.service.osrm.parser.OsrmTracePoint;
import openlr.LocationReferencePoint;
import openlr.rawLocRef.RawLocationReference;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class is ...
 *
 * @author keko2055
 */
public class OpenLRValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenLRValidator.class);

    private static final int MAX_TRACE_DIST_STRICT = 10;
    private static final int MAX_TRACE_DIST = 25;
    private static final int MAX_LENGTH_ERR = 50;
    private static final double WEIGHT_LENGTH = 1.;
    private static final double WEIGHT_TRACE = 2.;
    private static final Geodesic GD = Geodesic.WGS84;

    public boolean validate(OpenLRObject decoding, boolean setPlausibleAttribute) {
        if (decoding == null) {
            return false;
        }
        boolean res = true;
        if (decoding.getGeometry() == null || decoding.getGeometry().isEmpty() || decoding.getOpenlr() == null) {
            res = false;
        } else {
            RawLocationReference openlr = DecoderHelper.decode(ByteString.copyFrom(Base64.decodeBase64(decoding.getOpenlr())));
            double openlrLength = DecoderHelper.getDecodedLength(openlr.getLocationReferencePoints());
            res = isPlausibleLength(openlr.getLocationReferencePoints(), decoding.getGeometry(), openlrLength);
            if (setPlausibleAttribute) {
                decoding.setPlausible(res);
            }
        }
        return res;
    }

    public OpenLROsrmMatching getBestMatch(List<LocationReferencePoint> lrpL, List<OpenLROsrmMatching> osrmMatchingL,
                                           Double openlrLength) {
        OpenLROsrmMatching go = null;
        if (osrmMatchingL != null) {
            Double maxErr = null;
            int idx = 0;
            int bestIdx = -1;
            for (OpenLROsrmMatching osrmMatching : osrmMatchingL) {
                OpenLRDecodingStatus status = osrmMatching.validate();
                LineString lineString = osrmMatching.getGeometry();
                OsrmMatch osrmMatch = osrmMatching.getRouting();
                if (OpenLRDecodingStatus.OK.equals(status) && isValidTrace(osrmMatch)) {
                    Double traceDist = maxTraceDistance(osrmMatch);
                    double osrmLength = DecoderHelper.getRoutingLength(lineString, GD);
                    Double lenDist = Math.abs(osrmLength - openlrLength);
                    Double err = WEIGHT_TRACE * traceDist + WEIGHT_LENGTH * lenDist;
                    if (maxErr == null || err < maxErr) {
                        maxErr = err;
                        go = osrmMatching;
                        bestIdx = idx;
                    }
                    LOGGER.debug("getBestMatch() trace:{} len:{} mix:{} max:{}", traceDist, lenDist, err, maxErr);
                }
                idx++;
            }
            if (bestIdx >= 0) {
                OpenLRDecodingStatus state = getStatus(lrpL, go, openlrLength);
                LOGGER.debug("getBestMatch() state:{} idx:{} of ({})", state.name(), bestIdx, idx);
            }
        }
        return go;
    }

    public OpenLRDecodingStatus getStatus(List<LocationReferencePoint> lrpL, OpenLROsrmMatching osrmMatching,
                                          double openlrLength) {
        if (osrmMatching != null) {
            OpenLRDecodingStatus go = osrmMatching.validate();
            LineString geo = osrmMatching.getGeometry();
            OsrmMatch osrmMatch = osrmMatching.getRouting();
            if (OpenLRDecodingStatus.OK.equals(go)) {
                if (!isPlausibleTrace(osrmMatch, MAX_TRACE_DIST) || !isPlausibleLength(lrpL, geo, openlrLength)) {
                    go = OpenLRDecodingStatus.OSRM_MATCHING_INPLAUSIBLE;
                }
            }
            return go;
        } else {
            return OpenLRDecodingStatus.OSRM_MATCHING_FAILED;
        }
    }

    public OpenLRDecodingStatus getStatusStrictCriteria(List<LocationReferencePoint> lrpL,
                                                        OpenLROsrmMatching osrmMatching, double openlrLength) {
        if (osrmMatching != null) {
            OpenLRDecodingStatus go = osrmMatching.validate();
            LineString geo = osrmMatching.getGeometry();
            OsrmMatch osrmMatch = osrmMatching.getRouting();
            if (OpenLRDecodingStatus.OK.equals(go)) {
                if (!isPlausibleTrace(osrmMatch, MAX_TRACE_DIST_STRICT) || !isPlausibleLength(lrpL, geo,
                        openlrLength)) {
                    go = OpenLRDecodingStatus.OSRM_MATCHING_INPLAUSIBLE;
                }
            }
            return go;
        } else {
            return OpenLRDecodingStatus.OSRM_MATCHING_FAILED;
        }
    }

    public boolean isPlausibleLength(List<LocationReferencePoint> lrpL, LineString geo, double openlrLength) {
        boolean go = false;
        if (lrpL != null && lrpL.size() > 1) {
            double osrmLength = DecoderHelper.getRoutingLength(geo, GD);
            double err = Math.abs(osrmLength - openlrLength);

            // OpenLR Object has max. error of +-29 meters to next LocationReferencePoint, 50 meters is to consider more
            // detailed osm net
            double errorTolerance = ((lrpL.size() - 1) * MAX_LENGTH_ERR);
            if (openlrLength > 0 && osrmLength > 0 && err < errorTolerance) {
                go = true;
                LOGGER.debug("isPlausibleLength() OSRM:{} OLR:{}", osrmLength, openlrLength);
            } else {
                LOGGER.debug("isPlausibleLength() FAIL err:{} OSRM:{} OLR:{}", err, osrmLength, openlrLength);
            }
        }
        return go;
    }

    private boolean isPlausibleTrace(OsrmMatch osrmMatch, int tolerance) {
        List<OsrmTracePoint> osrmTracePointL = osrmMatch == null ? null : osrmMatch.getTracePoints();
        if (osrmTracePointL != null && osrmTracePointL.size() > 1) {
            for (int idx = 0; idx < osrmTracePointL.size(); idx++) {
                OsrmTracePoint osrmTracePoint = osrmTracePointL.get(idx);
                Double dist = osrmTracePoint == null ? null : osrmTracePoint.getDistance();
                if (osrmTracePoint == null || dist > tolerance) {
                    LOGGER.debug("isPlausibleTrace() FAIL idx:{} distance:{}", idx, dist);
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private Boolean isValidTrace(OsrmMatch osrmMatch) {
        Boolean go = true;
        List<OsrmTracePoint> osrmTracePointL = osrmMatch == null ? null : osrmMatch.getTracePoints();
        if (osrmTracePointL != null && osrmTracePointL.size() > 1) {
            for (OsrmTracePoint osrmTracePoint : osrmTracePointL) {
                if (osrmTracePoint == null) {
                    go = false;
                }
            }
        }
        return go;
    }

    private Double maxTraceDistance(OsrmMatch osrmMatch) {
        double go = 0;
        List<OsrmTracePoint> osrmTracePointL = osrmMatch == null ? null : osrmMatch.getTracePoints();
        if (osrmTracePointL != null && osrmTracePointL.size() > 1) {
            for (OsrmTracePoint osrmTracePoint : osrmTracePointL) {
                if (go < osrmTracePoint.getDistance()) {
                    go = osrmTracePoint.getDistance();
                }
            }
        }
        return go;
    }
}
