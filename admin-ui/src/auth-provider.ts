export interface AuthBindings {
  login: (params: any) => Promise<{ success: boolean; error?: Error }>;
  logout: () => Promise<{ success: boolean }>;
  check: () => Promise<{ authenticated: boolean }>;
  getIdentity: () => Promise<{ id: string; name: string } | undefined>;
  getPermissions: () => Promise<string[]>;
  onError: (error: any) => Promise<{ error?: Error; logout?: boolean }>;
}

/**
 * Auth provider for Quarkus CMS admin.
 * Uses JWT bearer tokens in Authorization header.
 * Reads from localStorage for persistence.
 */
export const authProvider = (): AuthBindings => ({
  login: async ({ username, password }) => {
    try {
      const res = await fetch("/cms-admin/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      if (!res.ok) return { success: false, error: new Error("Invalid credentials") };
      const json = await res.json();
      localStorage.setItem("token", json.token);
      localStorage.setItem("user", JSON.stringify(json.user ?? { id: username, name: username }));
      return { success: true };
    } catch {
      return { success: false, error: new Error("Network error") };
    }
  },
  logout: () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    return Promise.resolve({ success: true });
  },
  check: () => {
    const token = localStorage.getItem("token");
    return Promise.resolve({ authenticated: !!token });
  },
  getIdentity: () => {
    const user = localStorage.getItem("user");
    if (!user) return Promise.resolve(undefined);
    return Promise.resolve(JSON.parse(user));
  },
  getPermissions: async () => {
    try {
      const res = await fetch("/cms-admin/api/auth/me", {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
      });
      if (!res.ok) return [];
      const json = await res.json();
      return json.roles ?? [];
    } catch {
      return [];
    }
  },
  onError: () => Promise.resolve({ logout: true }),
});
