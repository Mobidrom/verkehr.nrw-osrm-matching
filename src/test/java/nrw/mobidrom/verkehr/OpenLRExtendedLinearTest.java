package nrw.mobidrom.verkehr;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import nrw.mobidrom.verkehr.helper.OsrmMatchingConstants;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.restassured.RestAssured.given;
import static io.smallrye.common.constraint.Assert.assertFalse;
import static io.smallrye.common.constraint.Assert.assertNotNull;
import static io.smallrye.common.constraint.Assert.assertTrue;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenLRExtendedLinearTest {

    private WireMockServer wireMockServer;

    @BeforeAll
    void setup() {
        wireMockServer = new WireMockServer(5001);
        wireMockServer.start();
    }

    @AfterAll
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void shouldDecodeExtendedLinear() {
        wireMockServer.stubFor(WireMock.get(WireMock.anyUrl())
                .willReturn(WireMock.aResponse().withStatus(Response.Status.OK.getStatusCode())
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("osrm_extended_linear.json")));

        String json = """
                {
                  "firstDirection": {
                    "openlrLocationReferencePoint": [
                      {
                        "openlrCoordinate": {
                          "latitude": 51.227741,
                          "longitude": 6.773456
                        },
                        "openlrLineAttributes": {
                          "openlrFunctionalRoadClass": "FRC_3",
                          "openlrFormOfWay": "SINGLE_CARRIAGEWAY",
                          "openlrBearing": 90
                        },
                        "openlrPathAttributes": {
                          "openlrLowestFRCToNextLRPoint": "FRC_3",
                          "openlrDistanceToNextLRPoint": 100
                        }
                      }
                    ],
                    "openlrLastLocationReferencePoint": {
                      "openlrCoordinate": {
                        "latitude": 51.228500,
                        "longitude": 6.774000
                      },
                      "openlrLineAttributes": {
                        "openlrFunctionalRoadClass": "FRC_3",
                        "openlrFormOfWay": "SINGLE_CARRIAGEWAY",
                        "openlrBearing": 180
                      }
                    },
                    "openlrOffsets": {
                      "openlrPositiveOffset": 0,
                      "openlrNegativeOffset": 0
                    }
                  }
                }
                """;

        OpenLRObject result =
                given()
                        .contentType(ContentType.JSON)
                        .body(json)
                        .when()
                        .post(OsrmMatchingConstants.OPENLR_EXTENDED_LINEAR)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(OpenLRObject.class);

        assertNotNull(result);
        assertNotNull(result.getStatus());

        assertNotNull(result.getGeometry());
        assertNotNull(result.getOsmIdList());
        assertFalse(result.getOsmIdList().isEmpty());

        assertTrue(result.getLength() > 0);
    }

    @Test
    void shouldReturnBadRequestForInvalidExtendedLinear() {
        String json = "{}";

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post(OsrmMatchingConstants.OPENLR_EXTENDED_LINEAR)
                .then()
                .statusCode(400);
    }
}
