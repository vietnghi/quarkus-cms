package com.acme.cms.types;

import java.util.List;

import io.quarkiverse.cms.runtime.annotation.ContentType;
import io.quarkiverse.cms.runtime.annotation.Field;
import io.quarkiverse.cms.runtime.annotation.Relation;
import io.quarkiverse.cms.runtime.annotation.RowPolicy;
import io.quarkiverse.cms.runtime.annotation.TenantScoped;
import io.quarkiverse.cms.runtime.model.ContentType.Kind;

/**
 * EXAMPLE code-first content type (Revision 2). The class is the single source of
 * truth: discovered at build time, turned into a Panache entity + auto REST/GraphQL,
 * tenant-isolated, and protected by a row-level policy so authors only touch their
 * own articles.
 */
@ContentType(api = "article", plural = "articles", kind = Kind.COLLECTION, draftAndPublish = true)
@TenantScoped
@RowPolicy(
        name = "own-articles",
        expression = "author.id = :currentUserId",
        appliesTo = { RowPolicy.Action.READ, RowPolicy.Action.UPDATE, RowPolicy.Action.DELETE },
        roles = "author")
public class Article {

    @Field(required = true, unique = true)
    public String title;

    @Field(localized = true)
    public String body;

    @Relation(Relation.Kind.MANY_TO_ONE)
    public Author author;

    // @Component Seo seo;                 // reusable field group (Phase 1)
    // @DynamicZone(of = {Hero.class}) ... // mixed-component layout (Phase 1)

    public static class Author {
        @Field(required = true) public String id;
        @Field public String name;
    }
}
