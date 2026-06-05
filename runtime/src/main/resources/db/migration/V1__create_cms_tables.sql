CREATE TABLE IF NOT EXISTS cms_entry (
    id VARCHAR(36) PRIMARY KEY,
    content_type VARCHAR(255) NOT NULL,
    entry_status VARCHAR(50),
    locale VARCHAR(10),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    data_json TEXT,
    tenant_id VARCHAR(255),
    created_by VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_cms_entry_content_type ON cms_entry(content_type);
CREATE INDEX IF NOT EXISTS idx_cms_entry_tenant ON cms_entry(tenant_id);

CREATE TABLE IF NOT EXISTS cms_entry_relation (
    id VARCHAR(36) PRIMARY KEY,
    source_content_type VARCHAR(255) NOT NULL,
    source_entry_id VARCHAR(36) NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    target_content_type VARCHAR(255) NOT NULL,
    target_entry_id VARCHAR(36) NOT NULL,
    relation_kind VARCHAR(50) NOT NULL,
    sort_order INT DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_relation_source ON cms_entry_relation(source_entry_id, field_name);
