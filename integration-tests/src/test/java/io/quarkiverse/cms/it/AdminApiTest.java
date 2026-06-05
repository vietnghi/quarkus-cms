package io.quarkiverse.cms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class AdminApiTest {

    @Test void spaIsServed() {
        given().when().get("/cms-admin/index.html").then().statusCode(200);
    }
    @Test void listContentTypes() {
        given().when().get("/cms-admin/api/content-types").then().statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }
    @Test void getArticleContentType() {
        given().when().get("/cms-admin/api/content-types/article").then().statusCode(200)
            .body("apiName", is("article"));
    }
    @Test void listEntriesViaAdmin() {
        given().when().get("/cms-admin/api/content-types/articles/entries").then().statusCode(200);
    }
    @Test void unknownContentTypeReturns404() {
        given().when().get("/cms-admin/api/content-types/nonexistent").then().statusCode(404);
    }
}
