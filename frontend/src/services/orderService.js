import api from "../api/axios";

export async function placeOrder(payload) {
    const response = await api.post("/api/order", payload);
    return response.data;
}

export async function getOrders(username) {
    const response = await api.get(`/api/order?username=${encodeURIComponent(username)}`);
    return Array.isArray(response.data) ? response.data : [];
}
