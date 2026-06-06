import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { Refine, Authenticated } from "@refinedev/core";
import routerProvider from "@refinedev/react-router";
import dataProvider from "./data-provider";
import { authProvider } from "./auth-provider";
import { AppRoutes } from "./routes";
import { LoginPage } from "./login";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <BrowserRouter>
      <Refine
        dataProvider={dataProvider}
        authProvider={authProvider()}
        routerProvider={routerProvider}
        resources={[]}
        options={{ syncWithLocation: true }}
      >
        <AppRoutes />
      </Refine>
    </BrowserRouter>
  </React.StrictMode>
);
