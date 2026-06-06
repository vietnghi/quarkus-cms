import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { AdminLayout, ContentList } from "./layout";

export const AppRoutes: React.FC = () => (
  <Routes>
    <Route path="/" element={<AdminLayout />}>
      <Route index element={<Dashboard />} />
      <Route path="content-manager/:plural" element={<ContentList />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Route>
  </Routes>
);

const Dashboard: React.FC = () => (
  <div>
    <h1>Quarkus CMS Admin</h1>
    <p>Select a content type from the sidebar to view entries.</p>
  </div>
);
