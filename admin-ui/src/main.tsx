import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { Refine } from "@refinedev/core";
import routerProvider from "@refinedev/react-router";
import dataProvider from "./data-provider";
import { AppRoutes } from "./routes";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <BrowserRouter>
      <Refine
        dataProvider={dataProvider}
        routerProvider={routerProvider}
        resources={[]}
        options={{ syncWithLocation: true }}
      >
        <AppRoutes />
      </Refine>
    </BrowserRouter>
  </React.StrictMode>
);
