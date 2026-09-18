package nrw.mobidrom.verkehr.service.openlr;

import com.google.protobuf.ByteString;
import net.postgis.jdbc.geometry.LineString;
import net.postgis.jdbc.geometry.Point;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRDecodingStatus;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRMatchingParameter;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRMatchingParameters;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRObject;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLROsrmMatching;
import nrw.mobidrom.verkehr.service.impl.OpenLROsrmMatchingService;
import nrw.mobidrom.verkehr.service.osrm.parser.OsrmMatch;
import openlr.LocationReferencePoint;
import openlr.rawLocRef.RawLocationReference;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is ...
 *
 * @author keko2055
 */
public class OpenLROsrmMatchingDecoder implements IOpenLRDecoder {
    private static final Geodesic GD = Geodesic.WGS84;
    private final OpenLRValidator validator;
    private final OpenLROsrmMatchingService matchingService;

    public OpenLROsrmMatchingDecoder(OpenLROsrmMatchingService matchingService) {
        this.matchingService = matchingService;
        validator = new OpenLRValidator();
    }

    @Override
    public OpenLRObject decode(ByteString openlr) {
        OpenLRObject res = new OpenLRObject();
        if (openlr == null) {
            res.setStatus(OpenLRDecodingStatus.NO_DATA);
            return res;
        }
        res.setOpenlr(Base64.getEncoder().encodeToString(openlr.toByteArray()));
        RawLocationReference rawLocRef = DecoderHelper.decode(openlr);
        // olro 20210805 use lowest FRC to next point on path
        RawLocationReferenceMgr mgr = new RawLocationReferenceMgr(rawLocRef);
        if (!mgr.isValid()) {
            res.setStatus(OpenLRDecodingStatus.BYTE_STRING_DECODING_ERROR);
            return res;
        }
        res.setValid(rawLocRef.isValid());
        // FunctionalRoadClass frc = getHighestFRC(rawLocRef.getLocationReferencePoints());
        res.setFrc(mgr.getFrc());
        List<LocationReferencePoint> lrpL = rawLocRef.getLocationReferencePoints();
        OpenLROsrmMatching routing = getRouting(lrpL);
        if (routing == null) {
            res.setStatus(OpenLRDecodingStatus.OSRM_MATCHING_FAILED);
            return res;
        }
        res.setStatus(routing.validate());
        if (!OpenLRDecodingStatus.OK.equals(res.getStatus())) {
            return res;
        }
        double openlrLength = DecoderHelper.getDecodedLength(lrpL);
        if (!validator.isPlausibleLength(lrpL, routing.getGeometry(), openlrLength)) {
            res.setStatus(OpenLRDecodingStatus.OSRM_MATCHING_INPLAUSIBLE);
        }

        res.setTracePoints(getTracePoints(routing.getRouting()));
        res.setBearings(getBearings(routing.getRouting()));
        // length? take care for offset
        // res.setLength(DecoderHelper.getRoutingLength(routing.getGeometry(), GD));
        OpenLROsrmMatchingOffset matchingOffset = new OpenLROsrmMatchingOffset(routing, rawLocRef);
        LineString geometry = matchingOffset.getGeometry();
        if (geometry == null || geometry.isEmpty() || geometry.numPoints() < 2) {
            res.setStatus(OpenLRDecodingStatus.APPLY_OFFSET_FAILED);
            return res;
        }
        res.setGeometry(geometry);
        res.setOsmIdList(matchingOffset.getOsmIds());
        res.setLengths(matchingOffset.getLengths());
        res.setLength(matchingOffset.getLength());

        Set<Long> set = new HashSet<>(res.getOsmIdList());
        if (set.size() != res.getOsmIdList().size()) {
            if (OpenLRDecodingStatus.OK.equals(res.getStatus())) {
                res.setStatus(OpenLRDecodingStatus.OSM_ID_DUPLICATION);
            }
        }

        return res;
    }

    @Override
    public OpenLRObject decode(ByteString openlrCode, Integer realLength) {
        // decode raw
        RawLocationReference rawLocation = decodeRaw(openlrCode);
        OpenLRObject openlrGo = decodeRawLocation(rawLocation, realLength);
        openlrGo.setOpenlr(Base64.getEncoder().encodeToString(openlrCode.toByteArray()));
        return openlrGo;
    }

    @Override
    public OpenLRObject decodeRawLocation(RawLocationReference rawLocation, Integer realLength) {
        OpenLRObject openlrGo = new OpenLRObject();
        Boolean isRawOk = populateRaw(openlrGo, rawLocation);
        if (isRawOk && OpenLRMatchingParameters.size() > 0) {
            // if raw OK, further decode by OSRM match service
            List<LocationReferencePoint> lrpL = rawLocation.getLocationReferencePoints();
            List<OpenLRMatchingParameter> parameterL = OpenLRMatchingParameters.getParameterlist();
            OpenLROsrmMatching osrmMatching = matchingService.getMatching(lrpL, parameterL.getFirst());
            double openlrLength = realLength <= 0 ? DecoderHelper.getDecodedLength(lrpL) : realLength;
            OpenLRDecodingStatus state = validator.getStatusStrictCriteria(lrpL, osrmMatching, openlrLength);
            if (!OpenLRDecodingStatus.OK.equals(state)) {
                // OSRM match with strict criteria failed, try alternative parameter with moderate criteria
                if (OpenLRMatchingParameters.size() > 1) {
                    List<OpenLRMatchingParameter> subL = parameterL.subList(1, OpenLRMatchingParameters.size());
                    List<OpenLROsrmMatching> osrmMatchingL = decodeMatch(lrpL, subL);
                    osrmMatchingL.add(osrmMatching);
                    osrmMatching = validator.getBestMatch(lrpL, osrmMatchingL, openlrLength);
                    state = validator.getStatus(lrpL, osrmMatching, openlrLength);
                }
            }
            openlrGo.setStatus(state);
            if (OpenLRDecodingStatus.OK.equals(state) || OpenLRDecodingStatus.OSRM_MATCHING_INPLAUSIBLE.equals(state)) {
                openlrGo = populateMatch(openlrGo, rawLocation, osrmMatching);
            }
        }
        return openlrGo;
    }

    private List<OpenLROsrmMatching> decodeMatch(List<LocationReferencePoint> lrpL,
                                                 List<OpenLRMatchingParameter> paraL) {
        List<OpenLROsrmMatching> osrmMatchingL = new ArrayList<OpenLROsrmMatching>();
        for (int idx = 0; idx < paraL.size(); idx++) {
            OpenLRMatchingParameter parameter = paraL.get(idx);
            OpenLROsrmMatching matching = matchingService.getMatching(lrpL, parameter);
            osrmMatchingL.add(matching);
        }
        return osrmMatchingL;
    }

    private RawLocationReference decodeRaw(ByteString openlrCode) {
        return openlrCode == null ? null : DecoderHelper.decode(openlrCode);
    }

    private Boolean populateRaw(OpenLRObject openlrGo, RawLocationReference rawLocation) {
        Boolean go = false;
        if (rawLocation != null) {
            RawLocationReferenceMgr mgr = new RawLocationReferenceMgr(rawLocation);
            if (mgr.isValid()) {
                openlrGo.setValid(rawLocation.isValid());
                openlrGo.setFrc(mgr.getFrc());
                go = true;
            } else {
                openlrGo.setStatus(OpenLRDecodingStatus.BYTE_STRING_DECODING_ERROR);
            }
        } else {
            openlrGo.setStatus(OpenLRDecodingStatus.NO_DATA);
        }
        return go;
    }

    private OpenLROsrmMatching getRouting(List<LocationReferencePoint> locationReferencePoints) {
        double openlrLength = DecoderHelper.getDecodedLength(locationReferencePoints);
        List<OpenLRMatchingParameter> parameterList = OpenLRMatchingParameters.getParameterlist();
        for (int i = 0; i < parameterList.size() - 1; i++) {
            OpenLROsrmMatching res = matchingService.getMatching(locationReferencePoints, parameterList.get(i));
            if (OpenLRDecodingStatus.OK.equals(res.validate()) && validator.isPlausibleLength(locationReferencePoints,
                    res.getGeometry(), openlrLength)) {
                return res;
            }
        }
        return matchingService.getMatching(locationReferencePoints, parameterList.get(parameterList.size() - 1));
    }

    private double[] getBearings(OsrmMatch routing) {
        double[] res = new double[2];
        LineString line = (LineString) routing.getMatchings().getFirst().getGeometry();
        Point intersect = LineStringHelper.intersect(line, 20);
        GeodesicData inverse = GD.Inverse(line.getFirstPoint().y, line.getFirstPoint().x, intersect.y, intersect.x);
        res[0] = inverse.azi1 < 0 ? inverse.azi1 + 360.0 : inverse.azi1;
        intersect = LineStringHelper.intersectReverse(line, 20);
        inverse = GD.Inverse(line.getLastPoint().y, line.getLastPoint().x, intersect.y, intersect.x);
        res[1] = inverse.azi1 < 0 ? inverse.azi1 + 360.0 : inverse.azi1;
        return res;
    }

    private LineString getTracePoints(OsrmMatch routing) {
        // Point[] points = new Point[routing.getTracePoints().size()];
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < routing.getTracePoints().size(); i++) {
            if (routing.getTracePoints().get(i) != null) {
                points.add(routing.getTracePoints().get(i).getLocation());
            }
            // points[i] = routing.getTracePoints().get(i).getLocation();
        }
        if (points.size() < 2) {
            return null;
        }
        return new LineString(points.toArray(new Point[0]));
    }

    private OpenLRObject populateMatch(OpenLRObject go, RawLocationReference rlr, OpenLROsrmMatching osrmMatching) {
        // OpenLRObject go = decodeMatch(olr, rlr);
        // go.setStatus(state);
        OsrmMatch osrmMatch = osrmMatching.getRouting();
        Long maxDist = DecoderHelper.getTraceMaxDistance(osrmMatch);
        go.setTraceMaxDistance(maxDist);
        go.setTracePoints(getTracePoints(osrmMatch));
        go.setBearings(getBearings(osrmMatch));
        OpenLROsrmMatchingOffset matchingOffset = new OpenLROsrmMatchingOffset(osrmMatching, rlr);
        LineString geometry = matchingOffset.getGeometry();
        if (geometry == null || geometry.isEmpty() || geometry.numPoints() < 2) {
            go.setStatus(OpenLRDecodingStatus.APPLY_OFFSET_FAILED);
            return go;
        }
        go.setGeometry(geometry);
        go.setOsmIdList(matchingOffset.getOsmIds());
        go.setLengths(matchingOffset.getLengths());
        go.setLength(matchingOffset.getLength());
        // TODO
        Set<Long> set = new HashSet<Long>(go.getOsmIdList());
        if (set.size() != go.getOsmIdList().size()) {
            if (OpenLRDecodingStatus.OK.equals(go.getStatus())) {
                go.setStatus(OpenLRDecodingStatus.OSM_ID_DUPLICATION);
            }
        }
        return go;
    }
}
