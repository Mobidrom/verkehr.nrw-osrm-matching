package nrw.mobidrom.verkehr.service.api.openlr;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.postgis.jdbc.geometry.LineString;

import java.util.List;

/**
 * This class is ...
 *
 * @author keko2055
 */
@Setter
@Getter
@ToString
public class OpenLRObject {

    // private ByteString openlr;
    private String openlr;

    private LineString geometry;

    private List<Long> osmIdList;

    // Is set to false if the invalid flag is set in the openLR code
    private boolean valid = true;

    // Indicates Decoding Problems
    private OpenLRDecodingStatus status = OpenLRDecodingStatus.OK;

    // The algorithm tries to validate the result of decoding. Possible methods:
    // Compare length of result geometry with the expected value
    // Compare roadclass or formOfRoad of result with expected value (not
    // implemented)
    private boolean plausible = true;

    // Functional roadClass
    private int frc;

    private String queryString;

    // //Flag to indicate that geometry is not calculated because of filter
    // settings
    // private boolean passedFilter = true;
    //
    // //Flag to indicate that the matching algorithm failed
    // private boolean matchingSucceded = true;

    // Extended parameters for
    // Tracepoints from osrm response
    private LineString tracePoints;

    // Calculated geometry length
    private double length;

    // maximum distance of trace points
    private long traceMaxDistance;

    private double[] lengths;
    private double[] bearings;
}
