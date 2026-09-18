package nrw.mobidrom.verkehr.service.api.geometry;

import lombok.Getter;
import lombok.Setter;
import net.postgis.jdbc.geometry.LineString;
import net.postgis.jdbc.geometry.Point;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class XGeometryLocationReference {

    private List<Long> osmIds;

    private LineString lineString;

    private Point point;

    public List<Long> getOsmIds() {
        if (osmIds == null) {
            osmIds = new ArrayList<>();
        }
        return osmIds;
    }

}
