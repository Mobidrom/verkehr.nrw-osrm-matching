package nrw.mobidrom.verkehr.service.openlr;

import com.google.protobuf.ByteString;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRObject;
import openlr.rawLocRef.RawLocationReference;

/**
 * Tries to get geometry from openLR
 *
 * @author keko2055
 */
public interface IOpenLRDecoder {

    /**
     * @param openlr openLR code
     * @return object containing geometry and metadata
     */
    OpenLRObject decode(ByteString openlr);

    /**
     * decode openLR with respect to expected ("real") length
     *
     * @param openlrCode open location reference code
     * @return object containing geometry and metadata
     */
    OpenLRObject decode(ByteString openlrCode, Integer realLength);

    OpenLRObject decodeRawLocation(RawLocationReference rawLocation, Integer realLength);
}
