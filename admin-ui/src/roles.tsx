import React, { useState } from "react";

interface Policy { role: string; resource: string; action: string; }

export const RolesPage: React.FC = () => {
  const [policies] = useState<Policy[]>([
    { role: "admin", resource: "*", action: "*" },
    { role: "author", resource: "articles", action: "create,read,update" },
    { role: "editor", resource: "*", action: "read,update" },
    { role: "viewer", resource: "*", action: "read" },
  ]);

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>Roles & Row Policies</h2>
      <p>Configure role-based access control and row-level security policies.</p>
      <div style={{ marginTop: 16, background: "white", borderRadius: 8, overflow: "hidden" }}>
        <table style={{ width: "100%", borderCollapse: "collapse" }}>
          <thead><tr style={{ background: "#f0f0f0", textAlign: "left" }}><th style={th}>Role</th><th style={th}>Resource</th><th style={th}>Actions</th></tr></thead>
          <tbody>{policies.map((p, i) => (
            <tr key={i} style={{ borderBottom: "1px solid #eee" }}>
              <td style={td}><strong>{p.role}</strong></td><td style={td}>{p.resource}</td><td style={td}><code>{p.action}</code></td>
            </tr>
          ))}</tbody>
        </table>
      </div>
      <p style={{ marginTop: 16, color: "#666", fontSize: 13 }}>
        Row policies are enforced through X-Tenant header and SecuredDocumentService. RLS bypass is available via X-Bypass-RLS: true header for authorized users.
      </p>
    </div>
  );
};

const th: React.CSSProperties = { padding: "8px 12px", borderBottom: "2px solid #ddd" };
const td: React.CSSProperties = { padding: "8px 12px" };
