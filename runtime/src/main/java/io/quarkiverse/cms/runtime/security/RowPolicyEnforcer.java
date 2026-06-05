package io.quarkiverse.cms.runtime.security;

public interface RowPolicyEnforcer {
    void apply(SecurityContext ctx);
}
