package io.quarkiverse.cms.it;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class MediaTest {
    @Test void uploadAndDownload() {
        given().multiPart("file", "test.txt", "Hello".getBytes())
            .when().post("/cms-admin/api/media")
            .then().statusCode(200).body("path", notNullValue());
    }
}
