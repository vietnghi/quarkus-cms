package io.quarkiverse.cms.runtime.model;

import java.util.UUID;
import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "cms_entry_relation")
public class CmsRelation extends PanacheEntityBase {
    @Id public String id;
    @Column(name = "source_content_type", nullable = false) public String sourceContentType;
    @Column(name = "source_entry_id", nullable = false) public String sourceEntryId;
    @Column(name = "field_name", nullable = false) public String fieldName;
    @Column(name = "target_content_type", nullable = false) public String targetContentType;
    @Column(name = "target_entry_id", nullable = false) public String targetEntryId;
    @Column(name = "relation_kind", nullable = false) public String relationKind;
    @Column(name = "sort_order") public int sortOrder;
    @PrePersist void prePersist() { if (id == null) id = UUID.randomUUID().toString(); }
}
