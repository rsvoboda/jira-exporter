package io.quarkus.qe.metrics;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class MetricsEndpointTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/metrics")
          .then()
             .statusCode(200);
    }
}