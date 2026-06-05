package io.quarkiverse.cms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class OpenAPITest {

    @Test
    void hasPerTypeArticlePaths() {
        String body = given()
            .when().get("/q/openapi")
            .then()
            .statusCode(200)
            .extract().asString();

        // Verify per-type article paths exist in YAML format
        org.hamcrest.MatcherAssert.assertThat(
            "OpenAPI must contain /api/articles list path",
            body, containsString("  /api/articles:"));

        org.hamcrest.MatcherAssert.assertThat(
            "OpenAPI must contain /api/articles/{id} detail path",
            body, containsString("/api/articles/{id}"));

        org.hamcrest.MatcherAssert.assertThat(
            "OpenAPI must have listArticle operationId",
            body, containsString("listArticle"));

        org.hamcrest.MatcherAssert.assertThat(
            "OpenAPI must have createArticle operationId",
            body, containsString("createArticle"));

        org.hamcrest.MatcherAssert.assertThat(
            "OpenAPI must have getArticle operationId",
            body, containsString("getArticle"));

        org.hamcrest.MatcherAssert.assertThat(
            "OpenAPI must have updateArticle operationId",
            body, containsString("updateArticle"));

        org.hamcrest.MatcherAssert.assertThat(
            "OpenAPI must have deleteArticle operationId",
            body, containsString("deleteArticle"));

        org.hamcrest.MatcherAssert.assertThat(
            "OpenAPI must have article schema",
            body, containsString("article:"));
    }

    @Test
    void hasTypedRequestResponses() {
        String body = given()
            .when().get("/q/openapi")
            .then()
            .statusCode(200)
            .extract().asString();

        // Verify typed schemas
        org.hamcrest.MatcherAssert.assertThat(
            "Schema must include title field",
            body, containsString("title"));

        org.hamcrest.MatcherAssert.assertThat(
            "Schema must include body field",
            body, containsString("body"));

        org.hamcrest.MatcherAssert.assertThat(
            "Must have pagination schema",
            body, containsString("page"));

        org.hamcrest.MatcherAssert.assertThat(
            "Must have page size parameter",
            body, containsString("pageSize"));

        org.hamcrest.MatcherAssert.assertThat(
            "Must have filter parameter",
            body, containsString("filters"));
    }
}
