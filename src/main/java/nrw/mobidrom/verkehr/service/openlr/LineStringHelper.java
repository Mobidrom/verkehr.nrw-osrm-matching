package nrw.mobidrom.verkehr.service.openlr;

import net.postgis.jdbc.geometry.LineString;
import net.postgis.jdbc.geometry.Point;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;

import java.util.ArrayList;

/**
 * Collection of helping methods
 *
 * @author keko2055
 */
public final class LineStringHelper {
    private static final Geodesic GD = Geodesic.WGS84;

    private LineStringHelper() {
    }

    public static Point intersect(LineString line, double offset) {
        if (offset <= 0) {
            return line.getFirstPoint();
        }
        if (line == null || line.numPoints() == 0) {
            // TODO LOG
            return null;
        }
        Point pFrom = line.getPoint(0);

        for (int i = 1; i < line.numPoints(); i++) {
            Point pTo = line.getPoint(i);
            if (offset <= 0) {
                return pTo;
            }
            GeodesicData inverse = GD.Inverse(pFrom.y, pFrom.x, pTo.y, pTo.x);
            double distance = inverse.s12;
            if (distance > offset) {
                return intersect(pFrom, pTo, offset);
            }
            offset = offset - distance;
            pFrom = pTo;
        }
        return line.getLastPoint();

    }

    public static Point intersect(Point pFrom, Point pTo, double offset) {
        if (offset <= 0) {
            return pFrom;
        }
        GeodesicData inverse = GD.Inverse(pFrom.y, pFrom.x, pTo.y, pTo.x);
        double distance = inverse.s12;
        if (offset >= distance) {
            return pTo;
        }
        double f = offset / distance;
        double x = pFrom.x + f * (pTo.x - pFrom.x);
        double y = pFrom.y + f * (pTo.y - pFrom.y);
        return new Point(x, y);
    }

    public static LineString reduceLine(LineString geometry, double pOffset, double nOffset) {
        LineString reducedLine = reduceLine(geometry, pOffset);
        reducedLine = reduceLineNegativ(reducedLine, nOffset);
        return reducedLine;
    }

    public static LineString reverseLine(LineString line) {
        if (line == null) {
            return null;
        }
        int size = line.numPoints();
        Point[] points = new Point[size];
        for (int i = 0; i < size; i++) {
            points[i] = line.getPoint(size - i - 1);
        }
        return new LineString(points);
    }

    public static Point intersectReverse(LineString line, double offset) {
        LineString tmp = reverseLine(line);
        return intersect(tmp, offset);
    }

    private static LineString reduceLine(LineString line, double positivOffset) {
        ArrayList<Point> resultList = new ArrayList<>();
        if (positivOffset <= 0) {
            return line;
        }
        if (line == null || line.numPoints() == 0) {
            // TODO LOG
            return line;
        }
        Point pFrom = line.getPoint(0);

        for (int i = 1; i < line.numPoints(); i++) {
            Point pTo = line.getPoint(i);
            if (positivOffset <= 0) {
                resultList.add(pTo);
            } else {
                GeodesicData inverse = GD.Inverse(pFrom.y, pFrom.x, pTo.y, pTo.x);
                double distance = inverse.s12;
                if (distance >= positivOffset) {
                    resultList.add(LineStringHelper.intersect(pFrom, pTo, positivOffset));
                    resultList.add(pTo);
                }
                positivOffset = positivOffset - distance;
            }
            pFrom = pTo;
        }
        return new LineString(resultList.toArray(new Point[0]));
    }

    private static LineString reduceLineNegativ(LineString line, double offset) {
        if (offset <= 0) {
            return line;
        }
        LineString tmp = LineStringHelper.reverseLine(line);
        LineString reducedLine = reduceLine(tmp, offset);
        return LineStringHelper.reverseLine(reducedLine);
    }

}
