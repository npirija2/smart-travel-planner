import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { getCurrentUser, loginUser, registerUser } from "../../api/userService";

const AuthContext = createContext(null);

function readStoredToken() {
  return localStorage.getItem("token");
}

function clearStoredAuth() {
  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
}

function isTokenExpired(token) {
  try {
    const [, payload] = token.split(".");
    if (!payload) {
      return true;
    }

    const decodedPayload = JSON.parse(atob(payload));
    if (!decodedPayload.exp) {
      return false;
    }

    return decodedPayload.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(readStoredToken);
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function hydrateUser() {
      const storedToken = readStoredToken();

      if (!storedToken) {
        setToken(null);
        setCurrentUser(null);
        setLoading(false);
        return;
      }

      if (isTokenExpired(storedToken)) {
        clearStoredAuth();
        setToken(null);
        setCurrentUser(null);
        setLoading(false);
        return;
      }

      try {
        setToken(storedToken);
        const user = await getCurrentUser();
        setCurrentUser(user);
      } catch (error) {
        clearStoredAuth();
        setToken(null);
        setCurrentUser(null);
      } finally {
        setLoading(false);
      }
    }

    hydrateUser();
  }, []);

  const value = useMemo(
    () => ({
      token,
      currentUser,
      loading,
      isAuthenticated: Boolean(token),
      async login(credentials) {
        const authData = await loginUser(credentials.email, credentials.password);
        localStorage.setItem("token", authData.accessToken);
        if (authData.refreshToken) {
          localStorage.setItem("refreshToken", authData.refreshToken);
        }

        setToken(authData.accessToken);
        const user = await getCurrentUser();
        setCurrentUser(user);
        return user;
      },
      async register(payload) {
        return registerUser(payload);
      },
      logout() {
        clearStoredAuth();
        setToken(null);
        setCurrentUser(null);
      },
    }),
    [currentUser, loading, token],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}
