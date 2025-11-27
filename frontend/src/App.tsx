import { useState, useEffect } from "react";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import "./styles/App.css";

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);

  // Check login status on app load
  useEffect(() => {
    checkLoginStatus();
  }, []);

  const checkLoginStatus = async (): Promise<void> => {
    try {
      // Try to access protected endpoint with credentials (cookies)
      const response = await fetch("http://localhost:8080/api/domains", {
        method: "GET",
        headers: {
          "Accept": "application/json"
        },
        credentials: "include" // Send cookies with request
      });

      if (response.ok) {
        setIsAuthenticated(true);
      } else {
        setIsAuthenticated(false);
      }
    } catch (error) {
      console.error("Not logged in or Server Error:", error);
      setIsAuthenticated(false);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async (): Promise<void> => {
    try {
      // Call custom /signout endpoint
      await fetch("http://localhost:8080/signout", {
        method: "POST",
        credentials: "include" // Important: Send cookies for session invalidation
      });
    } catch (error) {
      console.error("Logout failed:", error);
    }

    // Clear any local storage
    sessionStorage.clear();

    // Redirect to login page
    window.location.href = "http://localhost:5173/";
  };

  if (loading) {
    return (
      <div style={{
        color: "white",
        textAlign: "center",
        marginTop: "20%"
      }}>
        <h2>Loading...</h2>
      </div>
    );
  }

  return (
    <div>
      {isAuthenticated ? (
        <Dashboard onLogout={handleLogout} />
      ) : (
        <Login />
      )}
    </div>
  );
}

export default App;

