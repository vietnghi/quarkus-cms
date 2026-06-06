package io.quarkiverse.cms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/** Tests the server-side rendered admin panel (Qute + HTMX). */
@QuarkusTest
class AdminPageTest {

    @Test
    void dashboardRenders() {
        given()
            .when().get("/cms-admin")
            .then()
            .statusCode(200)
            .body(containsString("Quarkus CMS Admin Dashboard"))
            .body(containsString("Registered Content Types"))
            .body(containsString("articles"))
            .body(containsString("category"));
    }

    @Test
    void listPageRenders() {
        given()
            .when().get("/cms-admin/content/articles")
            .then()
            .statusCode(200)
            .body(containsString("articles"))
            .body(containsString("New article"));
    }

    @Test
    void createFormRenders() {
        given()
            .when().get("/cms-admin/content/articles/create")
            .then()
            .statusCode(200)
            .body(containsString("New article"))
            .body(containsString("title"))
            .body(containsString("body"));
    }

    @Test
    void tableFragmentRenders() {
        given()
            .when().get("/cms-admin/content/articles/table")
            .then()
            .statusCode(200)
            .body(containsString("<table"))
            .body(containsString("Total:"));
    }

    @Test
    void mediaPageRenders() {
        given()
            .when().get("/cms-admin/media")
            .then()
            .statusCode(200)
            .body(containsString("Media Library"));
    }
}
