package com.domaincrud.domaincrud.controller;

import com.domaincrud.domaincrud.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${oauth.redirect-uri}")
    private String redirectUri;

    @Value("${oauth.frontend-url}")
    private String frontendUrl;

    private final TokenService tokenService;

    public OAuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Step 1: User clicks "Login with Google"
     * Build Google OAuth authorization URL and redirect
     */
    @GetMapping("/login")
    public String login() {
        String scope = "profile email";
        String authorizationUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=" + scope +
                "&access_type=offline" +  // Request refresh token
                "&prompt=consent";         // Force consent to get refresh token

        return "redirect:" + authorizationUrl;
    }

    /**
     * Step 2: Google redirects back with authorization code
     * Exchange code for tokens and store them
     */
    @GetMapping("/oauth2/callback")
    public String callback(
            @RequestParam(value = "code", required = false) String authorizationCode,
            @RequestParam(value = "error", required = false) String error,
            HttpSession session,
            HttpServletResponse response
    ) {
        // Check if user denied permission
        if (error != null) {
            return "redirect:" + frontendUrl + "?error=access_denied";
        }

        if (authorizationCode == null) {
            return "redirect:" + frontendUrl + "?error=no_code";
        }

        try {
            // Step 3: Exchange authorization code for tokens
            TokenService.TokenResponse tokens = tokenService.exchangeCode(authorizationCode);

            // Step 4: Validate ID token and get user email
            String userEmail = tokenService.validateIdToken(tokens.getIdToken());

            if (userEmail == null) {
                // User not authorized (not in DB or not admin)
                return "redirect:" + frontendUrl + "?error=unauthorized";
            }

            // Step 5: Store tokens in session
            session.setAttribute("access_token", tokens.getAccessToken());
            session.setAttribute("user_email", userEmail);

            // Only store refresh token if present (first time login)
            if (tokens.getRefreshToken() != null) {
                session.setAttribute("refresh_token", tokens.getRefreshToken());
            }

            // Step 6: Store ID token in HTTP-only cookie
            Cookie idTokenCookie = new Cookie("id_token", tokens.getIdToken());
            idTokenCookie.setHttpOnly(true);
            idTokenCookie.setSecure(false); // Set to true in production with HTTPS
            idTokenCookie.setPath("/");
            idTokenCookie.setMaxAge(3600); // 1 hour
            response.addCookie(idTokenCookie);

            // Step 7: Redirect to frontend dashboard
            return "redirect:" + frontendUrl + "/dashboard";

        } catch (Exception e) {
            System.err.println("OAuth callback error: " + e.getMessage());
            return "redirect:" + frontendUrl + "?error=login_failed";
        }
    }

    /**
     * Step 8: Logout - Clear session and cookies
     */
    @PostMapping("/signout")
    public String logout(HttpSession session, HttpServletResponse response) {
        // Remove session attributes
        session.removeAttribute("access_token");
        session.removeAttribute("refresh_token");
        session.removeAttribute("user_email");
        session.invalidate();

        // Clear ID token cookie
        Cookie idTokenCookie = new Cookie("id_token", null);
        idTokenCookie.setHttpOnly(true);
        idTokenCookie.setPath("/");
        idTokenCookie.setMaxAge(0); // Delete cookie
        response.addCookie(idTokenCookie);

        return "redirect:" + frontendUrl;
    }
}
