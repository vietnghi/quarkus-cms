import React, { useEffect, useState } from "react";
import { useList, useLogout } from "@refinedev/core";
import { Outlet, Link, useLocation, useParams } from "react-router-dom";

interface TypeMeta { apiName: string; pluralName: string; kind: string; draftAndPublish: boolean; fields: FieldMeta[]; }
interface FieldMeta { name: string; type: string; required: boolean; unique: boolean; localized: boolean; }

export const AdminLayout: React.FC = () => {
  const [types, setTypes] = useState<TypeMeta[]>([]);
  const { mutate: logout } = useLogout();
  const location = useLocation();

  useEffect(() => {
    fetch("/cms-admin/api/types").then(r => r.json()).then(setTypes).catch(console.error);
  }, []);

  return (
    <div style={{ display: "flex", minHeight: "100vh" }}>
      <nav style={{ width: 220, background: "#1a1a2e", color: "white", padding: "12px 0" }}>
        <div style={{ padding: "0 16px 16px", fontWeight: "bold", fontSize: 18, borderBottom: "1px solid #333" }}>Quarkus CMS</div>
        {types.map(t => {
          const active = location.pathname.includes(`/content-manager/${t.pluralName}`);
          return (
            <React.Fragment key={t.apiName}>
              <Link to={`/content-manager/${t.pluralName}`} style={{ display: "block", padding: "8px 16px", color: active ? "white" : "#ccc", textDecoration: "none", background: active ? "#333" : "transparent" }}>{t.pluralName}</Link>
              {active && <Link to={`/content-manager/${t.pluralName}/create`} style={{ display: "block", padding: "4px 24px", color: "#8cf", fontSize: 13, textDecoration: "none" }}>+ New {t.apiName}</Link>}
            </React.Fragment>
          );
        })}
        <div style={{ marginTop: 20, padding: "0 16px" }}>
          <button onClick={() => logout()} style={{ background: "#c44", color: "white", border: "none", padding: "6px 12px", borderRadius: 4, cursor: "pointer" }}>Logout</button>
        </div>
      </nav>
      <main style={{ flex: 1, padding: 24, background: "#f5f5f5" }}><Outlet /></main>
    </div>
  );
};

export const ContentList: React.FC = () => {
  const { plural = "articles" } = useParams<{ plural: string }>();
  const [meta, setMeta] = useState<TypeMeta | null>(null);

  useEffect(() => {
    fetch("/cms-admin/api/types").then(r => r.json()).then((types: TypeMeta[]) => {
      setMeta(types.find(x => x.pluralName === plural) ?? null);
    });
  }, [plural]);

  const { data, isLoading } = useList({ resource: plural });
  if (isLoading) return <div>Loading...</div>;

  const entries: any[] = (data?.data ?? []) as any[];

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>{plural}</h2>
        <Link to={`/content-manager/${plural}/create`} style={{ padding: "8px 16px", background: "#1a73e8", color: "white", borderRadius: 4, textDecoration: "none" }}>+ New Entry</Link>
      </div>
      <table style={{ width: "100%", borderCollapse: "collapse", background: "white" }}>
        <thead><tr style={{ background: "#f0f0f0", textAlign: "left" }}><th style={th}>ID</th>{meta?.fields?.map(f => <th key={f.name} style={th}>{f.name}</th>)}<th style={th}>Status</th><th style={th}>Actions</th></tr></thead>
        <tbody>{entries.map((entry: any) => (
          <tr key={entry.id} style={{ borderBottom: "1px solid #eee" }}>
            <td style={td}>{entry.id?.substring(0, 8)}</td>
            {meta?.fields?.map(f => <td key={f.name} style={td}>{String(entry?.data?.[f.name] ?? entry?.[f.name] ?? "-")}</td>)}
            <td style={td}>{entry.status}</td>
            <td style={td}><Link to={`/content-manager/${plural}/edit/${entry.id}`} style={{ color: "#1a73e8", textDecoration: "none" }}>Edit</Link></td>
          </tr>
        ))}</tbody>
      </table>
    </div>
  );
};

const th: React.CSSProperties = { padding: "8px 12px", borderBottom: "2px solid #ddd" };
const td: React.CSSProperties = { padding: "8px 12px" };
