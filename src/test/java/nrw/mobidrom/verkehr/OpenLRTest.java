package nrw.mobidrom.verkehr;

import com.google.protobuf.ByteString;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRMatchingParameter;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRMatchingParameters;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRObject;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLROsrmMatching;
import nrw.mobidrom.verkehr.service.openlr.DecoderHelper;
import nrw.mobidrom.verkehr.service.openlr.IOpenLRDecoder;
import nrw.mobidrom.verkehr.service.openlr.OpenLRSimpleDecoder;
import nrw.mobidrom.verkehr.service.osrm.client.OsrmMatchServiceClient;
import openlr.LocationReferencePoint;
import openlr.Offsets;
import openlr.rawLocRef.RawLocationReference;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class OpenLRTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenLRTest.class);

    private static final int INPLAUSIBLE = 0;
    private static final int OK = 1;
    private static final int OK_BAD = 2;
    private static final String COMMA = ",";
    private static final String CSV = ";";
    private static final String LINE = "\n";

    private static IOpenLRDecoder decoder;

    public static void main(String[] args) {
        BasicConfigurator.configure(new ConsoleAppender(
                new PatternLayout("%d{yyyy/MM/dd - HH:mm:ss.SSS} %p %c: - %m%n")));
        decoder = new OpenLRSimpleDecoder();
        //        decoder = new OpenLROsrmMatchingDecoder(matchingService);
        testDecode();
    }

    public static void testDecode() {
        String base64 = "CwTK4iSTggEYMvAdABkBCA==";
        //        String base64 = "CwTEYSSTjgEIPxQiAHEBFQ==";

        RawLocationReference decodeRaw = decodeRaw(ByteString.copyFrom(Base64.decodeBase64(base64)));

        List<OpenLRMatchingParameter> parameterlist = OpenLRMatchingParameters.getParameterlist();
        for (OpenLRMatchingParameter parameter : parameterlist) {
            String host = parameter.isOpenlrRouter() ? "http://localhost:5001" : "http://localhost:5002";
            OsrmMatchServiceClient client = new OsrmMatchServiceClient(host, "v1", "driving");
            OpenLROsrmMatching openLROsrmMatching =
                    new OpenLROsrmMatching(decodeRaw.getLocationReferencePoints(), parameter, client);
            System.out.println(openLROsrmMatching.getRouting());
        }
    }

    private static RawLocationReference decodeRaw(ByteString openlrCode) {
        return openlrCode == null ? null : DecoderHelper.decode(openlrCode);
    }

    private static SortedMap<String, String> decode(List<String> b64L) {
        SortedMap<String, String> go = new TreeMap<String, String>();
        for (String b64 : b64L) {
            byte[] openlrByteArray = Base64.decodeBase64(b64);
            ByteString openlrByteString = ByteString.copyFrom(openlrByteArray);
            RawLocationReference rawLocRef = DecoderHelper.decode(openlrByteString);
            Offsets offsets = rawLocRef.getOffsets();
            List<LocationReferencePoint> locationReferencePointList = rawLocRef.getLocationReferencePoints();
            int sizeWork = locationReferencePointList.size() - 1;
            int lenRLR = 0;
            int positiveOffset = 0;
            int negativeOffset = 0;
            for (int idx = 0; idx < sizeWork; idx++) {
                LocationReferencePoint lrp = locationReferencePointList.get(idx);
                int length = lrp.getDistanceToNext();
                if (idx == 0 && offsets != null) {
                    positiveOffset = offsets.getPositiveOffset(length);
                }
                if (idx == sizeWork - 1 && offsets != null) {
                    negativeOffset = offsets.getNegativeOffset(length);
                }
                lenRLR += length;
            }
            OpenLRObject openlrObject = decoder.decode(openlrByteString, lenRLR);
            Double lenD = openlrObject.getLength();
            Long lenOSRM = lenD == null ? 0L : Math.round(lenD);
            Long lenERR = Math.abs(lenRLR - lenOSRM);
            StringBuffer sb = new StringBuffer();
            sb.append(openlrObject.getStatus().name()).append(CSV).append(positiveOffset).append(CSV);
            sb.append(negativeOffset).append(CSV).append(lenRLR).append(CSV).append(lenOSRM.toString()).append(CSV);
            sb.append(lenERR.toString()).append(CSV).append(openlrObject.getTraceMaxDistance());
            go.put(b64, sb.toString());
        }
        return go;
    }
}
