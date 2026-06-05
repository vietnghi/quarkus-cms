package io.quarkiverse.cms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;

/**
 * Tests CMS with SQLite datasource. Verifies that Flyway migration, Hibernate
 * ORM, and the DocumentService all work against a SQLite database.
 */
@QuarkusTest
@TestProfile(SQLiteTestProfile.class)
class SQLiteTest {

    @Test
    void createAndReadEntry() {
        String id = given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"SQLite-Test\",\"body\":\"SQLite body\"}")
            .when().post("/api/articles")
            .then().statusCode(201)
            .body("data.id", notNullValue())
            .body("data.contentType", is("article"))
            .body("data.data.title", is("SQLite-Test"))
            .extract().path("data.id");

        given()
            .when().get("/api/articles/" + id)
            .then().statusCode(200)
            .body("data.id", is(id));
    }

    @Test
    void listEntries() {
        given()
            .when().get("/api/articles")
            .then().statusCode(200)
            .body("meta.pagination.page", is(1));
    }
}
