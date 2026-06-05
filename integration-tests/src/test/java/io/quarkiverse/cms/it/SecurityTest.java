package io.quarkiverse.cms.it;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class SecurityTest {
    @Test void adminApiWorksWithAuth() {
        given().when().get("/cms-admin/api/content-types").then().statusCode(200);
    }
}
