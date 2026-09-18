package nrw.mobidrom.verkehr.service.openlr;

import net.postgis.jdbc.geometry.LineString;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLROsrmMatching;
import nrw.mobidrom.verkehr.service.osrm.parser.OsrmLeg;
import nrw.mobidrom.verkehr.service.osrm.parser.OsrmMatching;
import openlr.LocationReferencePoint;
import openlr.Offsets;
import openlr.rawLocRef.RawLocationReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OpenLROsrmMatchingOffset {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenLROsrmMatchingOffset.class);
    private static final int SRID = 4326;
    private double length;
    private double[] lengths;
    private LineString geometry;
    private List<Long> osmIds;

    public OpenLROsrmMatchingOffset(OpenLROsrmMatching routing, RawLocationReference rawLocRef) {
        double pOffset = 0.;
        double nOffset = 0.;
        if (offsetExist(rawLocRef.getOffsets())) {
            pOffset = getPositiveOffset(rawLocRef);
            nOffset = getNegativeOffset(rawLocRef);
            geometry = LineStringHelper.reduceLine(routing.getGeometry(), pOffset, nOffset);
        } else {
            geometry = routing.getGeometry();
        }
        geometry.setSrid(SRID);
        osmIds = nodeByRoute(routing.getRouting().getMatchings().get(0), pOffset, nOffset);
        lengths = lengthByRoute(routing.getRouting().getMatchings().get(0), pOffset, nOffset);
        length = sumLength(lengths);
    }

    /**
     * @return
     */
    public LineString getGeometry() {
        return geometry;
    }

    /**
     * @return
     */
    public double getLength() {
        return length;
    }

    /**
     * @return
     */
    public double[] getLengths() {
        return lengths;
    }

    /**
     * @return
     */
    public List<Long> getOsmIds() {
        return osmIds;
    }

    //    private double getLength(OpenLROsrmMatching openLROsrmMatching) {
    //        Double go = openLROsrmMatching.getRouting().getMatchings().get(0).getDistance();
    //        return go != null && go > 0. ? go : DecoderHelper.getRoutingLength(openLROsrmMatching.getGeometry(), GD);
    //    }

    private double sumLength(double[] lengthA) {
        double go = 0.;
        if (lengthA != null) {
            for (double val : lengthA) {
                go += val;
            }
        }
        return go;
    }

    private List<Long> nodeByRoute(OsrmMatching matching, double pOffset, double nOffset) {
        // aggregate legs
        int nodeCount = 2;
        for (OsrmLeg leg : matching.getLegs()) {
            nodeCount += leg.getAnnotation().getNodes().length - 2;
        }
        long[] osmIds = new long[nodeCount];
        double[] distances = new double[nodeCount - 1];
        int idx = 2;
        for (OsrmLeg leg : matching.getLegs()) {
            idx -= 2;
            for (int i = 0; i < leg.getAnnotation().getNodes().length; i++) {
                if (osmIds[idx + i] == 0 || osmIds[idx + i] == leg.getAnnotation().getNodes()[i]) {
                    osmIds[idx + i] = leg.getAnnotation().getNodes()[i];
                } else {
                    // First two points of leg should be duplication of last two points from previous leg
                    // In case of U-Turn at tracepoint this is not the case. For OpenLR matching this is considered as invalid
                    LOGGER.warn("Osrm response misformed!");
                    return new ArrayList<>();
                }
            }
            for (int i = 0; i < leg.getAnnotation().getDistance().length; i++) {
                distances[idx + i] += leg.getAnnotation().getDistance()[i];
            }
            idx += leg.getAnnotation().getNodes().length;
        }
        // consider positive offset
        int startIndex = 0;
        while (startIndex < distances.length - 1 && pOffset > 0) {
            pOffset -= distances[startIndex];
            startIndex++;
        }
        // consider negative offset
        int endIndex = osmIds.length - 1;
        while (endIndex > startIndex + 1 && nOffset > 0) {
            endIndex--;
            nOffset -= distances[endIndex];
        }
        List<Long> res = new ArrayList<>();
        for (int i = startIndex; i <= endIndex; i++) {
            res.add(osmIds[i]);
        }
        return res;
    }

    private double[] lengthByRoute(OsrmMatching matching, double pOffset, double nOffset) {
        double[] go = null;
        // aggregate legs
        int nodeCount = 2;
        for (OsrmLeg leg : matching.getLegs()) {
            nodeCount += leg.getAnnotation().getNodes().length - 2;
        }
        long[] osmIds = new long[nodeCount];
        double[] distances = new double[nodeCount - 1];
        int idx = 2;
        for (OsrmLeg leg : matching.getLegs()) {
            idx -= 2;
            for (int i = 0; i < leg.getAnnotation().getNodes().length; i++) {
                if (osmIds[idx + i] == 0 || osmIds[idx + i] == leg.getAnnotation().getNodes()[i]) {
                    osmIds[idx + i] = leg.getAnnotation().getNodes()[i];
                } else {
                    // First two points of leg should be duplication of last two points from previous leg
                    // In case of U-Turn at tracepoint this is not the case. For OpenLR matching this is considered as invalid
                    LOGGER.warn("Osrm response misformed!");
                    return go;
                }
            }
            for (int i = 0; i < leg.getAnnotation().getDistance().length; i++) {
                distances[idx + i] += leg.getAnnotation().getDistance()[i];
            }
            idx += leg.getAnnotation().getNodes().length;
        }
        // consider positive offset
        int startIndex = 0;
        while (startIndex < distances.length - 1 && pOffset > 0) {
            pOffset -= distances[startIndex];
            startIndex++;
        }
        // consider negative offset
        int endIndex = osmIds.length - 1;
        while (endIndex > startIndex + 1 && nOffset > 0) {
            endIndex--;
            nOffset -= distances[endIndex];
        }
        if (endIndex > startIndex) {
            go = new double[endIndex - startIndex];
            int src = startIndex;
            for (int iGo = 0; iGo < go.length; iGo++) {
                go[iGo] = distances[src++];
            }
        }
        return go;
    }

    private double getPositiveOffset(RawLocationReference rawLocRef) {
        Offsets offsets = rawLocRef.getOffsets();
        if (offsets == null) {
            return 0;
        }
        List<LocationReferencePoint> points = rawLocRef.getLocationReferencePoints();
        int firstSegmentLength = points.get(0).getDistanceToNext();
        return offsets.getPositiveOffset(firstSegmentLength);
    }

    private double getNegativeOffset(RawLocationReference rawLocRef) {
        Offsets offsets = rawLocRef.getOffsets();
        if (offsets == null) {
            return 0;
        }
        List<LocationReferencePoint> points = rawLocRef.getLocationReferencePoints();
        int lastSegmentLength = points.get(points.size() - 2).getDistanceToNext();
        return offsets.getNegativeOffset(lastSegmentLength);
    }

    private boolean offsetExist(Offsets offsets) {
        if (offsets == null) {
            return false;
        }
        return offsets.getPositiveOffset(100) > 0 || offsets.getNegativeOffset(100) > 0;
    }

}
