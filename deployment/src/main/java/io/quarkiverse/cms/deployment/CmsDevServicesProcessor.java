package io.quarkiverse.cms.deployment;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Produce;
import io.quarkus.deployment.builditem.FeatureBuildItem;

/**
 * Placeholder for Dev Services + Dev UI wiring (Phase 0/2).
 * Will: auto-start Postgres in dev, seed an admin user, and contribute a Dev UI
 * card linking to the admin panel and a content-type browser.
 */
public class CmsDevServicesProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    @Produce(FeatureBuildItem.class)
    void devServicesPlaceholder() {
        // TODO Phase 0: DevServicesResultBuildItem for Postgres
        // TODO Phase 2: CardPageBuildItem for the Dev UI
    }
}
