import api from "../api/axios";

export async function getAllProducts() {
    const response = await api.get("/api/product");
    return Array.isArray(response.data) ? response.data : [];
}
