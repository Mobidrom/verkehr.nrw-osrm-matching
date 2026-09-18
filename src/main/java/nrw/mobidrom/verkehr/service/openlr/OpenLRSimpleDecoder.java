package nrw.mobidrom.verkehr.service.openlr;

import com.google.protobuf.ByteString;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRDecodingStatus;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRObject;
import openlr.LocationReferencePoint;
import openlr.map.FunctionalRoadClass;
import openlr.rawLocRef.RawLocationReference;

import java.util.Base64;
import java.util.List;

/**
 * This class is ...
 *
 * @author keko2055
 */
public class OpenLRSimpleDecoder implements IOpenLRDecoder {

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenLRObject decode(ByteString openlr) {
        OpenLRObject go = new OpenLRObject();
        if (openlr == null) {
            go.setStatus(OpenLRDecodingStatus.NO_DATA);
            return go;
        }
        go.setOpenlr(Base64.getEncoder().encodeToString(openlr.toByteArray()));
        RawLocationReference rawLocRef = DecoderHelper.decode(openlr);
        if (rawLocRef == null || rawLocRef.getLocationReferencePoints() == null) {
            go.setStatus(OpenLRDecodingStatus.BYTE_STRING_DECODING_ERROR);
            return go;
        }
        go.setValid(rawLocRef.isValid());
        FunctionalRoadClass frc = getHighestFRC(rawLocRef.getLocationReferencePoints());
        go.setFrc(frc.getID());
        return go;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenLRObject decode(ByteString openlr, Integer realLength) {
        return decode(openlr);
    }

    @Override
    public OpenLRObject decodeRawLocation(RawLocationReference rawLocation, Integer realLength) {
        return null;
    }

    private FunctionalRoadClass getHighestFRC(List<LocationReferencePoint> locationReferencePoints) {
        FunctionalRoadClass res = FunctionalRoadClass.getLowestFrc();
        for (LocationReferencePoint locationReferencePoint : locationReferencePoints) {
            if (locationReferencePoint.getFRC().compareTo(res) < 0) {
                res = locationReferencePoint.getFRC();
            }
        }
        return res;
    }

}
