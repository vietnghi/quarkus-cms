import React from "react";
import { Outlet } from "react-router-dom";

export const AdminLayout: React.FC = () => (
  <div style={{ minHeight: "100vh", background: "#f5f5f5" }}>
    <header style={{ background: "#1a1a2e", color: "white", padding: "12px 24px" }}>
      <strong>Quarkus CMS</strong> Admin
    </header>
    <main style={{ padding: 24 }}>
      <Outlet />
    </main>
  </div>
);
