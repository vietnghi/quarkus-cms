import React from "react";
import ReactDOM from "react-dom/client";
import { Refine } from "@refinedev/core";
import routerProvider from "@refinedev/react-router";
import dataProvider from "./data-provider";
import { authProvider } from "./auth-provider";
import { AppRoutes } from "./routes";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <Refine
      dataProvider={dataProvider}
      authProvider={authProvider()}
      routerProvider={routerProvider}
      resources={[]}
      options={{ syncWithLocation: true }}
    >
      <AppRoutes />
    </Refine>
  </React.StrictMode>
);
