import React, { useState } from "react";
import { useLogin } from "@refinedev/core";

export const LoginPage: React.FC = () => {
  const { mutate: login } = useLogin();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  return (
    <div style={{ maxWidth: 400, margin: "100px auto", padding: 24, background: "white", borderRadius: 8, boxShadow: "0 2px 8px rgba(0,0,0,0.1)" }}>
      <h2 style={{ marginBottom: 20 }}>Quarkus CMS Login</h2>
      <div style={{ marginBottom: 12 }}>
        <input style={input} placeholder="Username" value={username} onChange={e => setUsername(e.target.value)} />
      </div>
      <div style={{ marginBottom: 12 }}>
        <input style={input} type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} />
      </div>
      <button style={{ width: "100%", padding: 10, background: "#1a73e8", color: "white", border: "none", borderRadius: 4, cursor: "pointer" }}
        onClick={() => login({ username, password })}>
        Sign In
      </button>
    </div>
  );
};

const input: React.CSSProperties = { width: "100%", padding: "8px 12px", border: "1px solid #ddd", borderRadius: 4, boxSizing: "border-box" };
