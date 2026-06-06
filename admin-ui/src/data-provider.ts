import type { DataProvider } from "@refinedev/core";

/**
 * Data provider for the Quarkus CMS admin REST API.
 * Maps Refine operations to Strapi-compatible query params and
 * the {data, meta:{pagination:{total}}} response envelope.
 */
const API = "/cms-admin/api";

function qs(params: Record<string, any>): string {
  const p = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v !== undefined && v !== null) p.append(k, String(v));
  }
  const s = p.toString();
  return s ? "?" + s : "";
}

const quarkusCmsProvider = (apiUrl: string): DataProvider => ({
  getList: async ({ resource, pagination, sorters, filters }) => {
    const query: Record<string, any> = {};
    if (pagination?.current) query.page = pagination.current;
    if (pagination?.pageSize) query.pageSize = pagination.pageSize;
    if (sorters?.length) {
      query.sort = sorters.map(s => `${s.field}:${s.order}`).join(",");
    }
    if (filters?.length) {
      const f: Record<string, any> = {};
      for (const filter of filters) {
        if (filter.operator === "eq") f[filter.field] = { $eq: filter.value };
        else if (filter.operator === "contains") f[filter.field] = { $contains: filter.value };
      }
      if (Object.keys(f).length) query.filters = JSON.stringify(f);
    }
    const res = await fetch(`${apiUrl}/content-types/${resource}/entries${qs(query)}`);
    const json = await res.json();
    return { data: json.data ?? [], total: json.meta?.total ?? 0 };
  },
  getOne: async ({ resource, id }) => {
    const res = await fetch(`${apiUrl}/content-types/${resource}/entries/${id}`);
    const json = await res.json();
    return { data: json.data };
  },
  create: async ({ resource, variables }) => {
    const res = await fetch(`${apiUrl}/content-types/${resource}/entries`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify(variables),
    });
    const json = await res.json();
    return { data: json.data };
  },
  update: async ({ resource, id, variables }) => {
    const res = await fetch(`${apiUrl}/content-types/${resource}/entries/${id}`, {
      method: "PUT", headers: { "Content-Type": "application/json" },
      body: JSON.stringify(variables),
    });
    const json = await res.json();
    return { data: json.data };
  },
  deleteOne: async ({ resource, id }) => {
    await fetch(`${apiUrl}/content-types/${resource}/entries/${id}`, { method: "DELETE" });
    return { data: { id } as any };
  },
  getApiUrl: () => apiUrl,
});

export default quarkusCmsProvider(API);
