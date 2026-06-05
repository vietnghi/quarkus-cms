package io.quarkiverse.cms.runtime.graphql;
import java.time.Instant; import java.util.Map;

public class GraphQLEntry {
    public String id; public String contentType; public String status; public String locale;
    public String createdAt; public String updatedAt; public String dataJson;
    public static GraphQLEntry from(String id, String ct, String s, String l,
            Instant ca, Instant ua, Map<String,Object> data) {
        GraphQLEntry e = new GraphQLEntry(); e.id = id; e.contentType = ct; e.status = s; e.locale = l;
        e.createdAt = ca != null ? ca.toString() : null; e.updatedAt = ua != null ? ua.toString() : null;
        try { e.dataJson = data != null ? new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data) : "{}"; }
        catch (Exception ex) { e.dataJson = "{}"; } return e;
    }
}
