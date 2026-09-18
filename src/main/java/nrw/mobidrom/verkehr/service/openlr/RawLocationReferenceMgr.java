package nrw.mobidrom.verkehr.service.openlr;

import net.postgis.jdbc.geometry.LineString;
import net.postgis.jdbc.geometry.Point;
import openlr.LocationReferencePoint;
import openlr.map.FunctionalRoadClass;
import openlr.rawLocRef.RawLocationReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Perform OR mapping for open-LR
 *
 * @author olro2060
 */
public class RawLocationReferenceMgr {
    public static final int SRID_WGS_84 = 4326;
    private boolean valid;
    private FunctionalRoadClass lowFrc;
    private LineString geometry;

    public RawLocationReferenceMgr(RawLocationReference rawLocRef) {
        valid = false;
        if (rawLocRef != null) {
            List<LocationReferencePoint> lLrp = rawLocRef.getLocationReferencePoints();
            if (lLrp != null && lLrp.size() > 1) {
                runFrc(lLrp);
                runGeo(lLrp);
                valid = true;
            }
        }
    }

    public int getFrc() {
        return lowFrc.getID();
    }

    public LineString getGeometry() {
        return geometry;
    }

    public boolean isValid() {
        return valid;
    }

    private void runFrc(List<LocationReferencePoint> lLrp) {
        // from FRC_0 (main) = highest to FRC_7 (other) = lowest 
        lowFrc = FunctionalRoadClass.getHighestFrc();
        // do not check the latest reference point in list
        for (int idx = lLrp.size() - 2; idx >= 0; idx--) {
            FunctionalRoadClass lFrc = lLrp.get(idx).getLfrc();
            // ordinal value of enum FRC greater than -> lower importance
            if (lFrc.compareTo(lowFrc) > 0) {
                lowFrc = lFrc;
            }
        }
    }

    private void runGeo(List<LocationReferencePoint> lLrp) {
        List<Point> points = new ArrayList<Point>();
        for (LocationReferencePoint lrp : lLrp) {
            double dx = lrp.getLongitudeDeg();
            double dy = lrp.getLatitudeDeg();
            points.add(new Point(dx, dy));
        }
        geometry = new LineString(points.toArray(new Point[points.size()]));
        geometry.setSrid(SRID_WGS_84);
    }

}
