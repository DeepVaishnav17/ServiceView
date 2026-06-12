import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { Toaster } from "react-hot-toast";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";

import { CartProvider } from "./context/CartContext";

import NotFoundPage from "./pages/NotFoundPage";

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <Toaster position="top-right" />
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route element={<ProtectedRoute />}>
              <Route path="/" element={<HomePage />} />
            </Route>
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
