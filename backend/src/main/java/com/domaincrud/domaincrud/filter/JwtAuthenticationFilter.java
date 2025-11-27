package com.domaincrud.domaincrud.filter;

import com.domaincrud.domaincrud.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Custom JWT Authentication Filter
 * Runs on every protected request to validate Google ID token
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public JwtAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Step 1: Extract ID token from cookie
            String idToken = extractIdTokenFromCookie(request);

            // Step 2: Fallback to Authorization header if cookie not found
            if (idToken == null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    idToken = authHeader.substring(7);
                }
            }

            // Step 3: Validate token with Google and get user email
            if (idToken != null) {
                String userEmail = tokenService.validateIdToken(idToken);

                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Step 4: Token is valid - Create authentication object
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userEmail,  // Principal (user email)
                                    null,       // Credentials (not needed)
                                    Collections.emptyList()  // Authorities (could add roles here)
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Step 5: Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        } catch (Exception e) {
            System.err.println("JWT Filter error: " + e.getMessage());
            // Don't stop the filter chain even if authentication fails
        }

        // Step 6: Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract ID token from HTTP-only cookie
     */
    private String extractIdTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("id_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Don't apply this filter to public endpoints
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/login") ||
                path.equals("/oauth2/callback") ||
                path.equals("/error");
    }
}