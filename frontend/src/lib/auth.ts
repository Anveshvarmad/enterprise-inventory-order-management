import type { AuthResponse, CurrentUser } from "../types";

const TOKEN_KEY = "inventory_auth_token";
const USER_KEY = "inventory_auth_user";

export function saveAuth(auth: AuthResponse) {
  localStorage.setItem(TOKEN_KEY, auth.token);

  const user: CurrentUser = {
    id: auth.userId,
    fullName: auth.fullName,
    email: auth.email,
    role: auth.role
  };

  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function getCurrentUser(): CurrentUser | null {
  const raw = localStorage.getItem(USER_KEY);

  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as CurrentUser;
  } catch {
    return null;
  }
}

export function logout() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function isAuthenticated() {
  return Boolean(getToken());
}
