package io.quarkiverse.cms.it;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

/**
 * Test profile that switches to SQLite datasource.
 * Uses a file-based SQLite database for testing H2-independent scenarios.
 */
public class SQLiteTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            "quarkus.datasource.db-kind", "other",
            "quarkus.datasource.jdbc.url", "jdbc:sqlite:target/cms-test.db",
            "quarkus.datasource.jdbc.driver", "org.sqlite.JDBC",
            "quarkus.hibernate-orm.dialect", "org.hibernate.community.dialect.SQLiteDialect",
            "quarkus.hibernate-orm.database.generation", "drop-and-create",
            "quarkus.flyway.migrate-at-start", "false"
        );
    }
}
