import api, { API_BASE_URL } from "../api/axios";

export async function getRecentNotifications() {
    const response = await api.get("/api/notification/recent");
    return Array.isArray(response.data) ? response.data : [];
}

export function connectToNotificationStream(onNotification, onStatusChange) {
    const eventSource = new EventSource(`${API_BASE_URL}/api/notification/stream`);

    eventSource.addEventListener("connected", () => {
        onStatusChange?.("connected");
    });

    eventSource.addEventListener("order-notification", (event) => {
        try {
            const data = JSON.parse(event.data);
            onNotification?.(data);
        } catch {
            // ignore malformed events and keep stream alive
        }
    });

    eventSource.onerror = () => {
        onStatusChange?.("reconnecting");
    };

    return eventSource;
}
