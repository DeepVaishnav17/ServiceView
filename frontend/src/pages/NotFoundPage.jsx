import { Link } from "react-router-dom";

function NotFoundPage() {
    return (
        <div className="not-found-page">
            <div className="not-found-content glass-card">
                <h1>404</h1>
                <h2>Page Not Found</h2>
                <p>Oops! The page you are looking for doesn't exist or has been moved.</p>
                <Link to="/" className="btn-primary" style={{ display: 'inline-block', textDecoration: 'none' }}>
                    Return to Home
                </Link>
            </div>
        </div>
    );
}

export default NotFoundPage;
