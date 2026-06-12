import api from "../api/axios";

export async function checkStock(skuCodes) {
    const response = await api.get("/api/inventory", {
        params: { skuCode: skuCodes },
    });
    return response.data;
}

export async function addStock(inventoryRequest) {
    const response = await api.post("/api/inventory", inventoryRequest);
    return response.data;
}

export async function decrementStock(inventoryRequests) {
    const response = await api.post("/api/inventory/decrement", inventoryRequests);
    return response.data;
}
