package nrw.mobidrom.verkehr;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import nrw.mobidrom.verkehr.service.impl.OpenLROsrmMatchingService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tester for OSRM matching service client
 *
 * @author olro2060
 */
@QuarkusTest
public class OpenLROsrmMatchingServiceTest {
    private static final String ASSERT_FALSE = Boolean.FALSE.toString() + ":";
    private static final String ALTERNATE_ROUTER = "http://localhost:5002";
    private static final String OPENLR_ROUTER = "http://localhost:5001";
    private static final String PROFILE = "driving";
    private static final String VERSION = "v1";

    @Inject
    private OpenLROsrmMatchingService matchingService;

    @Test
    public void testDistanceRouterHost() {
        String distanceRouterHost = matchingService.getDistanceRouterHost();

        assertNotNull(distanceRouterHost);
        assertEquals(ALTERNATE_ROUTER, distanceRouterHost.trim(), ASSERT_FALSE + distanceRouterHost);
    }

    @Test
    public void testRouterHost() {
        String routerHost = matchingService.getRouterHost();
        assertNotNull(routerHost);
        assertEquals(OPENLR_ROUTER, routerHost.trim(), ASSERT_FALSE + routerHost);
    }

    @Test
    public void testProfile() {
        String profile = matchingService.getProfile();
        assertNotNull(profile);
        assertEquals(PROFILE, profile, ASSERT_FALSE + profile);
    }

    @Test
    public void testVersion() {
        String version = matchingService.getVersion();
        assertNotNull(version);
        assertEquals(VERSION, version, ASSERT_FALSE + version);
    }

}
