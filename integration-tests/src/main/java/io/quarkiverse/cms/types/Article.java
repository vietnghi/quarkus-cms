package io.quarkiverse.cms.types;

import io.quarkiverse.cms.runtime.annotation.ContentType;
import io.quarkiverse.cms.runtime.model.ContentType.Kind;

@ContentType(api = "article", plural = "articles", kind = Kind.COLLECTION)
public class Article {
    public String title;
    public String body;
}
