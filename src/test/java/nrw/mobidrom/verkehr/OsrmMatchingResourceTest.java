package nrw.mobidrom.verkehr;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import net.postgis.jdbc.geometry.LineString;
import net.postgis.jdbc.geometry.Point;
import nrw.mobidrom.verkehr.entity.cache.OpenLRCache;
import nrw.mobidrom.verkehr.helper.OsrmMatchingConstants;
import nrw.mobidrom.verkehr.service.api.openlr.OpenLRDecodingStatus;
import nrw.mobidrom.verkehr.service.impl.OpenLROsrmMatchingService;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.SQLException;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OsrmMatchingResourceTest {

    private static final String JSON_ROOT = "$";
    private static final String STATUS = "status";
    private static final String LENGTH = "length";
    private static final String OPENLR = "openlr";
    private static final String FRC = "frc";
    private static final String OSM_ID_LIST = "osmIdList";
    private static final String OSM_IDS_SIZE = "osmIds.size()";
    private static final String LENGTHS = "lengths.size()";
    private static final String LINE_STRING = "lineString";
    private static final String POINT = "point";
    private static final String TEST_POINT = "SRID=4326;POINT(6.5051255 51.4163196)";
    public static final String OSRM_MATCH_URL = "/match/v1/driving/.*";
    public static final String OSRM_NEAREST_URL = "/nearest/v1/driving/.*";

    @Inject
    private OpenLROsrmMatchingService openLRService;

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

    @BeforeEach
    void stubs() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching(OSRM_MATCH_URL))
                .willReturn(WireMock.aResponse().withStatus(Response.Status.OK.getStatusCode())
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("osrm_match_response.json")));
        wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching(OSRM_NEAREST_URL))
                .willReturn(WireMock.aResponse().withStatus(Response.Status.OK.getStatusCode())
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("osrm_nearest_response.json")));
    }

    @Test
    void testMatchOsmIds() throws SQLException {
        LineString ls = new LineString(
                "SRID=4326;LINESTRING(6.5051255 51.4163196,6.50521 51.41628,6.5052543 51.4163176)");
        given().contentType(ContentType.JSON)
                .body(toJson(ls))
                .when()
                .post(OsrmMatchingConstants.MATCH_OSM_IDS)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(JSON_ROOT, Matchers.hasSize(greaterThanZero()));
    }

    @Test
    void testMatchOsmIdTypeMismatch() throws SQLException {
        given().contentType(ContentType.JSON)
                .body(toJson(new Point(TEST_POINT)))
                .when()
                .post(OsrmMatchingConstants.MATCH_OSM_IDS)
                .then()
                .statusCode(400);
    }

    @Test
    void testMatchOsmIdEmpty() {
        given().contentType(ContentType.JSON)
                .body(toJson(OsrmMatchingConstants.EMPTY))
                .when()
                .post(OsrmMatchingConstants.MATCH_OSM_IDS)
                .then()
                .statusCode(400);
    }

    @Test
    void testMatchOsmIdException() {
        stubInvalidOsrmResponse();
        given().contentType(ContentType.JSON)
                .body(toJson("SRID=4326;LINESTRING(51.6338365 7.027082,51.6338345 7.0277095)"))
                .when()
                .post(OsrmMatchingConstants.MATCH_OSM_IDS)
                .then()
                .statusCode(400)
                .body(Matchers.equalTo(OsrmMatchingConstants.NO_MATCHING_SEGMENT));
    }

    @Test
    void testNearestOsmIds() throws SQLException {
        given().contentType(ContentType.JSON)
                .body(toJson(new Point(TEST_POINT)))
                .when()
                .post(OsrmMatchingConstants.NEAREST_OSM_IDS)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(JSON_ROOT, Matchers.hasSize(Matchers.greaterThan(5)));
    }

    @Test
    void testNearestOsmIdsEmpty() throws SQLException {
        given().contentType(ContentType.JSON)
                .body(toJson(OsrmMatchingConstants.EMPTY))
                .when()
                .post(OsrmMatchingConstants.NEAREST_OSM_IDS)
                .then()
                .statusCode(400);
    }

    @Test
    void testGeometry() throws SQLException {
        LineString ls = new LineString(
                "SRID=4326;LINESTRING(7.9921063 50.9500889,7.99228 50.94995,7.99256 50.94976,7.99291 50.94957,7.99323 50.94942," +
                        "7.99359 50.94928,7.99401 50.94916,7.99443 50.94905,7.99494 50.94892,7.99664 50.94854,7.99741 50.94836," +
                        "7.99795 50.94824,7.99835 50.94815,7.99854 50.9481,7.99912 50.94796,7.99959 50.94784,7.99983 50.94779,8" +
                        ".00004 50.94775,8.00033 50.94769,8.00056 50.94763,8.0008 50.94757,8.00092 50.94754,8.00106 50.9475,8" +
                        ".00133 50.94741,8.00172 50.94728,8.00201 50.94714,8.00218 50.94704,8.00229 50.94698,8.00256 50.94682,8" +
                        ".00284 50.94663,8.00312 50.94641,8.00339 50.94617,8.00369 50.94588,8.00391 50.94565,8.00415 50.94538,8" +
                        ".00443 50.945,8.00462 50.94471,8.00476 50.94445,8.00493 50.94413,8.00507 50.94383,8.00517 50.94353,8" +
                        ".0051897 50.9434578)");
        given().contentType(ContentType.JSON)
                .when()
                .body(toJson(ls))
                .post(OsrmMatchingConstants.GEOMETRY)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(LINE_STRING, Matchers.equalTo(ls.toString()))
                .body(OSM_IDS_SIZE, greaterThanZero());
    }

    @Test
    void testGeometryEmpty() throws SQLException {
        given().contentType(ContentType.JSON)
                .when()
                .body(toJson(OsrmMatchingConstants.EMPTY))
                .post(OsrmMatchingConstants.GEOMETRY)
                .then()
                .statusCode(400);
    }

    @Test
    void testGeometryPoint() throws SQLException {
        Point p = new Point("SRID=4326;POINT(8.29601 51.6836)");
        given().contentType(ContentType.JSON)
                .when()
                .body(toJson(p))
                .post(OsrmMatchingConstants.GEOMETRY)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(POINT, Matchers.equalTo(p.toString()))
                .body(OSM_IDS_SIZE, greaterThanZero());
    }

    @Test
    void testGeometryPointEmpty() throws SQLException {
        given().contentType(ContentType.JSON)
                .when()
                .body(toJson(OsrmMatchingConstants.EMPTY))
                .post(OsrmMatchingConstants.GEOMETRY)
                .then()
                .statusCode(400);
    }

    @Test
    void testOpenlr() {
        String base64 = "CwW7ICSuhCOaNw76/lYjiyIF6gJaIxA=";
        given().contentType(ContentType.JSON)
                .when()
                .body(toJson(base64))
                .post(OsrmMatchingConstants.OPENLR)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(FRC, Matchers.equalTo(4))
                .body(OPENLR, Matchers.equalTo(base64))
                .body(OSM_ID_LIST, Matchers.hasSize(greaterThanZero()))
                .body(LENGTH, Matchers.greaterThan(5000F))
                .body(LENGTHS, greaterThanZero());

        OpenLRCache fromCache = openLRService.getFromCache(base64, 0);
        Assertions.assertNotNull(fromCache);
        Assertions.assertNotNull(fromCache.getJsonResult());
        Assertions.assertEquals(4, fromCache.getJsonResult().getFrc());
    }

    @Test
    void testOpenlrEmpty() {
        given().contentType(ContentType.JSON)
                .when()
                .body(toJson(OsrmMatchingConstants.EMPTY))
                .post(OsrmMatchingConstants.OPENLR)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(FRC, Matchers.equalTo(0))
                .body(OPENLR, Matchers.equalTo(OsrmMatchingConstants.EMPTY))
                .body(LENGTH, Matchers.equalTo(0F))
                .body(STATUS, Matchers.equalTo(OpenLRDecodingStatus.NO_DATA.name()));
    }

    @Test
    void testOpenlrWeirdBase64() {
        stubInvalidOsrmResponse();
        String base64 = "CwW2ICSuhCOaNw76/lYjiyIF6gJaIxA=";
        given().contentType(ContentType.JSON)
                .when()
                .body(toJson(base64))
                .post(OsrmMatchingConstants.OPENLR)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(FRC, Matchers.equalTo(4))
                .body(OPENLR, Matchers.equalTo(base64))
                .body(OSM_ID_LIST, Matchers.nullValue())
                .body(LENGTH, Matchers.equalTo(0F))
                .body(STATUS, Matchers.equalTo(OpenLRDecodingStatus.OSRM_MATCHING_FAILED.name()));
    }

    private void stubInvalidOsrmResponse() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching(OSRM_MATCH_URL))
                .willReturn(WireMock.aResponse().withStatus(Response.Status.OK.getStatusCode())
                        .withBodyFile("osrm_invalid_response.json")));
    }

    private Matcher<Integer> greaterThanZero() {
        return Matchers.greaterThan(0);
    }

    private String toJson(Object obj) {
        return "\"" + obj.toString() + "\"";
    }
}
