package nrw.mobidrom.verkehr;

import io.quarkus.test.junit.QuarkusTest;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRMatchingParameter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tester for provision of parameter for decoding of open-LR codes by matching
 *
 * @author olro2060
 */
@QuarkusTest
public class OpenLRMatchingParametersTest {
    private static final int MAX_RADIUS = 20;
    private static final int BEARING_TOLERANCE = 30;
    private static final String ASSERT_FALSE = Boolean.FALSE + ":";

    @Test
    public void testParameterDefault() {
        OpenLRMatchingParameter parameter = new OpenLRMatchingParameter(false, 0, 0, true);
        assertNotNull(parameter, ASSERT_FALSE + null);
        assertTrue(parameter.isIgnoreGaps(), ASSERT_FALSE);
        assertTrue(parameter.isUseTimestampParameter(), ASSERT_FALSE);
        assertFalse(parameter.isIgnoreLastBearing(), ASSERT_FALSE);
        assertTrue(parameter.isOpenlrRouter(), ASSERT_FALSE);
        assertEquals(BEARING_TOLERANCE, parameter.getBearingTolerance(), ASSERT_FALSE);
        assertEquals(0, parameter.getMaxRadius(), ASSERT_FALSE);
    }

    @Test
    public void testParameterAlternate() {
        OpenLRMatchingParameter parameter = new OpenLRMatchingParameter(true, MAX_RADIUS, MAX_RADIUS, false);
        assertNotNull(parameter, ASSERT_FALSE + null);
        assertTrue(parameter.isIgnoreGaps(), ASSERT_FALSE);
        assertTrue(parameter.isUseTimestampParameter(), ASSERT_FALSE);
        assertTrue(parameter.isIgnoreLastBearing(), ASSERT_FALSE);
        assertFalse(parameter.isOpenlrRouter(), ASSERT_FALSE);
        assertEquals(BEARING_TOLERANCE, parameter.getBearingTolerance(), ASSERT_FALSE);
        assertEquals(MAX_RADIUS, parameter.getMaxRadius(), ASSERT_FALSE);
    }

}
