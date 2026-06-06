import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { AdminLayout } from "./layout";

export const AppRoutes: React.FC = () => (
  <Routes>
    <Route path="/" element={<AdminLayout />}>
      <Route index element={<Dashboard />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Route>
  </Routes>
);

const Dashboard: React.FC = () => (
  <div style={{ padding: 24 }}>
    <h1>Quarkus CMS Admin</h1>
    <p>Content-Type Builder and Content Manager loaded from /cms-admin/api/types</p>
  </div>
);
