package io.quarkiverse.cms.it;

import io.restassured.http.ContentType;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class RelationsTest {

    @Test void testPopulateWithRelation() {
        String p = "Pop" + System.currentTimeMillis();
        String mainId = given().contentType(ContentType.JSON)
            .body("{\"title\":\"" + p + "-main\",\"body\":\"Main\"}")
            .when().post("/api/articles").then().statusCode(201)
            .extract().path("data.id");

        String relId = given().contentType(ContentType.JSON)
            .body("{\"title\":\"" + p + "-rel\",\"body\":\"Rel\"}")
            .when().post("/api/articles").then().statusCode(201)
            .extract().path("data.id");

        given().contentType(ContentType.JSON)
            .body("{\"sourceEntryId\":\"" + mainId + "\","
                + "\"targetEntryId\":\"" + relId + "\","
                + "\"fieldName\":\"related\",\"relationKind\":\"ONE_TO_ONE\"}")
            .when().post("/cms-admin/api/relations").then().statusCode(201);

        given().queryParam("populate", "related")
            .when().get("/api/articles/" + mainId)
            .then().statusCode(200).body("data.id", is(mainId))
            .body("data.data.related.id", is(relId));
    }

    @Test void testComponentRoundTrip() {
        String p = "Comp" + System.currentTimeMillis();
        given().contentType(ContentType.JSON)
            .body("{\"title\":\"" + p + "\",\"seo\":{\"title\":\"SEO-Title\"}}")
            .when().post("/api/articles").then().statusCode(201)
            .body("data.data.seo.title", is("SEO-Title"));
    }

    @Test void testDynamicZoneRoundTrip() {
        String p = "DZ" + System.currentTimeMillis();
        given().contentType(ContentType.JSON)
            .body("{\"title\":\"" + p + "\",\"layout\":["
                + "{\"__component\":\"hero\",\"heading\":\"Hi\"},"
                + "{\"__component\":\"quote\",\"text\":\"Hello\"}]}")
            .when().post("/api/articles").then().statusCode(201)
            .body("data.data.layout[0].__component", is("hero"))
            .body("data.data.layout[1].__component", is("quote"));
    }
}
