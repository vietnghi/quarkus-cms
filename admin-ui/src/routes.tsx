import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { LoginPage } from "./login";
import { AdminLayout, ContentList } from "./layout";
import { ContentForm } from "./content-form";
import { MediaPage } from "./media";
import { RolesPage } from "./roles";

export const AppRoutes: React.FC = () => (
  <Routes>
    <Route path="/login" element={<LoginPage />} />
    <Route path="/" element={<AdminLayout />}>
      <Route index element={<Dashboard />} />
      <Route path="content-manager/:plural" element={<ContentList />} />
      <Route path="content-manager/:plural/create" element={<ContentForm />} />
      <Route path="content-manager/:plural/edit/:id" element={<ContentForm />} />
      <Route path="media" element={<MediaPage />} />
      <Route path="roles" element={<RolesPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Route>
  </Routes>
);

const Dashboard: React.FC = () => (
  <div>
    <h1>Quarkus CMS Admin</h1>
    <p>Select a content type from the sidebar to manage entries.</p>
    <p style={{ marginTop: 16 }}><a href="/q/dev-ui">Quarkus Dev UI</a> &middot; <a href="/q/swagger-ui">Swagger UI</a></p>
  </div>
);
