import "../styles/App.css";

const Login = (): JSX.Element => {
  // Point to our custom /login endpoint (not Spring Security default)
  const handleGoogleLogin = (): void => {
    window.location.href = "http://localhost:8080/login";
  };

  return (
    <div className="login-body">
      <div className="login-container glass-panel">
        <h2>👋 Welcome Back</h2>
        <p>Login with Google (Admin Only)</p>

        <button
          onClick={handleGoogleLogin}
          className="btn btn-primary"
          style={{
            background: "#DB4437",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            gap: "10px",
            fontSize: "1.1rem"
          }}
        >
          <i className="fas fa-envelope"></i> Login with Google
        </button>
      </div>
    </div>
  );
};

export default Login;

