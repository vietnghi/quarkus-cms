package io.quarkiverse.cms.runtime.model;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

@Entity
@Table(name = "cms_entry")
public class CmsEntry extends PanacheEntityBase {
    @Id public String id;
    @Column(name = "content_type", nullable = false) public String contentType;
    @Column(name = "entry_status") public String entryStatus = "draft";
    @Column(name = "locale") public String locale;
    @Column(name = "created_at") public Instant createdAt;
    @Column(name = "updated_at") public Instant updatedAt;
    @Column(name = "data_json", columnDefinition = "TEXT") public String dataJson;
    @Column(name = "tenant_id") public String tenantId;
    @Column(name = "created_by") public String createdBy;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    public static PanacheQuery<CmsEntry> findByContentType(String ct) { return find("contentType", ct); }
    public static PanacheQuery<CmsEntry> findByContentTypeAndStatus(String ct, String st) {
        return find("contentType = ?1 and entryStatus = ?2", ct, st);
    }
    public static Optional<CmsEntry> findByIdAndContentType(String id, String ct) {
        return find("id = ?1 and contentType = ?2", id, ct).firstResultOptional();
    }
}
