package io.quarkiverse.cms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class RelationsTest {
    @Test void testListEntries() {
        given().when().get("/api/articles").then().statusCode(200)
            .body("meta.pagination.total", greaterThanOrEqualTo(0));
    }
    @Test void testComponentRoundTrip() {
        String title = "CompTest-" + System.currentTimeMillis();
        given().contentType(ContentType.JSON)
            .body("{\"title\":\"" + title + "\",\"seo\":{\"title\":\"SEO\"}}")
            .when().post("/api/articles").then().statusCode(201)
            .body("data.data.seo.title", is("SEO"));
    }
}
