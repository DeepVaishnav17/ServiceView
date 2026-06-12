import api from "../api/axios";

export async function placeOrder(payload) {
    const response = await api.post("/api/order", payload);
    return response.data;
}
