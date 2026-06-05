package io.quarkiverse.cms.runtime.document;

import java.time.Instant;
import java.util.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.cms.runtime.document.Query.Filter;
import io.quarkiverse.cms.runtime.document.Query.SortField;
import io.quarkiverse.cms.runtime.model.CmsEntry;
import io.quarkiverse.cms.runtime.model.CmsRelation;
import io.quarkiverse.cms.runtime.model.SchemaRegistry;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

@ApplicationScoped
@Transactional
public class PanacheDocumentService implements DocumentService {

    private static final Logger LOG = Logger.getLogger(PanacheDocumentService.class);

    @Inject SchemaRegistry registry;
    @Inject EntityManager em;
    @Inject ObjectMapper objectMapper;

    protected EntityManager getEntityManager() { return em; }

    @Override
    public PagedResult<Document> find(String contentType, Query query) {
        PanacheQuery<CmsEntry> base;
        if (query.status() != null && !query.status().isBlank())
            base = CmsEntry.findByContentTypeAndStatus(contentType, query.status());
        else
            base = CmsEntry.findByContentType(contentType);

        long total = base.count();
        int page = Math.max(1, query.page());
        int ps = Math.max(1, Math.min(100, query.pageSize()));
        base.page(page - 1, ps);
        List<CmsEntry> entries = new ArrayList<>(base.list());

        // In-memory filter
        if (query.filters() != null && !query.filters().isEmpty())
            entries.removeIf(e -> !matchesFilters(e, query.filters()));
        // In-memory sort
        if (query.sort() != null && !query.sort().isEmpty())
            entries.sort((a,b) -> compareBySort(a,b,query.sort()));

        List<Document> docs = entries.stream().map(e -> toDocument(e, query)).collect(java.util.stream.Collectors.toList());
        return new PagedResult<>(docs, total, page, ps);
    }

    @Override
    public Document findOne(String contentType, String id, Query query) {
        Optional<CmsEntry> opt = CmsEntry.findByIdAndContentType(id, contentType);
        if (opt.isEmpty()) return null;
        return toDocument(opt.get(), query != null ? query : Query.defaults());
    }

    @Override
    @Transactional
    public Document create(String contentType, Map<String, Object> data, String locale) {
        CmsEntry entry = new CmsEntry();
        entry.contentType = contentType;
        entry.entryStatus = "draft";
        entry.locale = locale;
        try { entry.dataJson = objectMapper.writeValueAsString(data); }
        catch (Exception e) { throw new RuntimeException("Serialize failed", e); }
        entry.persist();
        return toDocument(entry, Query.defaults());
    }

    @Override
    @Transactional
    public Document update(String contentType, String id, Map<String, Object> data) {
        Optional<CmsEntry> opt = CmsEntry.findByIdAndContentType(id, contentType);
        if (opt.isEmpty()) return null;
        CmsEntry entry = opt.get();
        try { entry.dataJson = objectMapper.writeValueAsString(data); }
        catch (Exception e) { throw new RuntimeException("Serialize failed", e); }
        entry.persist();
        return toDocument(entry, Query.defaults());
    }

    @Override
    @Transactional
    public void delete(String contentType, String id) {
        CmsEntry.delete("id = ?1 and contentType = ?2", id, contentType);
    }

    @Override
    @Transactional
    public Document publish(String contentType, String id) {
        Optional<CmsEntry> opt = CmsEntry.findByIdAndContentType(id, contentType);
        if (opt.isEmpty()) return null;
        CmsEntry e = opt.get();
        e.entryStatus = "published";
        e.persist();
        return toDocument(e, Query.defaults());
    }

    @Override
    @Transactional
    public Document unpublish(String contentType, String id) {
        Optional<CmsEntry> opt = CmsEntry.findByIdAndContentType(id, contentType);
        if (opt.isEmpty()) return null;
        CmsEntry e = opt.get();
        e.entryStatus = "draft";
        e.persist();
        return toDocument(e, Query.defaults());
    }

    // -- toDocument with populate --

    @SuppressWarnings("unchecked")
    protected Document toDocument(CmsEntry entry, Query query) {
        Map<String, Object> data = new HashMap<>();
        if (entry.dataJson != null && !entry.dataJson.isBlank()) {
            try { data = objectMapper.readValue(entry.dataJson, Map.class); }
            catch (JsonProcessingException e) { LOG.warnf(e, "Parse error %s", entry.id); }
        }
        // Fields projection
        if (query.fields() != null && !query.fields().isEmpty()) {
            Map<String, Object> p = new HashMap<>();
            for (String f : query.fields()) { if (data.containsKey(f)) p.put(f, data.get(f)); }
            data = p;
        }
        // Populate relations
        if (query.populate() != null && !query.populate().isEmpty()) {
            for (String relField : query.populate()) {
                List<CmsRelation> rels = CmsRelation.find(
                    "sourceContentType = ?1 and sourceEntryId = ?2 and fieldName = ?3",
                    entry.contentType, entry.id, relField).list();
                if (!rels.isEmpty()) {
                    List<Map<String, Object>> related = new ArrayList<>();
                    for (CmsRelation r : rels) {
                        Document doc = findOne(r.targetContentType, r.targetEntryId, Query.defaults());
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", r.targetEntryId);
                        m.put("contentType", r.targetContentType);
                        if (doc != null) m.put("data", doc.data());
                        related.add(m);
                    }
                    data.put(relField, related.size() == 1 ? related.get(0) : related);
                }
            }
        }
        return new Document(entry.id, entry.contentType, entry.entryStatus, entry.locale,
                entry.createdAt, entry.updatedAt, data);
    }

    // -- In-memory JSONB filtering and sorting --

    @SuppressWarnings("unchecked")
    protected boolean matchesFilters(CmsEntry entry, Map<String, List<Filter>> filterGroups) {
        Map<String, Object> data;
        try { data = entry.dataJson != null ? objectMapper.readValue(entry.dataJson, Map.class) : Map.of(); }
        catch (Exception e) { return true; }
        return filterGroups.entrySet().stream().allMatch(fg -> {
            Object val = data.get(fg.getKey());
            return fg.getValue().stream().allMatch(f -> {
                if (val == null) return false;
                return switch (f.operator()) {
                    case "$eq" -> Objects.equals(val.toString(), f.value());
                    case "$ne" -> !Objects.equals(val.toString(), f.value());
                    case "$contains" -> val.toString().toLowerCase().contains(f.value().toLowerCase());
                    case "$startsWith" -> val.toString().startsWith(f.value());
                    case "$endsWith" -> val.toString().endsWith(f.value());
                    case "$lt" -> compareValues(val, f.value()) < 0;
                    case "$lte" -> compareValues(val, f.value()) <= 0;
                    case "$gt" -> compareValues(val, f.value()) > 0;
                    case "$gte" -> compareValues(val, f.value()) >= 0;
                    case "$in" -> List.of(f.value().split(",")).contains(val.toString());
                    default -> true;
                };
            });
        });
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    protected int compareBySort(CmsEntry a, CmsEntry b, List<SortField> sortFields) {
        for (SortField sf : sortFields) {
            Object va = extractDataField(a, sf.field());
            Object vb = extractDataField(b, sf.field());
            int cmp = 0;
            if (va instanceof Comparable && vb instanceof Comparable)
                cmp = ((Comparable) va).compareTo(vb);
            else
                cmp = String.valueOf(va).compareTo(String.valueOf(vb));
            if (cmp != 0) return sf.dir().equalsIgnoreCase("desc") ? -cmp : cmp;
        }
        return 0;
    }

    private Object extractDataField(CmsEntry entry, String field) {
        try {
            Map<String, Object> data = objectMapper.readValue(entry.dataJson, Map.class);
            return data.getOrDefault(field, "");
        } catch (Exception e) { return ""; }
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private int compareValues(Object a, String b) {
        if (a instanceof Number n && b != null) {
            try { return Double.compare(n.doubleValue(), Double.parseDouble(b)); }
            catch (NumberFormatException e) { return a.toString().compareTo(b); }
        }
        return a.toString().compareTo(b);
    }
}
