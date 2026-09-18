package nrw.mobidrom.verkehr.service.openlr;

import jakarta.enterprise.context.ApplicationScoped;
import nrw.mobidrom.verkehr.service.api.openlr.datex2.OpenlrExtendedLinear;
import nrw.mobidrom.verkehr.service.api.openlr.datex2.OpenlrLastLocationReferencePoint;
import nrw.mobidrom.verkehr.service.api.openlr.datex2.OpenlrLocationReferencePoint;
import nrw.mobidrom.verkehr.service.api.openlr.datex2.OpenlrOffsets;
import openlr.LocationReferencePoint;
import openlr.Offsets;
import openlr.binary.impl.LocationReferencePointBinaryImpl;
import openlr.binary.impl.OffsetsBinaryImpl;
import openlr.rawLocRef.RawLineLocRef;
import openlr.rawLocRef.RawLocationReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class OpenLRExtendedLinearConverter {

    public RawLocationReference toRawLocationReference(OpenlrExtendedLinear openlrExtendedLinear) {

        if (openlrExtendedLinear == null
                || openlrExtendedLinear.getFirstDirection() == null
                || openlrExtendedLinear.getFirstDirection().getOpenlrLocationReferencePoint() == null
                || openlrExtendedLinear.getFirstDirection().getOpenlrLastLocationReferencePoint() == null
                || openlrExtendedLinear.getFirstDirection().getOpenlrOffsets() == null) {
            throw new IllegalArgumentException("Invalid OpenLR Extended Linear request");
        }

        List<LocationReferencePoint> points = new ArrayList<>();

        List<OpenlrLocationReferencePoint> lrps =
                openlrExtendedLinear.getFirstDirection().getOpenlrLocationReferencePoint();

        int seq = 1;

        for (OpenlrLocationReferencePoint node : lrps) {

            LocationReferencePoint lrp =
                    new LocationReferencePointBinaryImpl(
                            seq++,
                            node.getOpenlrPathAttributes().getOpenlrLowestFRCToNextLRPoint(),
                            node.getOpenlrLineAttributes().getOpenlrFormOfWay(),
                            node.getOpenlrCoordinate().getLongitude(),
                            node.getOpenlrCoordinate().getLatitude(),
                            node.getOpenlrLineAttributes().getOpenlrBearing(),
                            node.getOpenlrPathAttributes().getOpenlrDistanceToNextLRPoint(),
                            node.getOpenlrPathAttributes().getOpenlrLowestFRCToNextLRPoint(),
                            false
                    );

            points.add(lrp);
        }

        OpenlrLastLocationReferencePoint last =
                openlrExtendedLinear.getFirstDirection().getOpenlrLastLocationReferencePoint();

        LocationReferencePoint lastPoint =
                new LocationReferencePointBinaryImpl(
                        seq,
                        last.getOpenlrLineAttributes().getOpenlrFunctionalRoadClass(),
                        last.getOpenlrLineAttributes().getOpenlrFormOfWay(),
                        last.getOpenlrCoordinate().getLongitude(),
                        last.getOpenlrCoordinate().getLatitude(),
                        last.getOpenlrLineAttributes().getOpenlrBearing(),
                        0,
                        null,
                        true
                );

        points.add(lastPoint);

        OpenlrOffsets offsetsNode =
                openlrExtendedLinear.getFirstDirection().getOpenlrOffsets();

        Offsets offsets =
                new OffsetsBinaryImpl(
                        offsetsNode.getOpenlrPositiveOffset(),
                        offsetsNode.getOpenlrNegativeOffset()
                );

        return new RawLineLocRef(
                UUID.randomUUID().toString(),
                points,
                offsets
        );
    }
}
