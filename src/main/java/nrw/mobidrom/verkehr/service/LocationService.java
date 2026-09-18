package nrw.mobidrom.verkehr.service;

import net.postgis.jdbc.geometry.LineString;
import net.postgis.jdbc.geometry.Point;
import nrw.mobidrom.verkehr.service.api.geometry.XGeometryLocationReference;

public interface LocationService {
    XGeometryLocationReference createGeometryLinearLocation(int srid, LineString linestring);

    XGeometryLocationReference createGeometryLinearLocation(int srid, Point... points);

    XGeometryLocationReference createGeometryPointLocation(int srid, Point point);
}
