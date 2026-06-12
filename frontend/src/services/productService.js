import api from "../api/axios";

export async function getAllProducts(vendorName) {
    const url = vendorName ? `/api/product?vendorName=${encodeURIComponent(vendorName)}` : "/api/product";
    const response = await api.get(url);
    return Array.isArray(response.data) ? response.data : [];
}

export async function createProduct(productData) {
    const response = await api.post("/api/product", productData);
    return response.data;
}

export async function updateProduct(id, productData) {
    const response = await api.put(`/api/product/${id}`, productData);
    return response.data;
}

export async function deleteProduct(id) {
    const response = await api.delete(`/api/product/${id}`);
    return response.data;
}
