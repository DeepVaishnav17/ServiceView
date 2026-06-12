import { useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";
import { useAuth } from "../context/AuthContext";
import { getAllProducts } from "../services/productService";
import { placeOrder } from "../services/orderService";
import { connectToNotificationStream, getRecentNotifications } from "../services/notificationService";

function formatPrice(value) {
    const numericValue = Number(value);
    if (Number.isNaN(numericValue)) {
        return "₹0.00";
    }
    return new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }).format(numericValue);
}

function getErrorMessage(error, fallback) {
    return error?.response?.data?.message
        || (typeof error?.response?.data === "string" ? error.response.data : null)
        || fallback;
}

function HomePage() {
    const { username, logout } = useAuth();
    const [products, setProducts] = useState([]);
    const [productLoading, setProductLoading] = useState(true);
    const [placingForId, setPlacingForId] = useState(null);
    const [quantities, setQuantities] = useState({});
    const [orderResponse, setOrderResponse] = useState("");
    const [notifications, setNotifications] = useState([]);
    const [streamState, setStreamState] = useState("connecting");

    const productCount = useMemo(() => products.length, [products]);

    useEffect(() => {
        const loadProducts = async () => {
            setProductLoading(true);
            try {
                const response = await getAllProducts();
                const normalized = response.map((product, index) => ({
                    id: product.id || `${product.skuCode || product.name || "product"}-${index}`,
                    skuCode: product.skuCode || "",
                    name: product.name || "Unnamed Product",
                    description: product.description || "No description available",
                    price: product.price ?? 0,
                }));
                setProducts(normalized);
            } catch (error) {
                toast.error(getErrorMessage(error, "Unable to fetch products."));
            } finally {
                setProductLoading(false);
            }
        };

        loadProducts();
    }, []);

    useEffect(() => {
        let eventSource;

        const bootstrapNotifications = async () => {
            try {
                const recent = await getRecentNotifications();
                setNotifications(Array.isArray(recent) ? recent : []);
            } catch {
                // ignore, realtime stream can still provide updates
            }

            eventSource = connectToNotificationStream(
                (notification) => {
                    setNotifications((previous) => [notification, ...previous].slice(0, 50));
                    if (notification?.message) {
                        toast.success(notification.message);
                    }
                },
                (status) => setStreamState(status)
            );
        };

        bootstrapNotifications();

        return () => {
            if (eventSource) {
                eventSource.close();
            }
        };
    }, []);

    const handleQuantityChange = (productId, value) => {
        const parsed = Number.parseInt(value, 10);
        setQuantities((previous) => ({
            ...previous,
            [productId]: Number.isNaN(parsed) || parsed < 1 ? 1 : parsed,
        }));
    };

    const handlePlaceOrder = async (product) => {
        const quantity = quantities[product.id] || 1;
        const skuCode = product.skuCode || product.name;

        if (!skuCode) {
            toast.error("No SKU available for this product.");
            return;
        }

        setPlacingForId(product.id);
        try {
            const message = await placeOrder({
                orderLineItemsDtos: [
                    {
                        skuCode,
                        price: product.price,
                        quantity,
                    },
                ],
            });

            const resolvedMessage = typeof message === "string" ? message : "Order request completed.";
            setOrderResponse(resolvedMessage);

            if (resolvedMessage.toLowerCase().includes("success")) {
                toast.success(resolvedMessage);
            } else {
                toast(resolvedMessage);
            }
        } catch (error) {
            const resolvedError = getErrorMessage(error, "Order request failed.");
            setOrderResponse(resolvedError);
            toast.error(resolvedError);
        } finally {
            setPlacingForId(null);
        }
    };

    return (
        <main className="home-page">
            <header className="home-header">
                <div>
                    <h1>Microservice Store</h1>
                    <p>Welcome{username ? `, ${username}` : ""} • Products: {productCount}</p>
                </div>
                <button type="button" onClick={logout}>
                    Logout
                </button>
            </header>

            <section className="status-strip">
                <span>Notification stream: {streamState}</span>
                {orderResponse ? <span>Last order response: {orderResponse}</span> : null}
            </section>

            <div className="home-grid">
                <section className="card">
                    <h2>Products</h2>
                    {productLoading ? (
                        <p>Loading products...</p>
                    ) : products.length === 0 ? (
                        <p>No products found.</p>
                    ) : (
                        <div className="product-list">
                            {products.map((product) => (
                                <article key={product.id} className="product-card">
                                    <h3>{product.name}</h3>
                                    <p>{product.description}</p>
                                    <p><strong>SKU:</strong> {product.skuCode || product.name || "Not available"}</p>
                                    <p><strong>Price:</strong> {formatPrice(product.price)}</p>

                                    <label htmlFor={`qty-${product.id}`}>Quantity</label>
                                    <input
                                        id={`qty-${product.id}`}
                                        type="number"
                                        min="1"
                                        value={quantities[product.id] || 1}
                                        onChange={(event) => handleQuantityChange(product.id, event.target.value)}
                                    />

                                    <button
                                        type="button"
                                        onClick={() => handlePlaceOrder(product)}
                                        disabled={placingForId === product.id}
                                    >
                                        {placingForId === product.id ? "Placing..." : "Place order"}
                                    </button>
                                </article>
                            ))}
                        </div>
                    )}
                </section>

                <aside className="card">
                    <h2>Realtime notifications</h2>
                    {notifications.length === 0 ? (
                        <p>No notifications yet.</p>
                    ) : (
                        <ul className="notification-list">
                            {notifications.map((notification) => (
                                <li key={notification.id || `${notification.orderNumber}-${notification.createdAt}`}>
                                    <p><strong>{notification.title || notification.type || "Notification"}</strong></p>
                                    <p>{notification.message || "Order update received."}</p>
                                    {notification.orderNumber ? (
                                        <p><small>Order: {notification.orderNumber}</small></p>
                                    ) : null}
                                    {notification.createdAt ? (
                                        <p><small>{new Date(notification.createdAt).toLocaleString()}</small></p>
                                    ) : null}
                                </li>
                            ))}
                        </ul>
                    )}
                </aside>
            </div>
        </main>
    );
}

export default HomePage;
