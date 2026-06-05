package io.quarkiverse.cms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class CodegenTest {
    @Test void rejectsEmptyApiName() {
        given().contentType(ContentType.JSON)
            .body("{}")
            .when().post("/cms-admin/api/codegen/content-types")
            .then().statusCode(400);
    }
    @Test void simpleTypeGeneration() {
        given().contentType(ContentType.JSON)
            .body("{\"className\":\"Category\",\"apiName\":\"category\",\"fields\":[]}")
            .when().post("/cms-admin/api/codegen/content-types")
            .then().statusCode(200)
            .body("status", is("created"));
    }
}
