import React, { useEffect, useState } from "react";
import { useList, type BaseRecord } from "@refinedev/core";
import { Outlet, Link, useLocation, useParams } from "react-router-dom";

interface TypeMeta {
  apiName: string;
  pluralName: string;
  kind: string;
  draftAndPublish: boolean;
  fields: FieldMeta[];
}

interface FieldMeta {
  name: string;
  type: string;
  required: boolean;
  unique: boolean;
  localized: boolean;
}

/** Admin layout with dynamic sidebar populated from /cms-admin/api/types */
export const AdminLayout: React.FC = () => {
  const [types, setTypes] = useState<TypeMeta[]>([]);
  const location = useLocation();

  useEffect(() => {
    fetch("/cms-admin/api/types")
      .then(r => r.json())
      .then(setTypes)
      .catch(console.error);
  }, []);

  return (
    <div style={{ display: "flex", minHeight: "100vh" }}>
      <nav style={{ width: 220, background: "#1a1a2e", color: "white", padding: "12px 0" }}>
        <div style={{ padding: "0 16px 16px", fontWeight: "bold", fontSize: 18, borderBottom: "1px solid #333" }}>
          Quarkus CMS
        </div>
        {types.map(t => (
          <Link
            key={t.apiName}
            to={`/content-manager/${t.pluralName}`}
            style={{
              display: "block", padding: "8px 16px", color: "#ccc",
              textDecoration: "none",
              background: location.pathname.includes(t.pluralName) ? "#333" : "transparent"
            }}
          >
            {t.pluralName}
          </Link>
        ))}
      </nav>
      <main style={{ flex: 1, padding: 24, background: "#f5f5f5" }}>
        <Outlet />
      </main>
    </div>
  );
};

/** Dynamic content manager list page — auto-adapts to any type from /types metadata */
export const ContentList: React.FC = () => {
  const { plural = "articles" } = useParams<{ plural: string }>();
  const [meta, setMeta] = useState<TypeMeta | null>(null);

  useEffect(() => {
    fetch("/cms-admin/api/types")
      .then(r => r.json())
      .then((types: TypeMeta[]) => {
        const t = types.find(x => x.pluralName === plural);
        if (t) setMeta(t);
      });
  }, [plural]);

  const { data, isLoading } = useList({ resource: plural });

  if (isLoading) return <div>Loading...</div>;

  const entries = data?.data ?? [];

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>{plural}</h2>
      <table style={{ width: "100%", borderCollapse: "collapse", background: "white" }}>
        <thead>
          <tr style={{ background: "#f0f0f0", textAlign: "left" }}>
            <th style={th}>ID</th>
            {meta?.fields?.map(f => <th key={f.name} style={th}>{f.name}</th>)}
            <th style={th}>Status</th>
          </tr>
        </thead>
        <tbody>
          {entries.map((entry: any) => (
            <tr key={entry.id} style={{ borderBottom: "1px solid #eee" }}>
              <td style={td}>{entry.id?.substring(0, 8)}</td>
              {meta?.fields?.map(f => (
                <td key={f.name} style={td}>{String(entry?.data?.[f.name] ?? entry?.[f.name] ?? "-")}</td>
              ))}
              <td style={td}>{entry.status}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

const th: React.CSSProperties = { padding: "8px 12px", borderBottom: "2px solid #ddd" };
const td: React.CSSProperties = { padding: "8px 12px" };
