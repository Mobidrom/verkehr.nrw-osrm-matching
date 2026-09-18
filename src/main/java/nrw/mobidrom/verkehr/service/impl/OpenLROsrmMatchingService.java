package nrw.mobidrom.verkehr.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.Getter;
import nrw.mobidrom.verkehr.entity.cache.OpenLRCache;
import nrw.mobidrom.verkehr.repository.OpenLRCacheDao;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRMatchingParameter;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRObject;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLROsrmMatching;
import nrw.mobidrom.verkehr.service.osrm.client.OsrmMatchServiceClient;
import openlr.LocationReferencePoint;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

/**
 * Handler for OSRM matching service calls.
 *
 * @author anqu2052
 */
@ApplicationScoped
@Getter
public class OpenLROsrmMatchingService {

    @ConfigProperty(name = "osrm.openlr.router.host")
    private String routerHost;

    @ConfigProperty(name = "osrm.distance.router.host")
    private String distanceRouterHost;

    @ConfigProperty(name = "osrm.openlr.router.version", defaultValue = "v1")
    private String version;

    @ConfigProperty(name = "osrm.openlr.router.profile", defaultValue = "driving")
    private String profile;

    @Inject
    private OpenLRCacheDao openLRCacheDao;

    public OpenLROsrmMatching getMatching(List<LocationReferencePoint> lrp, OpenLRMatchingParameter parameter) {
        String host = parameter.isOpenlrRouter() ? routerHost : distanceRouterHost;
        OsrmMatchServiceClient client = new OsrmMatchServiceClient(host, version, profile);
        return new OpenLROsrmMatching(lrp, parameter, client);
    }

    public OpenLRCache getFromCache(String openLRBase64, int realLength) {
        return openLRCacheDao.getFromCache(openLRBase64, realLength);
    }

    @Transactional
    public void persist(String openLRBase64, int realLength, OpenLRObject openLRObject, OpenLRCache fromCache) {
        openLRCacheDao.persist(openLRBase64, realLength, openLRObject, fromCache);
    }
}
