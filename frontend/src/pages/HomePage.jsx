import { useEffect, useMemo, useState } from "react";
import toast from "react-hot-toast";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { getAllProducts, createProduct, updateProduct, deleteProduct } from "../services/productService";
import { placeOrder, getOrders } from "../services/orderService";
import { addStock } from "../services/inventoryService";
import { connectToNotificationStream, getRecentNotifications } from "../services/notificationService";

function formatPrice(value) {
    const numericValue = Number(value);
    if (Number.isNaN(numericValue)) return "₹0.00";
    return new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR" }).format(numericValue);
}

function getErrorMessage(error, fallback) {
    return error?.response?.data?.message
        || (typeof error?.response?.data === "string" ? error.response.data : null)
        || fallback;
}

function HomePage() {
    const { username, role, logout } = useAuth();
    const { cart, addToCart, removeFromCart, clearCart, cartTotal } = useCart();
    
    // Normalize role defensively
    const currentRole = (role || "USER").toUpperCase();
    
    const [currentTab, setCurrentTab] = useState(currentRole === "VENDOR" ? "DASHBOARD" : "STORE");
    const [products, setProducts] = useState([]);
    const [orders, setOrders] = useState([]);
    const [productLoading, setProductLoading] = useState(true);
    const [placingOrder, setPlacingOrder] = useState(false);
    const [notifications, setNotifications] = useState([]);
    const [streamState, setStreamState] = useState("connecting");
    const [searchQuery, setSearchQuery] = useState("");

    // Vendor specific states
    const [vendorForm, setVendorForm] = useState({ skuCode: "", name: "", description: "", price: "", category: "", imageUrl: "", quantity: 1 });
    const [addStockAmount, setAddStockAmount] = useState(0);
    const [isEditing, setIsEditing] = useState(false);
    const [editId, setEditId] = useState(null);

    const loadProducts = async () => {
        setProductLoading(true);
        try {
            // Vendors only see their own catalog in Dashboard, but can see all products in Store
            const vendorFilter = currentRole === "VENDOR" && currentTab === "DASHBOARD" ? username : null;
            const response = await getAllProducts(vendorFilter);
            setProducts(response.map((p, i) => ({
                id: p.id || `${p.skuCode || p.name}-${i}`,
                ...p
            })));
        } catch (error) {
            toast.error(getErrorMessage(error, "Unable to fetch products."));
        } finally {
            setProductLoading(false);
        }
    };

    const loadOrders = async () => {
        try {
            const response = await getOrders(username);
            setOrders(response);
        } catch (error) {
            toast.error("Unable to fetch your orders.");
        }
    };

    useEffect(() => { loadProducts(); }, [currentRole, username, currentTab]);
    useEffect(() => { if (currentTab === "ORDERS") loadOrders(); }, [currentTab]);

    useEffect(() => {
        let eventSource;
        const bootstrapNotifications = async () => {
            try {
                const recent = await getRecentNotifications();
                setNotifications(Array.isArray(recent) ? recent : []);
            } catch { }

            eventSource = connectToNotificationStream(
                (notif) => {
                    setNotifications((prev) => [notif, ...prev].slice(0, 50));
                    if (notif?.message) toast.success(notif.message);
                },
                (status) => setStreamState(status)
            );
        };
        bootstrapNotifications();
        return () => { if (eventSource) eventSource.close(); };
    }, []);

    const handleCheckout = async () => {
        if (cart.length === 0) return toast.error("Cart is empty.");
        setPlacingOrder(true);
        try {
            const orderLineItemsDtos = cart.map(item => ({
                skuCode: item.skuCode || item.name,
                price: item.price,
                quantity: item.quantity
            }));
            const message = await placeOrder({ username, orderLineItemsDtos });
            toast.success(typeof message === "string" ? message : "Order placed successfully!");
            clearCart();
            setCurrentTab("ORDERS");
        } catch (error) {
            toast.error(getErrorMessage(error, "Order request failed. Product might be out of stock."));
        } finally {
            setPlacingOrder(false);
        }
    };

    const handleVendorSubmit = async (e) => {
        e.preventDefault();
        try {
            const productData = { ...vendorForm, vendorName: username };
            if (isEditing) {
                await updateProduct(editId, productData);
                if (addStockAmount > 0) {
                    await addStock({ skuCode: vendorForm.skuCode, quantity: addStockAmount });
                }
                toast.success("Product updated!");
            } else {
                await createProduct(productData);
                await addStock({ skuCode: vendorForm.skuCode, quantity: vendorForm.quantity });
                toast.success("Product created and stock added!");
            }
            setVendorForm({ skuCode: "", name: "", description: "", price: "", category: "", imageUrl: "", quantity: 1 });
            setAddStockAmount(0);
            setIsEditing(false);
            setEditId(null);
            loadProducts();
        } catch (err) {
            toast.error("Failed to save product.");
        }
    };

    const editProduct = (p) => {
        setVendorForm({ ...p, quantity: p.quantity || 0 });
        setAddStockAmount(0);
        setIsEditing(true);
        setEditId(p.id);
    };

    const deleteProd = async (id) => {
        if (!window.confirm("Are you sure?")) return;
        try {
            await deleteProduct(id);
            toast.success("Product deleted.");
            loadProducts();
        } catch (err) {
            toast.error("Failed to delete product.");
        }
    };

    const filteredProducts = products.filter(p => p.name?.toLowerCase().includes(searchQuery.toLowerCase()) || p.category?.toLowerCase().includes(searchQuery.toLowerCase()));

    return (
        <main className="home-page">
            <nav className="glass-nav">
                <div className="nav-brand">
                    <h2>Marketplace<span className="dot">.</span></h2>
                </div>
                <div className="nav-tabs">
                    {currentRole === "VENDOR" && (
                        <button className={currentTab === "DASHBOARD" ? "active-tab" : ""} onClick={() => setCurrentTab("DASHBOARD")}>Dashboard</button>
                    )}
                    <button className={currentTab === "STORE" ? "active-tab" : ""} onClick={() => setCurrentTab("STORE")}>Store</button>
                    <button className={currentTab === "CART" ? "active-tab" : ""} onClick={() => setCurrentTab("CART")}>Cart ({cart.length})</button>
                    <button className={currentTab === "ORDERS" ? "active-tab" : ""} onClick={() => setCurrentTab("ORDERS")}>My Orders</button>
                </div>
                <div className="nav-info">
                    <span className="badge role-badge">{currentRole}</span>
                    <span>{username}</span>
                    <button className="btn-logout" onClick={logout}>Sign Out</button>
                </div>
            </nav>

            <div className="dashboard-grid">
                <div className="main-content">
                    {currentTab === "DASHBOARD" && currentRole === "VENDOR" && (
                        <div className="glass-card mb-2">
                            <h3>{isEditing ? "Edit Product" : "Add New Product"}</h3>
                            <form onSubmit={handleVendorSubmit} className="vendor-form">
                                <div className="form-group">
                                    <label>SKU Code</label>
                                    <input placeholder="e.g. IPHONE-12" required value={vendorForm.skuCode} onChange={e => setVendorForm({...vendorForm, skuCode: e.target.value})} disabled={isEditing} />
                                </div>
                                <div className="form-group">
                                    <label>Product Name</label>
                                    <input placeholder="e.g. Apple iPhone 12" required value={vendorForm.name} onChange={e => setVendorForm({...vendorForm, name: e.target.value})} />
                                </div>
                                <div className="form-group">
                                    <label>Category</label>
                                    <input placeholder="e.g. Electronics" value={vendorForm.category} onChange={e => setVendorForm({...vendorForm, category: e.target.value})} />
                                </div>
                                <div className="form-group">
                                    <label>Price (₹)</label>
                                    <input type="number" step="0.01" placeholder="0.00" required value={vendorForm.price} onChange={e => setVendorForm({...vendorForm, price: e.target.value})} />
                                </div>
                                <div className="form-group full-width">
                                    <label>Image URL</label>
                                    <input placeholder="https://example.com/image.jpg" value={vendorForm.imageUrl} onChange={e => setVendorForm({...vendorForm, imageUrl: e.target.value})} />
                                </div>
                                <div className="form-group full-width">
                                    <label>Description</label>
                                    <textarea placeholder="Product description..." value={vendorForm.description} onChange={e => setVendorForm({...vendorForm, description: e.target.value})} />
                                </div>
                                {!isEditing ? (
                                    <div className="form-group">
                                        <label>Initial Stock Quantity</label>
                                        <input type="number" min="1" placeholder="10" required value={vendorForm.quantity} onChange={e => setVendorForm({...vendorForm, quantity: e.target.value})} />
                                    </div>
                                ) : (
                                    <div className="form-group">
                                        <label>Add Stock</label>
                                        <input type="number" min="0" placeholder="0" value={addStockAmount} onChange={e => setAddStockAmount(e.target.value)} />
                                    </div>
                                )}
                                <div className="form-actions">
                                    <button type="submit" className="btn-primary">{isEditing ? "Update Product" : "Create Product"}</button>
                                    {isEditing && <button type="button" className="btn-secondary" onClick={() => { setIsEditing(false); setVendorForm({skuCode:"", name:"", description:"", price:"", category:"", imageUrl:"", quantity:1}); setAddStockAmount(0); }}>Cancel Edit</button>}
                                </div>
                            </form>
                        </div>
                    )}

                    {(currentTab === "STORE" || currentTab === "DASHBOARD") && (
                        <div className="glass-card">
                            <div className="card-header">
                                <h3>{currentTab === "DASHBOARD" ? "My Catalog" : "Discover Products"}</h3>
                                {currentTab === "STORE" && (
                                    <input type="search" placeholder="Search products..." className="search-bar" value={searchQuery} onChange={e => setSearchQuery(e.target.value)} />
                                )}
                            </div>
                            
                            {productLoading ? (
                                <div className="loader"></div>
                            ) : filteredProducts.length === 0 ? (
                                <p className="empty-state">No products found.</p>
                            ) : (
                                <div className="product-grid">
                                    {filteredProducts.map(p => {
                                        const isOutOfStock = p.quantity !== undefined && p.quantity <= 0;
                                        return (
                                        <div key={p.id} className="modern-product-card">
                                            <div className="product-image" style={{backgroundImage: `url(${p.imageUrl || 'https://via.placeholder.com/300x200?text=No+Image'})`}}>
                                                <span className="category-badge">{p.category || "General"}</span>
                                                {p.quantity > 0 && p.quantity <= 5 && <span className="selling-fast-badge">Selling Fast!</span>}
                                            </div>
                                            <div className="product-info">
                                                <h4>{p.name}</h4>
                                                <p className="vendor-name">by {p.vendorName || "Unknown"}</p>
                                                <p className="price">{formatPrice(p.price)}</p>
                                                
                                                {currentTab === "STORE" ? (
                                                    <div className="action-row">
                                                        {isOutOfStock ? (
                                                            <span className="out-of-stock-badge">Out of Stock</span>
                                                        ) : (
                                                            <>
                                                                <button className="btn-primary" onClick={() => { addToCart(p, 1); toast.success("Added to cart"); }}>
                                                                    Add to Cart
                                                                </button>
                                                                <button className="btn-buy-now" onClick={() => { clearCart(); addToCart(p, 1); setCurrentTab("CART"); }}>
                                                                    Buy Now
                                                                </button>
                                                            </>
                                                        )}
                                                    </div>
                                                ) : (
                                                    <div className="action-row vendor-actions">
                                                        <button className="btn-secondary" onClick={() => editProduct(p)}>Edit</button>
                                                        <button className="btn-danger" onClick={() => deleteProd(p.id)}>Delete</button>
                                                        <span className="stock-info">Stock: {p.quantity || 0}</span>
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    )})}
                                </div>
                            )}
                        </div>
                    )}

                    {currentTab === "CART" && (
                        <div className="glass-card">
                            <div className="card-header">
                                <h3>Your Cart</h3>
                            </div>
                            {cart.length === 0 ? (
                                <p className="empty-state">Your cart is empty.</p>
                            ) : (
                                <div className="cart-list">
                                    {cart.map(item => (
                                        <div key={item.id} className="cart-item">
                                            <div className="cart-item-info">
                                                <h4>{item.name}</h4>
                                                <p>{formatPrice(item.price)} x {item.quantity}</p>
                                            </div>
                                            <button className="btn-danger-sm" onClick={() => removeFromCart(item.id)}>Remove</button>
                                        </div>
                                    ))}
                                    <div className="cart-total">
                                        <h4>Total: {formatPrice(cartTotal)}</h4>
                                        <button className="btn-primary" onClick={handleCheckout} disabled={placingOrder}>
                                            {placingOrder ? "Processing..." : "Checkout"}
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>
                    )}

                    {currentTab === "ORDERS" && (
                        <div className="glass-card">
                            <div className="card-header">
                                <h3>Order History</h3>
                            </div>
                            {orders.length === 0 ? (
                                <p className="empty-state">No orders placed yet.</p>
                            ) : (
                                <div className="orders-list">
                                    {orders.map(order => (
                                        <div key={order.id} className="order-card">
                                            <div className="order-header">
                                                <strong>Order #{order.orderNumber.substring(0,8)}</strong>
                                            </div>
                                            <div className="order-items">
                                                {order.orderItemsList?.map(item => (
                                                    <div key={item.id} className="order-item">
                                                        <span>{item.skuCode}</span>
                                                        <span>{formatPrice(item.price)} x {item.quantity}</span>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}
                </div>

                <div className="side-content">
                    <div className="glass-card sticky">
                        <h3>Live Activity <span className={`status-dot ${streamState === 'connected' ? 'live' : ''}`}></span></h3>
                        <p className="stream-status">Status: {streamState}</p>
                        <div className="notification-feed">
                            {notifications.length === 0 ? <p className="empty-state">Quiet right now...</p> : 
                                notifications.map((n, index) => (
                                    <div key={`${n.id || n.orderNumber}-${index}`} className="notif-item">
                                        <div className="notif-icon">📦</div>
                                        <div className="notif-content">
                                            <strong>{n.message || "Order Update"}</strong>
                                            <small>{n.orderNumber && `Order #${n.orderNumber.substring(0,8)}`}</small>
                                        </div>
                                    </div>
                                ))
                            }
                        </div>
                    </div>
                </div>
            </div>
        </main>
    );
}

export default HomePage;
