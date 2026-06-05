package io.quarkiverse.cms.it;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import io.quarkiverse.cms.runtime.media.StorageProvider;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class StorageProviderTest {
    @Inject StorageProvider storage;
    @Test void providerIsLocalByDefault() { assertEquals("local", storage.name()); }
}
