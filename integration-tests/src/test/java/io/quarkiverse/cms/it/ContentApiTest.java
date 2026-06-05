package io.quarkiverse.cms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class ContentApiTest {
    private static int seq = 100;

    private synchronized String next() { return "t" + (seq++); }

    @Test void listEmpty() {
        given().when().get("/api/articles").then().statusCode(200)
            .body("meta.pagination.page", is(1))
            .body("meta.pagination.pageSize", is(25));
    }

    @Test void createAndRead() {
        String prefix = next();
        String id = given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"" + prefix + "-create\",\"body\":\"First article\"}")
            .when().post("/api/articles")
            .then().statusCode(201).body("data.id", notNullValue())
            .body("data.contentType", is("article"))
            .body("data.status", is("draft"))
            .body("data.data.title", is(prefix + "-create"))
            .extract().path("data.id");
        given().when().get("/api/articles/" + id).then().statusCode(200)
            .body("data.id", is(id));
    }

    @Test void updateEntry() {
        String prefix = next();
        String id = given().contentType(ContentType.JSON)
            .body("{\"title\":\"" + prefix + "-orig\"}").when().post("/api/articles")
            .then().statusCode(201).extract().path("data.id");
        given().contentType(ContentType.JSON)
            .body("{\"title\":\"" + prefix + "-updated\"}")
            .when().put("/api/articles/" + id).then().statusCode(200)
            .body("data.data.title", is(prefix + "-updated"));
    }

    @Test void deleteEntry() {
        String prefix = next();
        String id = given().contentType(ContentType.JSON)
            .body("{\"title\":\"" + prefix + "-del\"}").when().post("/api/articles")
            .then().statusCode(201).extract().path("data.id");
        given().when().delete("/api/articles/" + id).then().statusCode(204);
        given().when().get("/api/articles/" + id).then().statusCode(404);
    }

    @Test void testPagination() {
        String prefix = next();
        for (int i = 0; i < 3; i++)
            given().contentType(ContentType.JSON)
                .body("{\"title\":\"" + prefix + "-p" + i + "\"}")
                .when().post("/api/articles").then().statusCode(201);
        given().param("page", 1).param("pageSize", 2).when().get("/api/articles")
            .then().statusCode(200)
            .body("meta.pagination.pageSize", is(2))
            .body("meta.pagination.total", greaterThanOrEqualTo(3));
    }

    @Test void testFieldsProjection() {
        String prefix = next();
        given().contentType(ContentType.JSON)
            .body("{\"title\":\"" + prefix + "-f\",\"body\":\"Hidden\"}")
            .when().post("/api/articles").then().statusCode(201);
        given().param("fields", "title").when().get("/api/articles")
            .then().statusCode(200).body("data.size()", greaterThanOrEqualTo(1));
    }

    @Test void testFilterExactMatch() {
        String prefix = next();
        given().contentType(ContentType.JSON)
            .body("{\"title\":\"" + prefix + "-exact\",\"body\":\"Find me\"}")
            .when().post("/api/articles").then().statusCode(201);
        given().queryParam("filters", "{\"title\":{\"$eq\":\"" + prefix + "-exact\"}}")
            .when().get("/api/articles").then().statusCode(200)
            .body("data.size()", greaterThanOrEqualTo(1))
            .body("data[0].data.title", is(prefix + "-exact"));
    }

    @Test void testFilterContains() {
        String prefix = next();
        String t = prefix + "-containsTarget";
        given().contentType(ContentType.JSON)
            .body("{\"title\":\"" + t + "\"}").when().post("/api/articles")
            .then().statusCode(201);
        given().queryParam("filters", "{\"title\":{\"$contains\":\"containsTarget\"}}")
            .when().get("/api/articles").then().statusCode(200)
            .body("data.size()", greaterThanOrEqualTo(1))
            .body("data[0].data.title", is(t));
    }

    @Test void testSort() {
        String prefix = next();
        given().contentType(ContentType.JSON)
            .body("{\"title\":\"" + prefix + "-Z\"}").when().post("/api/articles").then().statusCode(201);
        given().contentType(ContentType.JSON)
            .body("{\"title\":\"" + prefix + "-A\"}").when().post("/api/articles").then().statusCode(201);
        given().queryParam("sort", "title:asc")
            .queryParam("filters", "{\"title\":{\"$startsWith\":\"" + prefix + "\"}}")
            .when().get("/api/articles").then().statusCode(200)
            .body("data[0].data.title", is(prefix + "-A"));
    }

    @Test void unknownTypeReturns404() {
        given().when().get("/api/nonexistent").then().statusCode(404);
    }

    @Test void unknownEntryReturns404() {
        given().when().get("/api/articles/nonexistent-id").then().statusCode(404);
    }

    @Test void updateNonExistentReturns404() {
        given().contentType(ContentType.JSON)
            .body("{\"title\":\"Nope\"}")
            .when().put("/api/articles/nonexistent-id").then().statusCode(404);
    }
}
