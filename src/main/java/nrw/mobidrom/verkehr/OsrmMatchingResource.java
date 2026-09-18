package nrw.mobidrom.verkehr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.postgis.jdbc.geometry.Geometry;
import net.postgis.jdbc.geometry.LineString;
import net.postgis.jdbc.geometry.Point;
import nrw.mobidrom.verkehr.entity.cache.OpenLRCache;
import nrw.mobidrom.verkehr.helper.OsrmMatchingConstants;
import nrw.mobidrom.verkehr.service.LocationService;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRObject;
import nrw.mobidrom.verkehr.service.api.openlr.datex2.OpenlrExtendedLinear;
import nrw.mobidrom.verkehr.service.impl.OpenLROsrmMatchingService;
import nrw.mobidrom.verkehr.service.openlr.IOpenLRDecoder;
import nrw.mobidrom.verkehr.service.openlr.OpenLRExtendedLinearConverter;
import nrw.mobidrom.verkehr.service.openlr.OpenLROsrmMatchingDecoder;
import nrw.mobidrom.verkehr.service.osrm.OsrmRoutingService;
import openlr.rawLocRef.RawLocationReference;
import org.jboss.logging.Logger;

import java.util.Base64;

@Path(OsrmMatchingConstants.ROOT_PATH)
public class OsrmMatchingResource {

    private static final Logger LOGGER = Logger.getLogger(OsrmMatchingResource.class);

    @Inject
    private OpenLROsrmMatchingService openLRService;

    @Inject
    private LocationService locationService;

    @Inject
    private OsrmRoutingService osrmRoutingService;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    OpenLRExtendedLinearConverter converter;

    @POST
    @Path(OsrmMatchingConstants.MATCH_OSM_IDS)
    @Produces(MediaType.APPLICATION_JSON)
    public Response matchOsmIds(Geometry geometry) {
        try {
            if (!(geometry instanceof LineString linestring)) {
                throw new Exception("Invalid type " + geometry.getClass());
            }
            return Response.ok(osrmRoutingService.matchOsmIds(linestring))
                    .build();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            return Response.status(400)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path(OsrmMatchingConstants.NEAREST_OSM_IDS)
    @Produces(MediaType.APPLICATION_JSON)
    public Response nearest(Geometry geometry, @QueryParam(value = OsrmMatchingConstants.NUMBER) @DefaultValue(
            value = OsrmMatchingConstants.FIVE) Integer number) {
        try {
            if (!(geometry instanceof Point point)) {
                throw new Exception("Invalid type " + geometry.getClass());
            }
            return Response.ok(osrmRoutingService.nearestOsmIds(point, number))
                    .build();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            return Response.status(400)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path(OsrmMatchingConstants.GEOMETRY)
    @Produces(MediaType.APPLICATION_JSON)
    public Response geometry(Geometry geometry, @QueryParam(value = OsrmMatchingConstants.SRID) @DefaultValue(
            value = OsrmMatchingConstants.DEFAULT_SRID) Integer srid) {
        try {
            if (geometry instanceof Point point) {
                return Response.ok(locationService.createGeometryLinearLocation(srid, point))
                        .build();
            } else if (geometry instanceof LineString lineString) {
                return Response.ok(locationService.createGeometryLinearLocation(srid, lineString))
                        .build();
            }
            throw new Exception("Invalid type " + geometry.getClass());
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            return Response.status(400)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path(OsrmMatchingConstants.OPENLR)
    @Produces(MediaType.APPLICATION_JSON)
    public Response openlr(String base64,
                           @QueryParam(value = OsrmMatchingConstants.REAL_LENGTH)
                           @DefaultValue(value = OsrmMatchingConstants.ZERO) Integer realLength,
                           @QueryParam(value = OsrmMatchingConstants.FORCE_RECALCULATE) @DefaultValue(value = "false")
                           Boolean forceRecalculate) {
        try {
            String correctedBase64 = replaceIllegalCharacters(base64);
            OpenLRCache fromCache = openLRService.getFromCache(correctedBase64, realLength);
            if (!forceRecalculate && fromCache != null) {
                return Response.ok(fromCache.getJsonResult()).build();
            }

            ByteString copyFrom = ByteString.copyFrom(Base64.getDecoder().decode(correctedBase64));
            IOpenLRDecoder decoder = new OpenLROsrmMatchingDecoder(openLRService);
            OpenLRObject openlrObject = decoder.decode(copyFrom, realLength);
            openLRService.persist(correctedBase64, realLength, openlrObject, fromCache);
            return Response.ok(openlrObject).build();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            return Response.status(400)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path(OsrmMatchingConstants.OPENLR_EXTENDED_LINEAR)
    @Produces(MediaType.APPLICATION_JSON)
    public Response openlrExtendedLinear(OpenlrExtendedLinear openlrExtendedLinear,
                                         @QueryParam(value = OsrmMatchingConstants.REAL_LENGTH)
                                         @DefaultValue(value = OsrmMatchingConstants.ZERO) Integer realLength,
                                         @QueryParam(value = OsrmMatchingConstants.FORCE_RECALCULATE)
                                         @DefaultValue(value = "false") Boolean forceRecalculate) {
        try {

            String cacheKey = objectMapper.writeValueAsString(openlrExtendedLinear);
            OpenLRCache fromCache = openLRService.getFromCache(cacheKey, realLength);

            if (!forceRecalculate && fromCache != null) {
                return Response.ok(fromCache.getJsonResult()).build();
            }

            RawLocationReference raw = converter.toRawLocationReference(openlrExtendedLinear);
            IOpenLRDecoder decoder = new OpenLROsrmMatchingDecoder(openLRService);
            OpenLRObject openlrObject = decoder.decodeRawLocation(raw, realLength);
            openLRService.persist(cacheKey, realLength, openlrObject, fromCache);
            return Response.ok(openlrObject).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    /**
     * replaces all " and ' from the base64 String, since old API would sometimes have them in the body of the openLR code
     *
     * @param base64 the base64 to replace illegal characters in
     * @return valid base64 string
     */
    private String replaceIllegalCharacters(String base64) {
        if (base64 == null) {
            return null;
        }
        return base64.replace("\"", "").replace("'", "");
    }
}
