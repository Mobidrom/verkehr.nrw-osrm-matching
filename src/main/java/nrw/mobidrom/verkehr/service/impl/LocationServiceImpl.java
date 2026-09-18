package nrw.mobidrom.verkehr.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.postgis.jdbc.geometry.LineString;
import net.postgis.jdbc.geometry.Point;
import nrw.mobidrom.verkehr.service.LocationService;
import nrw.mobidrom.verkehr.service.api.geometry.XGeometryLocationReference;
import nrw.mobidrom.verkehr.service.osrm.OsrmRoutingService;
import nrw.mobidrom.verkehr.service.osrm.OsrmServiceException;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class LocationServiceImpl implements LocationService {

    private static final Logger LOGGER = Logger.getLogger(LocationServiceImpl.class);

    @Inject
    private OsrmRoutingService routingService;

    @Override
    public XGeometryLocationReference createGeometryLinearLocation(int srid, LineString linestring) {
        return createGeometryLinearLocation(srid, linestring.getPoints());
    }

    @Override
    public XGeometryLocationReference createGeometryLinearLocation(int srid, Point... points) {
        if (points.length == 1) {
            return createGeometryPointLocation(srid, points[0]);
        }
        XGeometryLocationReference res = new XGeometryLocationReference();
        LineString ls = new LineString(points);
        ls.setSrid(srid);
        res.setLineString(ls);
        try {
            List<Long> osmIds = routingService.matchOsmIds(ls);
            if (osmIds != null) {
                res.getOsmIds()
                        .addAll(osmIds);
            }
        } catch (OsrmServiceException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return res;
    }

    @Override
    public XGeometryLocationReference createGeometryPointLocation(int srid, Point point) {
        XGeometryLocationReference res = new XGeometryLocationReference();
        Point p = new Point(point.x, point.y);
        p.setSrid(srid);
        res.setPoint(p);
        try {
            List<Long> osmIds = routingService.nearestOsmIds(point);
            if (osmIds != null) {
                res.getOsmIds()
                        .addAll(osmIds);
            }
        } catch (OsrmServiceException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return res;
    }
}
