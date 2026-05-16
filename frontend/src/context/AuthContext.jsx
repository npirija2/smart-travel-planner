import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import {
  clearSession,
  getCurrentUserProfile,
  getStoredSession,
  loginUser,
  refreshAccessToken,
  registerUser,
  storeSession,
} from '../api/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const bootstrap = async () => {
      const session = getStoredSession();

      if (!session?.accessToken) {
        setLoading(false);
        return;
      }

      try {
        const profile = await getCurrentUserProfile();
        setCurrentUser(profile);
      } catch (error) {
        try {
          const refreshed = await refreshAccessToken(session.refreshToken);
          storeSession(refreshed);
          const profile = await getCurrentUserProfile();
          setCurrentUser(profile);
        } catch {
          clearSession();
          setCurrentUser(null);
        }
      } finally {
        setLoading(false);
      }
    };

    bootstrap();
  }, []);

  const value = useMemo(
    () => ({
      currentUser,
      loading,
      isAuthenticated: Boolean(currentUser),
      async login(email, password) {
        const tokens = await loginUser(email, password);
        storeSession(tokens);
        const profile = await getCurrentUserProfile();
        setCurrentUser(profile);
        return profile;
      },
      async register(payload) {
        return registerUser(payload);
      },
      async refreshProfile() {
        const profile = await getCurrentUserProfile();
        setCurrentUser(profile);
        return profile;
      },
      logout() {
        clearSession();
        setCurrentUser(null);
      },
    }),
    [currentUser, loading],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }

  return context;
}
