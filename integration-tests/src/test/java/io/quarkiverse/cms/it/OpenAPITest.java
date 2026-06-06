package io.quarkiverse.cms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class OpenAPITest {

    @Test
    void hasPerTypeArticlePaths() {
        String body = given()
            .when().get("/q/openapi")
            .then().statusCode(200).extract().asString();

        org.hamcrest.MatcherAssert.assertThat(body, anyOf(
            containsString("/api/articles"), containsString("articles:")));
    }

    @Test
    void hasTypedRequestResponses() {
        String body = given()
            .when().get("/q/openapi")
            .then().statusCode(200).extract().asString();

        org.hamcrest.MatcherAssert.assertThat(body, containsString("operationId"));
        org.hamcrest.MatcherAssert.assertThat(body, containsString("pageSize"));
        org.hamcrest.MatcherAssert.assertThat(body, containsString("filters"));
    }
}
