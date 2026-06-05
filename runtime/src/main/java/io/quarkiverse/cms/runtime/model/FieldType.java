package io.quarkiverse.cms.runtime.model;

/** The set of field types the Content-Type Builder can produce (Strapi parity). */
public enum FieldType {
    STRING,
    TEXT,
    RICHTEXT,
    NUMBER,
    BOOLEAN,
    DATE,
    DATETIME,
    EMAIL,
    PASSWORD,
    ENUMERATION,
    JSON,
    UID,
    MEDIA,
    RELATION,
    COMPONENT,
    DYNAMIC_ZONE
}
