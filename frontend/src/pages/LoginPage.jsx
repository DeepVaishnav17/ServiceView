import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { loginUser, registerUser } from "../services/authService";
import { useAuth } from "../context/AuthContext";

function getErrorMessage(error, fallback) {
    return error?.response?.data?.message
        || (typeof error?.response?.data === "string" ? error.response.data : null)
        || fallback;
}

function LoginPage() {
    const navigate = useNavigate();
    const { isAuthenticated, login } = useAuth();

    const [mode, setMode] = useState("login");
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({
        username: "",
        password: "",
    });

    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormData((previous) => ({
            ...previous,
            [name]: value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        if (!formData.username.trim() || !formData.password.trim()) {
            toast.error("Username and password are required.");
            return;
        }

        setLoading(true);
        try {
            if (mode === "register") {
                const message = await registerUser(formData);
                toast.success(typeof message === "string" ? message : "Registration successful. Please login.");
                setMode("login");
                return;
            }

            const token = await loginUser(formData);
            if (!token || typeof token !== "string") {
                throw new Error("Auth service returned an invalid token response.");
            }

            login(token);
            toast.success("Login successful.");
            navigate("/", { replace: true });
        } catch (error) {
            const fallback = mode === "login" ? "Login failed." : "Registration failed.";
            toast.error(getErrorMessage(error, fallback));
        } finally {
            setLoading(false);
        }
    };

    if (isAuthenticated) {
        return <Navigate to="/" replace />;
    }

    return (
        <main className="auth-page">
            <section className="auth-card">
                <h1>Microservice Store</h1>
                <p>{mode === "login" ? "Sign in to continue" : "Create your account"}</p>

                <form onSubmit={handleSubmit} className="auth-form">
                    <label htmlFor="username">Username</label>
                    <input
                        id="username"
                        name="username"
                        type="text"
                        value={formData.username}
                        onChange={handleChange}
                        placeholder="Enter username"
                        autoComplete="username"
                    />

                    <label htmlFor="password">Password</label>
                    <input
                        id="password"
                        name="password"
                        type="password"
                        value={formData.password}
                        onChange={handleChange}
                        placeholder="Enter password"
                        autoComplete={mode === "login" ? "current-password" : "new-password"}
                    />

                    <button type="submit" disabled={loading}>
                        {loading ? "Please wait..." : mode === "login" ? "Login" : "Register"}
                    </button>
                </form>

                <button
                    type="button"
                    className="text-button"
                    onClick={() => setMode((previous) => (previous === "login" ? "register" : "login"))}
                    disabled={loading}
                >
                    {mode === "login"
                        ? "Need an account? Register"
                        : "Already registered? Login"}
                </button>
            </section>
        </main>
    );
}

export default LoginPage;
