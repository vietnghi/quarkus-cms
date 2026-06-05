package io.quarkiverse.cms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusIntegrationTest;

/**
 * Native integration test — verifies the basic CMS REST API works
 * when compiled to a GraalVM native image.
 */
@QuarkusIntegrationTest
class NativeContentIT {

    @Test
    void healthCheck() {
        given()
            .when().get("/q/health")
            .then()
            .statusCode(200);
    }

    @Test
    void listArticles() {
        given()
            .when().get("/api/articles")
            .then()
            .statusCode(200)
            .body("meta.pagination.page", is(1));
    }
}
