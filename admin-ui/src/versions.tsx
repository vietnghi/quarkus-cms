import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";

interface Version { id: string; version: number; status: string; updatedAt: string; }

export const VersionHistory: React.FC = () => {
  const { plural = "articles", id } = useParams<{ plural: string; id?: string }>();
  const [versions, setVersions] = useState<Version[]>([]);

  useEffect(() => {
    if (!id) return;
    fetch(`/cms-admin/api/content-types/${plural}/entries/${id}/versions`)
      .then(r => r.ok ? r.json() : [])
      .then(setVersions).catch(() => setVersions([]));
  }, [id, plural]);

  const restore = async (versionId: string) => {
    await fetch(`/cms-admin/api/content-types/${plural}/entries/${id}/versions/${versionId}/restore`, { method: "POST" });
    window.location.reload();
  };

  if (!id || versions.length === 0) return null;

  return (
    <div style={{ margin: "16px 0", padding: 12, background: "#fef3c7", borderRadius: 8 }}>
      <strong>Version History</strong>
      <table style={{ width: "100%", marginTop: 8, fontSize: 13 }}>
        <tbody>
          {versions.map(v => (
            <tr key={v.id}><td>v{v.version}</td><td>{v.status}</td><td>{v.updatedAt?.substring(0,10)}</td>
              <td><button onClick={() => restore(v.id)} style={{ padding: "2px 8px", cursor: "pointer" }}>Restore</button></td></tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
