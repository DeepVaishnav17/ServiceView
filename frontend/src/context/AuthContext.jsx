import { createContext, useContext, useMemo, useState } from "react";

const TOKEN_KEY = "accessToken";
const AuthContext = createContext(null);

function parseJwtUsername(token) {
    if (!token) {
        return "";
    }

    try {
        const payload = token.split(".")[1];
        const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
        const decoded = JSON.parse(window.atob(normalized));
        return decoded.sub || "";
    } catch {
        return "";
    }
}

export function AuthProvider({ children }) {
    const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY) || "");

    const login = (jwtToken) => {
        localStorage.setItem(TOKEN_KEY, jwtToken);
        setToken(jwtToken);
    };

    const logout = () => {
        localStorage.removeItem(TOKEN_KEY);
        setToken("");
    };

    const value = useMemo(() => ({
        token,
        username: parseJwtUsername(token),
        isAuthenticated: Boolean(token),
        login,
        logout,
    }), [token]);

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used inside AuthProvider.");
    }
    return context;
}
