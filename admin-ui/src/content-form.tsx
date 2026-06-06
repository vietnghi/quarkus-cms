import React, { useEffect, useState } from "react";
import { useForm, useList } from "@refinedev/core";
import { useParams, useNavigate } from "react-router-dom";

interface TypeMeta {
  apiName: string; pluralName: string; kind: string;
  draftAndPublish: boolean; fields: FieldMeta[];
}
interface FieldMeta {
  name: string; type: string; required: boolean;
  unique: boolean; localized: boolean;
}

/** Form for creating/editing entries. Reads type metadata from /types to build fields. */
export const ContentForm: React.FC = () => {
  const { plural = "articles", id } = useParams<{ plural: string; id?: string }>();
  const navigate = useNavigate();
  const [meta, setMeta] = useState<TypeMeta | null>(null);
  const [formData, setFormData] = useState<Record<string, any>>({});
  const [status, setStatus] = useState("draft");

  useEffect(() => {
    fetch("/cms-admin/api/types").then(r => r.json()).then((types: TypeMeta[]) => {
      const t = types.find(x => x.pluralName === plural);
      if (t) setMeta(t);
    });
  }, [plural]);

  // Load existing entry for edit mode
  useEffect(() => {
    if (id) {
      fetch(`/cms-admin/api/content-types/${plural}/entries/${id}`)
        .then(r => r.json())
        .then(json => {
          if (json.data) {
            setFormData(json.data.data ?? {});
            setStatus(json.data.status ?? "draft");
          }
        });
    }
  }, [id, plural]);

  const handleChange = (field: string, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleSave = async () => {
    const url = id
      ? `/cms-admin/api/content-types/${plural}/entries/${id}`
      : `/cms-admin/api/content-types/${plural}/entries`;
    const method = id ? "PUT" : "POST";
    const body: any = { ...formData };
    if (meta?.draftAndPublish) body.status = status;

    const res = await fetch(url, {
      method, headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (res.ok) {
      const json = await res.json();
      navigate(`/content-manager/${plural}`);
    }
  };

  if (!meta) return <div>Loading...</div>;

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>{id ? "Edit" : "Create"} {meta.apiName}</h2>
      <div style={{ background: "white", padding: 20, borderRadius: 8, maxWidth: 600 }}>
        {meta.fields.map(f => (
          <div key={f.name} style={{ marginBottom: 12 }}>
            <label style={{ display: "block", fontWeight: "bold", marginBottom: 4 }}>
              {f.name} {f.required && "*"}
            </label>
            {f.type === "TEXT" || f.type === "STRING" ? (
              <input style={input} value={formData[f.name] ?? ""} onChange={e => handleChange(f.name, e.target.value)}
                placeholder={f.name} />
            ) : f.type === "NUMBER" ? (
              <input style={input} type="number" value={formData[f.name] ?? ""}
                onChange={e => handleChange(f.name, e.target.value)} />
            ) : f.type === "BOOLEAN" ? (
              <input type="checkbox" checked={!!formData[f.name]} onChange={e => handleChange(f.name, e.target.checked)} />
            ) : (
              <input style={input} value={formData[f.name] ?? ""} onChange={e => handleChange(f.name, e.target.value)} />
            )}
          </div>
        ))}
        {meta.draftAndPublish && (
          <div style={{ marginBottom: 12 }}>
            <label style={{ display: "block", fontWeight: "bold", marginBottom: 4 }}>Status</label>
            <select value={status} onChange={e => setStatus(e.target.value)} style={input}>
              <option value="draft">Draft</option>
              <option value="published">Published</option>
            </select>
          </div>
        )}
        <div style={{ display: "flex", gap: 8 }}>
          <button onClick={handleSave} style={btn}>Save</button>
          <button onClick={() => navigate(`/content-manager/${plural}`)} style={{ ...btn, background: "#ccc" }}>Cancel</button>
        </div>
      </div>
    </div>
  );
};

const input: React.CSSProperties = { width: "100%", padding: "8px 12px", border: "1px solid #ddd", borderRadius: 4, boxSizing: "border-box" };
const btn: React.CSSProperties = { padding: "10px 20px", background: "#1a73e8", color: "white", border: "none", borderRadius: 4, cursor: "pointer" };
